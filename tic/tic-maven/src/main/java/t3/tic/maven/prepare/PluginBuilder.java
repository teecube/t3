package t3.tic.maven.prepare;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class PluginBuilder {

	private Plugin plugin;

	public PluginBuilder(Plugin plugin) {
		this.plugin = plugin;
	}

	public PluginBuilder(String groupId, String artifactId, String version) {
		Plugin plugin = new Plugin();
		plugin.setArtifactId(artifactId);
		plugin.setGroupId(groupId);
		plugin.setVersion(version);

		this.plugin = plugin;
	}

	public void addConfiguration(Xpp3Dom configuration) {
		// TODO: take care of priority (dominant vs recessive)
		this.plugin.setConfiguration(Xpp3Dom.mergeXpp3Dom(configuration, (Xpp3Dom) this.plugin.getConfiguration()));
	}

	public boolean addConfigurationFromClasspath() throws MojoExecutionException {
		String filename = "/plugins-configuration/" +
						  this.plugin.getGroupId() + "/" + 
						  this.plugin.getArtifactId()  + ".xml";

		InputStream configStream = PluginBuilder.class.getResourceAsStream(filename);
		if (configStream == null) return false;

		try {
			String configString = IOUtils.toString(configStream);
			Xpp3Dom pluginConfiguration = Xpp3DomBuilder.build(new ByteArrayInputStream(configString.getBytes()), "UTF-8"); // FIXME: encoding

			if (pluginConfiguration != null) {
				Xpp3Dom configuration = pluginConfiguration.getChild("configuration");
				if (configuration != null) {
					this.plugin.setConfiguration(Xpp3Dom.mergeXpp3Dom((Xpp3Dom) this.plugin.getConfiguration(), configuration));
				}
				
				Xpp3Dom executions = pluginConfiguration.getChild("executions");
				if (executions != null) {
					List<PluginExecution> pluginExecutions = new ArrayList<PluginExecution>();
					pluginExecutions.addAll(this.plugin.getExecutions());
					for (Xpp3Dom execution : executions.getChildren()) {
						if ("execution".equals(execution.getName())) {
							PluginExecution ex = new PluginExecution();
							
							Xpp3Dom inherited = execution.getChild("inherited");
							if (inherited != null && inherited.getValue() != null && !inherited.getValue().isEmpty()) {
								ex.setInherited(inherited.getValue());
							} else {
								ex.setInherited(false);
							}
							Xpp3Dom idDom = execution.getChild("id");
							String id = null;
							if (idDom != null && idDom.getValue() != null && !idDom.getValue().isEmpty()) {
								id = idDom.getValue();
								ex.setId(id);
							}
							Xpp3Dom goalsDom = execution.getChild("goals");
							if (goalsDom != null) {
								List<String> goals = new ArrayList<String>();
								for (Xpp3Dom goal : goalsDom.getChildren()) {
									if (goal.getValue() != null && !goal.getValue().isEmpty()) {
										goals.add(goal.getValue());
									}
								}
								ex.setGoals(goals);
							}
							Xpp3Dom phase = execution.getChild("phase");
							if (phase != null && phase.getValue() != null && !phase.getValue().isEmpty()) {
								ex.setPhase(phase.getValue());
							}

							configuration = execution.getChild("configuration");
							if (configuration != null) {
								ex.setConfiguration(configuration);
							}
							
							if (id != null && !id.isEmpty()) {
								PluginExecution oldEx = this.plugin.getExecutionsAsMap().get(id);
								if (oldEx != null) {									
									oldEx.setConfiguration(Xpp3Dom.mergeXpp3Dom((Xpp3Dom) oldEx.getConfiguration(), (Xpp3Dom) ex.getConfiguration()));
									this.plugin.getExecutionsAsMap().put(id, oldEx);
								}
							} else {
								pluginExecutions.add(ex);
							}
						}
					}
					this.plugin.setExecutions(pluginExecutions);
				}
			}
		} catch (IOException | XmlPullParserException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		return true;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}

	public static MavenProject addPluginFirst(MavenProject mavenProject, Plugin plugin) {
		if (mavenProject == null || plugin == null) return mavenProject;

		List<Plugin> plugins = mavenProject.getBuild().getPlugins();
		if (plugins != null) {
			plugins.add(0, plugin);
		}

		mavenProject.getBuild().setPlugins(plugins);

		return mavenProject;
	}
}
