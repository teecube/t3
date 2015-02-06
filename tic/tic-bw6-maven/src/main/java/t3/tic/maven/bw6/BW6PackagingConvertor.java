/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://www.t3soft.org) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package t3.tic.maven.bw6;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.list.SetUniqueList;
import org.apache.maven.artifact.UnknownRepositoryLayoutException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.StringModelSource;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.BundleException;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;

import t3.POMManager;
import t3.PluginBuilder;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class BW6PackagingConvertor {

	private Logger logger;
	private MavenProject mavenProject;
	private ArtifactRepositoryFactory artifactRepositoryFactory;
	private String tibcoHome;
	private String bw6Version;
	private ProjectBuilder projectBuilder;
	private ProjectBuildingRequest projectBuildingRequest;

	public BW6PackagingConvertor(Logger logger) {
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
			String plugin = BW6Constants.capabilities.get(cap);
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
		 * &lt;dependency-resolution&gt;
		 *   &lt;extraRequirements&gt;
		 *     &lt;requirement&gt;
		 *       &lt;type&gt;eclipse-plugin&lt;/type&gt;
		 *       &lt;id&gt;com.tibco.bw.core.model&lt;/id&gt;
		 *       &lt;versionRange&gt;6.0.0&lt;/versionRange&gt;
		 *     &lt;/requirement&gt;
		 *   &lt;/extraRequirements&gt;
		 * &lt;/dependency-resolution&gt;
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

	private void updateTychoTargetPlatformPlugin(List<String> capabilities) throws XmlPullParserException, IOException {
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

	/**
	 * <p>
	 * Merge configuration in "plugins-configuration" of existing plugins.
	 * </p>
	 *
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	private void updatePluginsConfiguration(boolean createIfNotExists) throws MojoExecutionException, IOException {
		if (mavenProject == null) return;

		if (!createIfNotExists) {
			for (ListIterator<Plugin> it = mavenProject.getBuild().getPlugins().listIterator(); it.hasNext();) {
				Plugin plugin = it.next();

				PluginBuilder pluginBuilder = new PluginBuilder(plugin);

				if (pluginBuilder.addConfigurationFromClasspath()) {
					plugin = pluginBuilder.getPlugin();
				}
				it.set(plugin);
			}
		} else {
			List<File> pluginsConfiguration = getPluginsConfigurationFromClasspath();
			for (File file : pluginsConfiguration) {
				String artifactId = file.getName().replace(".xml", "");
				String groupId = file.getParentFile().getName();
				String pluginKey = groupId+":"+artifactId;

				Plugin plugin = mavenProject.getPlugin(pluginKey);

				PluginBuilder pluginBuilder;
				if (plugin == null) {
					pluginBuilder = new PluginBuilder(groupId, artifactId);
					mavenProject.getBuild().addPlugin(pluginBuilder.getPlugin());
				} else {
					pluginBuilder = new PluginBuilder(plugin);
				}
				pluginBuilder.addConfigurationFromClasspath();

			}
		}
	}

	private List<File> getPluginsConfigurationFromClasspath() {
		List<File> result = new ArrayList<File>();

		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forClass(BW6PackagingConvertor.class))
			.setScanners(new ResourcesScanner())
		);

		Set<String> _files = reflections.getResources(Pattern.compile(".*\\.xml"));
		List<String> files = new ArrayList<String>(_files);

		for (ListIterator<String> it = files.listIterator(); it.hasNext();) {
			String file = (String) it.next();
			if (!file.startsWith("plugins-configuration/")) {
				it.remove();
			}
		}

		for (String file : files) {
			result.add(new File(file));
		}

		logger.debug("Adding plugins from classpath: " + result.toString());

		return result;
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

	private void addBW6P2Repository() throws UnknownRepositoryLayoutException  {
		if (artifactRepositoryFactory != null) {
			ArtifactRepository artifactRepository = artifactRepositoryFactory.createArtifactRepository(
			"tibco.bw6.p2repository",
			"file:///" + mavenProject.getProperties().getProperty("tibco.bw6.p2repository"),
			"p2",
			new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN),
			new ArtifactRepositoryPolicy(false, ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN));

			mavenProject.getRemoteArtifactRepositories().add(artifactRepository);
		}
	}

	private MavenProject prepareBW6Module() throws MojoExecutionException, IOException, XmlPullParserException, BundleException, UnknownRepositoryLayoutException {
		mavenProject.setPackaging("eclipse-plugin"); // change packaging of the POM to "eclipse-plugin"
		mavenProject.getModel().addProperty("tibco.bw6.p2repository", tibcoHome + "/bw/" + bw6Version + "/maven/p2repo");

		updatePluginsConfiguration(true);
		updateTychoTargetPlatformPlugin(getCapabilities(mavenProject));
		addBW6P2Repository();

		return mavenProject;
	}

	public MavenProject prepareBW6AppModule() throws MojoExecutionException, UnknownRepositoryLayoutException, IOException, XmlPullParserException, BundleException {
		return prepareBW6Module();
	}

	public MavenProject prepareBW6SharedModule() throws MojoExecutionException, UnknownRepositoryLayoutException, IOException, XmlPullParserException, BundleException {
		return prepareBW6Module();
	}

	public boolean hasBW6ModuleOrDependency() {
		if (mavenProject == null) return false;

		List<String> modules = mavenProject.getModel().getModules();
		for (String module : modules) {
			try {
				Model model = POMManager.getModelOfModule(mavenProject, module);

				if ("bw6-app-module".equals(model.getPackaging())) {
					return true;
				}
			} catch (IOException | XmlPullParserException e) {
				logger.debug(e.getLocalizedMessage());
			}
		}
		return false;
	}

	private List<Model> getBW6Modules() {
		List<Model> result = new ArrayList<Model>();
		if (mavenProject == null) return result;

		List<String> modules = mavenProject.getModel().getModules();
		for (String module : modules) {
			try {
				Model model = POMManager.getModelOfModule(mavenProject, module);

				if ("bw6-app-module".equals(model.getPackaging()) || // TODO: add OSGi ?
					"bw6-shared-module".equals(model.getPackaging())) {
					result.add(model);
				}
			} catch (IOException | XmlPullParserException e) {
				logger.debug(e.getLocalizedMessage());
			}
		}

		return result;
	}

	/**
	 * <p>
	 * A BW6 application can be defined by a "pom" packaging and have
	 * &lt;modules&gt;.
	 * However, the internal lifecycle to use is the one of "bw6-application"
	 * packaging.
	 * </p>
	 * @return
	 * @throws MojoExecutionException
	 * @throws UnknownRepositoryLayoutException
	 * @throws IOException
	 * @throws XmlPullParserException
	 * @throws BundleException
	 */
	public MavenProject prepareBW6Application() throws MojoExecutionException, UnknownRepositoryLayoutException, IOException, XmlPullParserException, BundleException {
		mavenProject.setPackaging("bw6-application"); // change packaging of the POM from "pom" to "bw6-application"

		// all BW6 modules become dependency
		List<Model> bw6Modules = getBW6Modules();
		for (Model bw6Module : bw6Modules) {
			Dependency dependency = new Dependency();
			dependency.setGroupId(bw6Module.getGroupId());
			dependency.setArtifactId(bw6Module.getArtifactId());
			dependency.setVersion(bw6Module.getVersion());
			dependency.setType(bw6Module.getPackaging());

			mavenProject.getModel().addDependency(dependency);
		}

		// hack: save <modules> and remove them to allow Model validation
		List<String> modules = new ArrayList<String>(mavenProject.getModel().getModules());
		mavenProject.getModel().getModules().clear();

		// convert this hacked Model as a StringModelSource
		MavenXpp3Writer writer = new MavenXpp3Writer();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.write(baos, mavenProject.getModel());
		String model = baos.toString();
		StringModelSource modelSource = new StringModelSource(model);

		// recreate a MavenProject from the hacked model
		// this is useful to have the correct <executions> in this <plugin>
		ProjectBuildingResult projectBuildingResult = null;
		try {
			projectBuildingResult = projectBuilder.build(modelSource, projectBuildingRequest);
		} catch (ProjectBuildingException e) {
			e.printStackTrace();
		}
		MavenProject newMavenProject = projectBuildingResult.getProject();
		newMavenProject.setFile(mavenProject.getFile());

		// hack: put back the <modules> in the Model
		newMavenProject.getModel().getModules().addAll(modules);

		mavenProject = newMavenProject;

		return newMavenProject;
	}

	public void setArtifactRepositoryRepository(ArtifactRepositoryFactory artifactRepositoryFactory) {
		this.artifactRepositoryFactory = artifactRepositoryFactory;
	}

	public void setBW6Version(String bw6Version) {
		this.bw6Version = bw6Version;
	}

	public void setMavenProject(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}

	public void setProjectBuilder(ProjectBuilder projectBuilder) {
		this.projectBuilder = projectBuilder;
	}

	public void setProjectBuildingRequest(ProjectBuildingRequest projectBuildingRequest) {
		this.projectBuildingRequest = projectBuildingRequest;
	}

	public void setTIBCOHome(String tibcoHome) {
		this.tibcoHome = tibcoHome;
	}

}
