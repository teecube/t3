package t3.tic.maven.prepare;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;
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
	 * Create the list of Plugins which provides the capabilities. The list is now hardcoded.
	 *
	 * @param caps the list of capabilities for the given module.
	 *
	 * @return the List of plugins providing the capability.
	 */
	protected List<String> processCapabilites(String caps) {
		List<String> list = new ArrayList<String>();

		if (caps == null || caps.equals("")) {
			return list;
		}
		String[] capArray = caps.split(",");
		for (String cap : capArray) {
			cap = cap.trim();
			String plugin = BWMavenConstants.capabilities.get(cap);
			if (plugin == null || plugin.equals("")) {
				continue;
			}
			list.add(plugin);
		}

		return list;
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

	private List<BW6Requirement> getBW6Requirements(List<String> capabilities) {
		List<BW6Requirement> bw6Requirements = new ArrayList<BW6Requirement>();

		for (String capability : capabilities) {
			BW6Requirement bw6Requirement = new BW6Requirement();
			bw6Requirement.setId(capability);
			bw6Requirement.setType("eclipse-plugin"); // TODO: externalize in static string
			bw6Requirement.setVersionRange("6.0.0"); // FIXME: externalize in configuration

			bw6Requirements.add(bw6Requirement);
		}

		return bw6Requirements;
	}

	private List<Element> getRequirementsConfiguration(List<BW6Requirement> bw6Requirements) {
		List<Element> configuration = new ArrayList<Element>();

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
		configuration.add(
			element("dependency-resolution", 
				element("extraRequirements",
					requirements.toArray(new Element[0])
				)
			)
		);

		return configuration;
	}

	private void addTychoTargetPlatformPlugin(MavenProject mavenProject, List<String> capabilities) throws XmlPullParserException, IOException {
		if (mavenProject == null) return;
		
		List<BW6Requirement> bw6Requirements = SetUniqueList.setUniqueList(new ArrayList<BW6Requirement>());
		bw6Requirements.addAll(getBW6Requirements(mavenProject)); // retrieve from configuration of this plugin
		bw6Requirements.addAll(getBW6Requirements(capabilities));

		// the target-platform-configuration plugin exists because it is part of the lifecycle (see 'plexus/components.xml')
		Plugin tychoTargetPlatformPlugin = mavenProject.getPlugin("org.eclipse.tycho:target-platform-configuration");
		PluginBuilder pluginBuilder = new PluginBuilder(tychoTargetPlatformPlugin);
		
		List<Element> requirementsConfiguration = getRequirementsConfiguration(bw6Requirements);

		pluginBuilder.addConfiguration(configuration(requirementsConfiguration.toArray(new Element[0])));
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

	public File getManifest(MavenProject mavenProject) {
		if (mavenProject == null) return null;

		File manifest = new File(mavenProject.getFile().getParentFile(), "META-INF/MANIFEST.MF");

		return manifest;
	}

	private List<String> getCapabilities(MavenProject mavenProject) throws FileNotFoundException, IOException, BundleException {
		Map<String,String> headers = new HashMap<String,String>();
		ManifestElement.parseBundleManifest(new FileInputStream(getManifest(mavenProject)), headers);

		List<String> capabilities = processCapabilites( headers.get("Require-Capability"));

		return capabilities;
	}

	public MavenProject prepareBW6AppModule(MavenProject mavenProject) throws Exception {
		mavenProject.setPackaging("eclipse-plugin"); // change packaging of the POM to "eclipse-plugin"

		updatePluginsConfiguration(mavenProject);
		addTychoTargetPlatformPlugin(mavenProject, getCapabilities(mavenProject));
		addEnforcerPlugin(mavenProject);

		return mavenProject;
	}

}
