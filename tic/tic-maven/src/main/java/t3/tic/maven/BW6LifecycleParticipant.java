package t3.tic.maven;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.tycho.core.maven.TychoMavenLifecycleParticipant;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "BW6LifecycleListener")
public class BW6LifecycleParticipant extends TychoMavenLifecycleParticipant {

	@Requirement
	private Logger logger;

	List<BW6Requirement> dependencies;

	/**
	 * Used to retrieve self (this plugin)
	 */
	private Plugin getPlugin(MavenProject mavenProject) {
		List<Plugin> plugins = mavenProject.getBuild().getPlugins();
		for (Plugin plugin : plugins) {
			logger.info(plugin.getArtifactId());
			plugin.getConfiguration();
			String config = plugin.getConfiguration().toString();
			
			Xpp3Dom dom = null;
			try {
				dom = Xpp3DomBuilder.build(new ByteArrayInputStream(config.getBytes()),"UTF-8");
			} catch (XmlPullParserException | IOException e) {
				e.printStackTrace();
			}
			
			Xpp3Dom dependenciesNode = dom.getChild(0);
			if (dependenciesNode != null && "dependencies".equals(dependenciesNode.getName())) {
				dependencies = new ArrayList<BW6Requirement>();
				for (Xpp3Dom requirementNode : dependenciesNode.getChildren()) {
					if ("requirement".equals(requirementNode.getName())) {
						Xpp3Dom type = requirementNode.getChild("type");
						Xpp3Dom id = requirementNode.getChild("id");
						Xpp3Dom versionRange = requirementNode.getChild("versionRange");
						if (type != null && id != null && versionRange != null) {
							BW6Requirement requirement = new BW6Requirement();
							requirement.setType(type.getValue());
							requirement.setId(id.getValue());
							requirement.setVersionRange(versionRange.getValue());
							dependencies.add(requirement);
						} else {
							logger.warn("wrong requirement");
						}
					}
				}
			}
		}
		return null;
	}

	private MavenProject prepareBW6AppModule(MavenProject mavenProject) {
		mavenProject.setPackaging("eclipse-plugin");

		Plugin self = getPlugin(mavenProject);

		return mavenProject;
	}

	@Override
	public void afterProjectsRead(MavenSession session)
			throws MavenExecutionException {
		List<MavenProject> projects = session.getProjects();
		for (MavenProject mavenProject : projects) {
			if ("bw6-app-module".equals(mavenProject.getPackaging())) {
				prepareBW6AppModule(mavenProject);
			}
		}
		// super.afterProjectsRead(session);
		logger.info("HELLO WORLD!");
	}
}