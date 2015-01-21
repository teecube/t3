package t3.tic.maven.prepare;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import t3.tic.maven.configuration.BW6Requirement;

/**
 * 
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
public class EclipsePluginConvertor {

	private Logger logger;

	public EclipsePluginConvertor(Logger logger) {
		this.logger = logger;
	}

	/**
	 * Used to retrieve BW6 requirements from this plugin (self) configuration
	 * 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private List<BW6Requirement> getBW6Requirements(MavenProject mavenProject) throws XmlPullParserException, IOException {
		List<BW6Requirement> bw6Requirements = new ArrayList<BW6Requirement>();

		List<Plugin> plugins = mavenProject.getBuild().getPlugins();
		for (Plugin plugin : plugins) {
			if ("tic-maven".equals(plugin.getArtifactId())) { // FIXME: use a pluginDescriptor to identify self
				Object pluginConfiguration = plugin.getConfiguration();
				String config = null;
				if (pluginConfiguration != null) {
					config = plugin.getConfiguration().toString();
				}
				if (config == null) {
					continue;
				}

				Xpp3Dom dom = Xpp3DomBuilder.build(new ByteArrayInputStream(config.getBytes()), "UTF-8"); // FIXME: encoding
	
				for (Xpp3Dom child : dom.getChildren()) {
					if ("dependencies".equals(child.getName())) {
						for (Xpp3Dom requirementNode : child.getChildren()) {
							if ("requirement".equals(requirementNode.getName())) {
								Xpp3Dom type = requirementNode.getChild("type");
								Xpp3Dom id = requirementNode.getChild("id");
								Xpp3Dom versionRange = requirementNode.getChild("versionRange");
								if (type != null && id != null && versionRange != null) {
									BW6Requirement requirement = new BW6Requirement();
									requirement.setType(type.getValue());
									requirement.setId(id.getValue());
									requirement.setVersionRange(versionRange.getValue());
									bw6Requirements.add(requirement);
								} else {
									logger.warn("wrong requirement");
								}
							}
						}
					}
				}

			}
		}

		logger.debug("BW6 requirements:");
		for (BW6Requirement bw6Requirement : bw6Requirements) {
			logger.debug(bw6Requirement.getId());
		}

		return bw6Requirements;
	}

	private List<Element> getTychoTargetPlatformConfiguration(List<BW6Requirement> bw6Requirements) {
		List<Element> configuration = new ArrayList<Element>();

		// <resolver>p2</resolver>
		configuration.add(element("resolver", "p2"));

		/*
		 * <dependency-resolution>
		 *   <extraRequirements>
		 *     <requirement>
		 *       <type>eclipse-plugin</type>
		 *       <id>com.tibco.bw.core.model</id>
		 *       <versionRange>6.0.0</versionRange>
		 *     </requirement>
		 *   </extraRequirements>
		 * </dependency-resolution>
		 */
		// <requirement>
		ArrayList<Element> requirements = new ArrayList<Element>();
		for (BW6Requirement bw6Requirement : bw6Requirements) {
			requirements.add(
				element(
					"requirement",
					element("type", bw6Requirement.getType()),
					element("id", bw6Requirement.getId()),
					element("versionRange", bw6Requirement.getVersionRange())
				)
			);
		}
		// <extraRequirements>
		Element extraRequirements = element("extraRequirements", requirements.toArray(new Element[0]));
		// <dependency-resolution>
		Element dependencyResolution = element("dependency-resolution", extraRequirements);
		configuration.add(dependencyResolution);

		/*
		* <environments>
		*   <environment>
		*     <os>win32</os>
		*     <ws>win32</ws>
		*     <arch>x86</arch>
		*   </environment>
		*   <environment>
		*     <os>linux</os>
		*     <ws>gtk</ws>
		*     <arch>x86_64</arch>
		*   </environment>
		*   <environment>
		*     <os>macosx</os>
		*     <ws>cocoa</ws>
		*     <arch>x86_84</arch>
		*   </environment>
		* </environments>
		*/
		configuration.add(
			element("environments",
				element("environment",
					element("os", "win32"),
					element("ws", "win32"),
					element("arch", "x86")
				),
				element("environment",
					element("os", "linux"),
					element("ws", "gtk"),
					element("arch", "x86_84")
				),
				element("environment",
					element("os", "macosx"),
					element("ws", "cocoa"),
					element("arch", "x86_84")
				)
			)
		);

		return configuration;
	}

	private void addTychoTargetPlatformPlugin(MavenProject mavenProject) throws XmlPullParserException, IOException {
		if (mavenProject == null) return;
		
		List<BW6Requirement> bw6Requirements = getBW6Requirements(mavenProject);

		PluginBuilder pluginBuilder = new PluginBuilder("org.eclipse.tycho", "target-platform-configuration", "0.22.0"); // TODO: externalize, allow external version management by end-user
		
		List<Element> configuration = getTychoTargetPlatformConfiguration(bw6Requirements);

		pluginBuilder.addConfiguration(configuration(configuration.toArray(new Element[0])));
		
		Plugin p = pluginBuilder.getPlugin();
		mavenProject.getBuild().addPlugin(p);
	}

	private void addEnforcerPlugin(MavenProject mavenProject) throws MojoExecutionException {
		if (mavenProject == null) return;

		PluginBuilder pluginBuilder = new PluginBuilder("org.apache.maven.plugins", "maven-enforcer-plugin", "1.3.1"); // TODO: externalize, allow external version management by end-user
		pluginBuilder.addConfigurationFromClasspath();

		Plugin p = pluginBuilder.getPlugin();
		PluginBuilder.addPluginFirst(mavenProject, p);
	}

	/**
	 * <p>
	 * Merge configuration in "plugins-configuration" of existing plugins.
	 * </p>
	 * 
	 * @param mavenProject
	 * @throws MojoExecutionException
	 */
	private void updatePluginsConfiguration(MavenProject mavenProject) throws MojoExecutionException {
		if (mavenProject == null) return;

		for (ListIterator<Plugin> it = mavenProject.getBuild().getPlugins().listIterator(); it.hasNext();) {
			Plugin plugin = (Plugin) it.next();

			PluginBuilder pluginBuilder = new PluginBuilder(plugin);

			if (pluginBuilder.addConfigurationFromClasspath()) {
				plugin = pluginBuilder.getPlugin();
			}
			it.set(plugin);
		}
	}

	public MavenProject prepareBW6AppModule(MavenProject mavenProject) throws XmlPullParserException, IOException, MojoExecutionException {
		mavenProject.setPackaging("eclipse-plugin"); // change packaging of the POM to "eclipse-plugin"

		updatePluginsConfiguration(mavenProject);
		addTychoTargetPlatformPlugin(mavenProject);
		addEnforcerPlugin(mavenProject);

		return mavenProject;
	}

}
