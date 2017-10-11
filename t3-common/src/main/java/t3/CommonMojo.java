/**
 * (C) Copyright 2016-2017 teecube
 * (http://teecu.be) and others.
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
package t3;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.exec.launcher.CommandLauncher;
import org.apache.commons.exec.launcher.CommandLauncherFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.InvocationRequest.ReactorFailureBehavior;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.jboss.shrinkwrap.resolver.api.maven.ConfigurableMavenResolverSystem;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolvedArtifact;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.ConfigurationDistributionStage;
import org.jboss.shrinkwrap.resolver.impl.maven.bootstrap.MavenSettingsBuilder;
import org.slf4j.LoggerFactory;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

import t3.plugin.PluginManager;
import t3.plugin.annotations.GlobalParameter;
import t3.plugin.annotations.injection.ParametersListener;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class CommonMojo extends AbstractMojo {

	@GlobalParameter (property = CommonMojoInformation.executablesExtension, required = true, description = CommonMojoInformation.executablesExtension_description, category = CommonMojoInformation.systemCategory)
	protected String executablesExtension;

	@GlobalParameter (property = CommonMojoInformation.platformArch, required = true, description = CommonMojoInformation.platformArch_description, category = CommonMojoInformation.systemCategory)
	protected String platformArch;

	@GlobalParameter (property = CommonMojoInformation.platformOs, required = true, description = CommonMojoInformation.platformOs_description, category = CommonMojoInformation.systemCategory)
	protected String platformOs;

	@GlobalParameter (property = "project.build.directory", description = CommonMojoInformation.directory_description, defaultValue = "${basedir}/target", category = CommonMojoInformation.mavenCategory) // target
	protected File directory;

	@GlobalParameter (property = "project.output.directory", description = CommonMojoInformation.outputDirectory_description, defaultValue = "${project.build.directory}/output", category = CommonMojoInformation.mavenCategory) // target/output (instead of target/classes)
	protected File outputDirectory;

	@GlobalParameter (property = "project.test.directory", description = CommonMojoInformation.testOutputDirectory_description, defaultValue = "${project.build.directory}/test", category = CommonMojoInformation.mavenCategory) // target/test
	protected File testOutputDirectory;

	@GlobalParameter (property="project.build.sourceEncoding", defaultValue = "UTF-8", description = CommonMojoInformation.sourceEncoding_description, category = CommonMojoInformation.mavenCategory)
	protected String sourceEncoding;

	@Parameter (defaultValue = "${basedir}", readonly = true)
	protected File projectBasedir;

	@Parameter (defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	@Parameter (defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	@Parameter (defaultValue = "${mojoExecution}", readonly = true)
	protected MojoExecution mojoExecution;

	@Parameter (defaultValue = "${plugin}", readonly = true)
	protected PluginDescriptor pluginDescriptor; // plugin descriptor of this plugin

	@Parameter (defaultValue = "${settings}", readonly = true)
	protected Settings settings;

	public static final Pattern mavenPropertyPattern = Pattern.compile("\\$\\{([^}]*)\\}"); // ${prop} regex pattern

	@Component
	protected ProjectBuilder projectBuilder;

	@Component (role=org.apache.maven.shared.filtering.MavenResourcesFiltering.class, hint="default")
	protected MavenResourcesFiltering mavenResourcesFiltering;

	@Component (role = BuildPluginManager.class)
	protected BuildPluginManager pluginManager;

	@Component
	protected PlexusContainer plexus;

	@Component
	protected Logger logger;

	@Component
	protected ArtifactRepositoryFactory artifactRepositoryFactory;

	@Component
	protected RepositorySystem system;

	@org.apache.maven.plugins.annotations.Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
	protected RepositorySystemSession systemSession;

	private static CommonMojo propertiesManager = null;
	/**
	 * <p>
	 * Instantiate a minimalistic {@link CommonMojo} to use properties
	 * management as a standalone object.
	 * </p>
	 *
	 * @param session
	 * @param mavenProject
	 * @return
	 */
	public static CommonMojo propertiesManager(MavenSession session, MavenProject mavenProject) {
		if (propertiesManager != null) {
			propertiesManager.setProject(mavenProject);
			return propertiesManager;
		}
		propertiesManager = new CommonMojo();
		propertiesManager.setProject(mavenProject);
		propertiesManager.setSession(session);
		propertiesManager.setSettings(session.getSettings());

		return propertiesManager;
	}

	/**
	 * <p>
	 * Create the output directory (usually "target/") if it doesn't exist yet.
	 * </p>
	 */
	protected void createOutputDirectory() {
		if (directory == null || directory.exists()) {
			return;
		}

		directory.mkdirs();
	}

	protected void enableTestScope() {
		directory = testOutputDirectory; // set directory to "target/test" instead of "target"
	}

	protected boolean isCurrentGoal(String goal) {
		return session.getRequest().getGoals().contains(goal);
	}

	// properties
	@SuppressWarnings("unchecked") // because of Maven poor typing
	public String getPropertyValueInSettings(String propertyName, Settings settings) {
		if (settings == null) {
			return null;
		}

		List<String> activeProfiles = settings.getActiveProfiles();

		for (Object _profileWithId : settings.getProfilesAsMap().entrySet()) {
			Entry<String, Profile> profileWithId = (Entry<String, Profile>) _profileWithId;
			if (activeProfiles.contains(profileWithId.getKey())) {
				Profile profile = profileWithId.getValue();

				String value = profile.getProperties().getProperty(propertyName);
				if (value != null) {
					return value;
				}
			}
		}

		return null;
	}

	private List<String> getActiveProfiles(Settings settings) {
		if (settings == null) return null;

		List<String> result = settings.getActiveProfiles();
		if (result == null) {
			result = new ArrayList<String>();
		}

		if (settings.getProfiles() != null) {
			for (Profile profile : settings.getProfiles()) {
				if (!result.contains(profile.getId())) {
					if (profile.getActivation() != null && profile.getActivation().isActiveByDefault()) {
						result.add(profile.getId());
					}
				}
			}
		}

		return result;
	}

	@SuppressWarnings("unchecked") // because of Maven poor typing
	public boolean propertyExistsInSettings(String propertyName, Settings settings) {
		if (settings == null) {
			return false;
		}

		List<String> activeProfiles = getActiveProfiles(settings);

		for (Object _profileWithId : settings.getProfilesAsMap().entrySet()) {
			Entry<String, Profile> profileWithId = (Entry<String, Profile>) _profileWithId;
			if (activeProfiles.contains(profileWithId.getKey())) {
				Profile profile = profileWithId.getValue();

				boolean result = profile.getProperties().containsKey(propertyName);
				if (result) {
					return result;
				}
			}
		}

		return false;
	}

	private String getPropertyValueInCommandLine(String propertyName, MavenSession session) {
		if (session == null) {
			return null;
		}

		return session.getRequest().getUserProperties().getProperty(propertyName);
	}

	public boolean propertyExistsInSettings(String propertyName) {
		return propertyExistsInSettings(propertyName, session.getSettings());
	}

	public boolean propertyExists(String propertyName) {
		return propertyExists(project, propertyName);
	}

	public boolean propertyExists(MavenProject mavenProject, String propertyName) {
		return mavenProject.getOriginalModel().getProperties().containsKey(propertyName) ||
			   mavenProject.getModel().getProperties().containsKey(propertyName) ||
			   session.getRequest().getUserProperties().containsKey(propertyName) ||
			   propertyExistsInSettings(propertyName, session.getSettings());
	}

	private String getPropertyValueInOriginalModel(Model originalModel, String propertyName, List<org.apache.maven.model.Profile> activeProfiles) {
		if (originalModel == null || propertyName == null) return null;

		String result = originalModel.getProperties().getProperty(propertyName);

		if (result == null && activeProfiles != null) {
			for (org.apache.maven.model.Profile profile : originalModel.getProfiles()) {
				if (activeProfiles.contains(profile)) {
					result = profile.getProperties().getProperty(propertyName);
				}
			}
		}

		return result;
	}
	public String getPropertyValue(MavenProject mavenProject, String propertyName, boolean lookInSettingsProperties, boolean lookInCommandLine, boolean onlyInOriginalModel) {
		if (mavenProject == null) return null;
		String result = null;

		if (onlyInOriginalModel) {
//			result = mavenProject.getOriginalModel().getProperties().getProperty(propertyName);
			result = getPropertyValueInOriginalModel(mavenProject.getOriginalModel(), propertyName, mavenProject.getActiveProfiles());
		} else {
			result = mavenProject.getModel().getProperties().getProperty(propertyName);
		}
		if (lookInCommandLine && (result == null || result.isEmpty())) {
			boolean wasEmpty = result != null && result.isEmpty();
			result = getPropertyValueInCommandLine(propertyName, session);
			if (result == null && wasEmpty) {
				result = "";
			}
		}
		if (lookInSettingsProperties && (result == null || result.isEmpty())) {
			boolean wasEmpty = result != null && result.isEmpty();
			result = getPropertyValueInSettings(propertyName, settings);
			if (result == null && wasEmpty) {
				result = "";
			}
		}

		if (result == null && ("basedir".equals(propertyName) || "project.basedir".equals(propertyName))) {
			if (mavenProject.getFile() != null && mavenProject.getFile().getParentFile() != null && mavenProject.getFile().getParentFile().isDirectory()) {
				result = mavenProject.getFile().getParentFile().getAbsolutePath();
			}
		} else if (result == null && ("project.groupId".equals(propertyName))) {
			result = mavenProject.getGroupId();
		} else if (result == null && ("project.artifactId".equals(propertyName))) {
			result = mavenProject.getArtifactId();
		} else if (result == null && ("project.version".equals(propertyName))) {
			result = mavenProject.getVersion();
		} else if (result == null && ("user.home".equals(propertyName))) {
			result = System.getProperty("user.home");
		}

		return result;
	}

	public String getPropertyValue(String propertyName, boolean onlyInOriginalModel) {
		return getPropertyValue(project, propertyName, true, true, onlyInOriginalModel);
	}

	public String getPropertyValue(String propertyName) {
		return getPropertyValue(propertyName, false);
	}

	public String getRootProjectProperty(MavenProject mavenProject, String propertyName) {
		return mavenProject == null ? "" : (mavenProject.getParent() == null ? getPropertyValue(mavenProject, propertyName, false, false, false) : getRootProjectProperty(mavenProject.getParent(), propertyName));
	}

	public String getRootProjectProperty(MavenProject mavenProject, String propertyName, boolean onlyInOriginalModel) {
		return mavenProject == null ? "" : (mavenProject.getParent() == null ? getPropertyValue(mavenProject, propertyName, false, false, onlyInOriginalModel) : getRootProjectProperty(mavenProject.getParent(), propertyName, onlyInOriginalModel));
	}

	public String getPropertyValue(String modelPropertyName, boolean propertyInRootProject, boolean onlyInOriginalModel, boolean lookInSettings) {
		String value = null;
		if (lookInSettings) {
			value = getPropertyValueInSettings(modelPropertyName, settings);
		}
		if (value == null) {
			if (propertyInRootProject) {
				value = getRootProjectProperty(project, modelPropertyName, onlyInOriginalModel);
			} else {
				value = getPropertyValue(modelPropertyName, onlyInOriginalModel);
			}
		}
		return value;
	}

	public String replaceProperties(String string) {
		if (string == null) return null;

		Matcher m = mavenPropertyPattern.matcher(string);

		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String propertyKey = m.group(1);
			String propertyValue = getPropertyValue(propertyKey);
			if (propertyValue != null) {
			    m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
			}
		}
		m.appendTail(sb);
		string = sb.toString();

		return string;
	}

	public String replaceProperty(String string, String propertyKey, String propertyValue) {
		if (string == null || propertyKey == null || propertyValue == null) return null;

		Matcher m = Pattern.compile("\\$\\{" + propertyKey + "\\}").matcher(string);

		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
		}
		m.appendTail(sb);
		string = sb.toString();

		return string;
	}
	//

	private static ExecutionEnvironment environment = null;

	protected ExecutionEnvironment getEnvironment() {
		return getEnvironment(pluginManager);
	}

	protected ExecutionEnvironment getEnvironment(MavenProject project, MavenSession session, BuildPluginManager pluginManager) {
		if (environment == null) {
			environment = executionEnvironment(project, session, pluginManager);
		}

		return environment;
	}

	protected ExecutionEnvironment getEnvironment(BuildPluginManager pluginManager) {
		return getEnvironment(project, session, pluginManager);
	}

	// execution of binaries
	protected int getTimeOut() {
		// to be overridden in child classes
		return 180; // 3 minutes
	}

	protected static OutputStream commandOutputStream = null;
	protected static boolean silentBinaryExecution = false;

	protected String displayCommandLine(CommandLine commandLine) {
		StringBuilder sb = new StringBuilder();
		for (String s : commandLine.toStrings()) {
			sb.append(s);
			sb.append(" ");
		}

		return sb.toString();
	}

	protected int executeBinary(File binary, List<String> arguments, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
		Integer result = -666;

		CommandLine cmdLine = new CommandLine(binary);

		for (String argument : arguments) {
			cmdLine.addArgument(argument);
		}
		getLog().debug("command line : " + cmdLine.toString());
		getLog().debug("working directory : " + workingDir);

		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(workingDir);

		if (getTimeOut() > 0) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog(getTimeOut() * 1000);
			executor.setWatchdog(watchdog);
		}

		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

		OutputStream stdOutAndErr;
		if (commandOutputStream != null) {
			stdOutAndErr = commandOutputStream;
		} else {
			stdOutAndErr = new ByteArrayOutputStream();
		}
		executor.setStreamHandler(new PumpStreamHandler(stdOutAndErr));

		if (!silentBinaryExecution) {
			getLog().info("$ " + displayCommandLine(cmdLine));
		}

		if (fork) {
			CommandLauncher commandLauncher = CommandLauncherFactory.createVMLauncher();
			commandLauncher.exec(cmdLine, null, workingDir);
		} else {
			try {
				if (synchronous) {
					result = executor.execute(cmdLine);
				} else {
					executor.execute(cmdLine, new DefaultExecuteResultHandler());
				}
			} catch (ExecuteException e) {
				if (result == -666) {
					result = e.getExitValue();
				}

				// TODO manage default errors
				if (silentBinaryExecution) {
					getLog().info("$ " + displayCommandLine(cmdLine));
				}
				getLog().error("\n" + stdOutAndErr.toString());
				if (!"0".equals(result.toString())) {
					getLog().info("Exit status of command was: " + result.toString());
				}
				throw new MojoExecutionException(errorMsg, e);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}

		return result;
	}

	/**
	 * <p>
	 * Default behaviour is synchronous and no fork.
	 * </p>
	 */
	protected int executeBinary(File binary, ArrayList<String> arguments, File workingDirectory, String errorMsg) throws IOException, MojoExecutionException {
		return executeBinary(binary, arguments, workingDirectory, errorMsg, false, true);
	}
	//

	// initialization of standalone POMs (ie no POM) because they are not included in a lifecycle
	private static boolean standalonePOMInitialized = false;
	public final static String mojoInitialized = "t3.initialized";

	@Parameter
	private Map<String,String> ignoredParameters;

	public Map<String,String> getIgnoredParameters() {
		return ignoredParameters;
	}

	/**
	 *
	 * @param ignoredParameters, the list to set
	 * @return old ignoredParameters list
	 */
	public Map<String,String> setIgnoredParameters(Map<String,String> ignoredParameters) {
		Map<String, String> oldIgnoredParameters = this.ignoredParameters;

		this.ignoredParameters = ignoredParameters;

		PluginManager.addIgnoredParametersInPluginManager(this.pluginManager, ignoredParameters);
		return oldIgnoredParameters;
	}

	protected <T> void initStandalonePOM() throws MojoExecutionException {
//		if (project != null && "standalone-pom".equals(project.getArtifactId()) && !standalonePOMInitialized) {
		if (!standalonePOMInitialized && !"true".equals(session.getUserProperties().get(mojoInitialized))) {
			AdvancedMavenLifecycleParticipant lifecycleParticipant = getLifecycleParticipant();
			lifecycleParticipant.setPlexus(plexus);
			lifecycleParticipant.setLogger(logger);
			lifecycleParticipant.setArtifactRepositoryFactory(artifactRepositoryFactory);
			lifecycleParticipant.setPluginManager(pluginManager);
			lifecycleParticipant.setProjectBuilder(projectBuilder);
			lifecycleParticipant.setPluginDescriptor(pluginDescriptor);
			try {
				lifecycleParticipant.afterProjectsRead(session);
			} catch (MavenExecutionException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}

			standalonePOMInitialized = true;
		}

		Injector i = Guice.createInjector(new AbstractModule() {
			@Override
			protected void configure() {
				bindListener(Matchers.any(), new ParametersListener<T>(this, session.getCurrentProject(), session, ignoredParameters)); // WARN: using getCurrentProject() ?
			}
		});

		i.injectMembers(this); // will also inject in configuredMojo
	}

	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		logger.warn("getLifecycleParticipant() is not overridden!");
		return null; // to be overridden in children classes
	}
	//

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException  {
		createOutputDirectory();
	}

	protected void skipGoal(String skipParameterName) {
		getLog().info(Messages.SKIPPING + " Set '" + skipParameterName + "' to 'false' to execute this goal");
	}

	public CommonMojo() {}

	/**
	 * <p>
	 * A minimalist copy-constructor to create a CommonMojo from another,
	 * keeping the pluginManager, project, session and settings.
	 * In fact this constructor is to create a CommonMojo to be called by
	 * another CommonMojo.
	 * </p>
	 *
	 * @param mojo
	 */
	public CommonMojo(CommonMojo mojo) {
		if (mojo == null) return;

		this.artifactRepositoryFactory = mojo.artifactRepositoryFactory;
		this.logger = mojo.logger;
//		this.setLog(new NoOpLogger());
		this.setLog(mojo.getLog());
		this.mavenResourcesFiltering = mojo.mavenResourcesFiltering;
		this.mojoExecution = mojo.mojoExecution;
		this.plexus = mojo.plexus;
		this.setPluginContext(mojo.getPluginContext());
		this.pluginDescriptor = mojo.pluginDescriptor;
		this.pluginManager = mojo.pluginManager;
		this.project = mojo.project;
		this.projectBasedir = mojo.projectBasedir;
		this.projectBuilder = mojo.projectBuilder;
		this.session = mojo.session;
		this.settings = mojo.settings;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setPluginManager(BuildPluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setSession(MavenSession session) {
		this.session = session;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	protected File copyDependency(String groupId, String artifactId, String version, String type, String classifier, File outputDirectory, String fileName) throws MojoExecutionException {
		if (outputDirectory == null || !outputDirectory.isDirectory()) return null;

		ArrayList<Element> configuration = new ArrayList<Element>();

		List<Element> options = new ArrayList<>();
		options.add(new Element("groupId", groupId));
		options.add(new Element("artifactId", artifactId));
		options.add(new Element("version", version)); 
		options.add(new Element("type", type));
		if (classifier != null) {
			options.add(new Element("classifier", classifier));
		}
		options.add(new Element("outputDirectory", outputDirectory.getAbsolutePath()));
		options.add(new Element("destFileName", fileName));
		Element artifactItem = new Element("artifactItem", options.toArray(new Element[0]));
		Element artifactItems = new Element("artifactItems", artifactItem);

		configuration.add(artifactItems);
		configuration.add(new Element("silent", "true"));

		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-dependency-plugin"),
				version("3.0.2") // version defined in pom.xml of this plugin
			),
			goal("copy"),
			configuration(
				configuration.toArray(new Element[0])
			),
			getEnvironment(),
			true
		);

		return new File(outputDirectory, fileName);
	}

	protected File copyDependency(String groupId, String artifactId, String version, String type, File outputDirectory, String fileName) throws MojoExecutionException {
		return copyDependency(groupId, artifactId, version, type, null, outputDirectory, fileName);
	}

	protected File getDependency(String groupId, String artifactId, String version, String type, String classifier, boolean silent) throws MojoExecutionException, ArtifactNotFoundException, ArtifactResolutionException {
		ArrayList<Element> configuration = new ArrayList<Element>();
		
		configuration.add(new Element("groupId", groupId));
		configuration.add(new Element("artifactId", artifactId));
		configuration.add(new Element("version", version)); 
		configuration.add(new Element("packaging", type));
		if (classifier != null) {
			configuration.add(new Element("classifier", classifier));
		}
		configuration.add(new Element("transitive", "false"));

		getLog().info("Resolving artifact '" + groupId + ":" + artifactId + ":" + version + ":" + type.toLowerCase() + (classifier != null ? ":" + classifier : "") + "'...");

		try {
			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId("maven-dependency-plugin"),
					version("3.0.2") // version defined in pom.xml of this plugin
				),
				goal("get"),
				configuration(
					configuration.toArray(new Element[0])
				),
				getEnvironment(),
				silent
			);
		} catch (MojoExecutionException e) {
			if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof ArtifactNotFoundException) {
				ArtifactNotFoundException artifactNotFoundException = (ArtifactNotFoundException) e.getCause().getCause();
				throw artifactNotFoundException;
			} else if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof ArtifactResolutionException) {
				ArtifactResolutionException artifactResolutionException = (ArtifactResolutionException) e.getCause().getCause();
				throw artifactResolutionException;
			}
		}

		File result = new File(session.getLocalRepository().getBasedir() + "/" + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + (classifier != null ? "-" + classifier : "") + "." + type.toLowerCase());

		getLog().info("Artifact '" + groupId + ":" + artifactId + ":" + version + ":" + type.toLowerCase() + (classifier != null ? ":" + classifier : "") + "' has been resolved to '" + result.getAbsolutePath() + "'");

		return result;
	}

	protected File getDependency(String groupId, String artifactId, String version, String type, boolean silent) throws MojoExecutionException, ArtifactNotFoundException, ArtifactResolutionException {
		return getDependency(groupId, artifactId, version, type, null, silent);
	}

	protected void deployDependency(String groupId, String artifactId, String version, String type, String classifier, File file, String remoteRepositoryId, String remoteRepositoryURL, boolean silent) throws MojoExecutionException {
		ArrayList<Element> configuration = new ArrayList<Element>();
		
		configuration.add(new Element("file", file.getAbsolutePath()));

		configuration.add(new Element("groupId", groupId));
		configuration.add(new Element("artifactId", artifactId));
		configuration.add(new Element("version", version)); 
		configuration.add(new Element("packaging", type));
		if (classifier != null) {
			configuration.add(new Element("classifier", classifier));
		}

		if (remoteRepositoryId != null) {
			configuration.add(new Element("repositoryId", remoteRepositoryId));
		}
		if (remoteRepositoryURL != null) {
			configuration.add(new Element("url", remoteRepositoryURL));
		}

		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-deploy-plugin"),
				version("2.8.2") // version defined in pom.xml of this plugin
			),
			goal("deploy-file"),
			configuration(
				configuration.toArray(new Element[0])
			),
			getEnvironment(),
			silent
		);
	}

	protected void installDependency(String groupId, String artifactId, String version, String type, String classifier, File file, File localRepositoryPath, boolean silent) throws MojoExecutionException {
		ArrayList<Element> configuration = new ArrayList<Element>();
		
		configuration.add(new Element("file", file.getAbsolutePath()));

		configuration.add(new Element("groupId", groupId));
		configuration.add(new Element("artifactId", artifactId));
		configuration.add(new Element("version", version)); 
		configuration.add(new Element("packaging", type));
		if (classifier != null) {
			configuration.add(new Element("classifier", classifier));
		}

		if (localRepositoryPath != null) {
			configuration.add(new Element("localRepositoryPath", localRepositoryPath.getAbsolutePath()));
		}

		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-install-plugin"),
				version("2.5.2") // version defined in pom.xml of this plugin
			),
			goal("install-file"),
			configuration(
				configuration.toArray(new Element[0])
			),
			getEnvironment(),
			silent
		);
	}

	protected List<ArtifactResult> getPomArtifact(String coords) {
        Artifact artifact = new DefaultArtifact(coords);

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency( artifact, JavaScopes.COMPILE));
        collectRequest.setRepositories(project.getRemotePluginRepositories());

        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFlter);

        List<ArtifactResult> artifactResults = null;
		try {
			artifactResults = system.resolveDependencies(systemSession, dependencyRequest).getArtifactResults();
		} catch (DependencyResolutionException e) {
			//
		}

		return artifactResults;
	}

	private void writeMavenMetadata(File localRepository, String groupIdPath, String resourcePath) {
		// create a maven-metadata-local.xml for plugin group
		InputStream metadataInputStream = this.getClass().getResourceAsStream(resourcePath);
		File groupDirectory = new File(localRepository, groupIdPath);
		groupDirectory.mkdirs();
		File metadataFile = new File(groupDirectory, "maven-metadata-local.xml");
		OutputStream metadataOutputStream = null;
		try {
			metadataOutputStream = new FileOutputStream(metadataFile);
			IOUtils.copy(metadataInputStream, metadataOutputStream);
		} catch (IOException e1) {
		} finally {
			if (metadataOutputStream != null) {
				try {
					metadataOutputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	protected void goOffline(MavenProject project, File localRepositoryPath, String mavenVersion) throws MojoExecutionException {
		localRepositoryPath.mkdirs();
		File tmpDirectory = Files.createTempDir();

        // create a settings.xml with <pluginGroups>
		InputStream globalSettingInputStream = this.getClass().getResourceAsStream("/maven/default-t3-settings.xml");
		File globalSettingsFile = new File(tmpDirectory, "settings.xml");
		OutputStream globalSettingsOutputStream = null;
		try {
			globalSettingsOutputStream = new FileOutputStream(globalSettingsFile);
			IOUtils.copy(globalSettingInputStream, globalSettingsOutputStream);
		} catch (IOException e1) {
		} finally {
			if (globalSettingsOutputStream != null) {
				try {
					globalSettingsOutputStream.close();
				} catch (IOException e) {
				}
			}
		}

        // create a maven-metadata-local.xml for Maven plugin group
		writeMavenMetadata(localRepositoryPath, "org/apache/maven/plugins", "/maven/maven-plugins-maven-metadata.xml");

		// create a maven-metadata-local.xml for tic plugin group
		writeMavenMetadata(localRepositoryPath, "io/teecube/tic", "/maven/tic-maven-metadata-local.xml");

		// create a maven-metadata-local.xml for toe plugin group
		writeMavenMetadata(localRepositoryPath, "io/teecube/toe", "/maven/toe-maven-metadata-local.xml");

		List<MavenResolvedArtifact> mavenResolvedArtifacts = new ArrayList<MavenResolvedArtifact>();

		System.setProperty(MavenSettingsBuilder.ALT_LOCAL_REPOSITORY_LOCATION, session.getLocalRepository().getBasedir().replace("\\", "/"));

		ConfigurableMavenResolverSystem mavenResolver = Maven.configureResolver();

		List<ArtifactResult> poms = new ArrayList<ArtifactResult>();
		poms.addAll(getPomArtifact("org.apache:apache:pom:4"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:6"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:9"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:10"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:11"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:13"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:17"));
		poms.addAll(getPomArtifact("org.apache:apache:pom:18"));
		poms.addAll(getPomArtifact("org.apache.ant:ant-parent:pom:1.8.1"));
		poms.addAll(getPomArtifact("org.apache.commons:commons-parent:pom:24"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:9"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:15"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:21"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:22"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:23"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:27"));
		poms.addAll(getPomArtifact("org.apache.maven:maven-parent:pom:30"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:12"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:16"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:22"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:23"));
		poms.addAll(getPomArtifact("org.apache.maven.plugins:maven-plugins:pom:24"));
		poms.addAll(getPomArtifact("org.apache.maven.archetype:maven-archetype:pom:3.0.1"));
		poms.addAll(getPomArtifact("org.apache.maven.archetype:archetype-models:pom:3.0.1"));
		poms.addAll(getPomArtifact("org.codehaus.plexus:plexus-components:pom:1.1.15"));
		poms.addAll(getPomArtifact("org.codehaus.plexus:plexus-components:pom:1.1.18"));
		poms.addAll(getPomArtifact("org.sonatype.aether:aether-parent:pom:1.7"));
		poms.addAll(getPomArtifact("asm:asm-parent:pom:3.2"));
		poms.addAll(getPomArtifact("org.slf4j:slf4j-parent:pom:1.7.5"));
		poms.addAll(getPomArtifact("org.slf4j:slf4j-parent:pom:1.7.24"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:tycho:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:sisu-equinox:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:tycho-bundles:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.eclipse.tycho:tycho-p2:pom:0.22.0"));
		poms.addAll(getPomArtifact("org.apache.maven.shared:maven-shared-components:pom:22"));
		poms.addAll(getPomArtifact("org.apache.maven.release:maven-release:pom:2.3.2"));

		// plugins from super POM
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-antrun-plugin:jar:1.3").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-assembly-plugin:jar:2.2-beta-5").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-clean-plugin:jar:2.5").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-dependency-plugin:jar:2.8").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-deploy-plugin:jar:2.7").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-install-plugin:jar:2.4").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-release-plugin:jar:2.3.2").withoutTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-site-plugin:jar:3.3").withoutTransitivity().asList(MavenResolvedArtifact.class));

		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-archetype-plugin:jar:3.0.1").withTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.apache.maven.plugins:maven-enforcer-plugin:jar:1.3.1").withTransitivity().asList(MavenResolvedArtifact.class));
		mavenResolvedArtifacts.addAll(mavenResolver.resolve("org.codehaus.plexus:plexus-component-annotations:jar:1.6").withTransitivity().asList(MavenResolvedArtifact.class));

//		org.jboss.shrinkwrap.resolver.impl.maven.logging.LogTransferListener
		// add plugins from project
		for (Plugin plugin : project.getBuild().getPlugins()) {			
			mavenResolvedArtifacts.addAll(mavenResolver.resolve(plugin.getKey() + ":jar:" + plugin.getVersion()).withTransitivity().asList(MavenResolvedArtifact.class));
		}

		// add all as artifacts
		List<Artifact> artifacts = new ArrayList<Artifact>();
		for (MavenResolvedArtifact mavenResolvedArtifact : mavenResolvedArtifacts) {
			Artifact artifact = new DefaultArtifact(mavenResolvedArtifact.getCoordinate().getGroupId() + ":" + mavenResolvedArtifact.getCoordinate().getArtifactId() + ":" + mavenResolvedArtifact.getCoordinate().getType() + ":" + mavenResolvedArtifact.getCoordinate().getVersion());
			File resolvedFile = mavenResolvedArtifact.asFile();
			if (resolvedFile.getAbsolutePath().contains("..")) continue;
			String name = resolvedFile.getName();
			String shortName = artifact.getArtifactId() + "-" + artifact.getVersion();
			int lengthWithoutExtension = name.length() - artifact.getExtension().length() - 1;
			if (lengthWithoutExtension > 0) {
				name = name.substring(0, lengthWithoutExtension);
				if (!name.equals(shortName)) {
					String classifier = name.substring(shortName.length() + 1);
					artifact = new DefaultArtifact(mavenResolvedArtifact.getCoordinate().getGroupId() + ":" + mavenResolvedArtifact.getCoordinate().getArtifactId() + ":" + mavenResolvedArtifact.getCoordinate().getType() + ":" + classifier + ":" + mavenResolvedArtifact.getCoordinate().getVersion());
				}
			}
			artifact = artifact.setFile(resolvedFile);

			artifacts.add(artifact);
		}
		for (ArtifactResult pom : poms) {
			artifacts.add(pom.getArtifact());
		}

		// install artifacts
		for (Artifact artifact : artifacts) {
			boolean installPomSeparately = false;
			List<Element> configuration = new ArrayList<Element>();

			if (artifact.getArtifactId().equals("velocity") && artifact.getVersion().equals("1.5")) {
				configuration.add(new Element("generatePom", "true"));
				configuration.add(new Element("packaging", "jar"));
				installPomSeparately = true;
			}

			configuration.add(new Element("localRepositoryPath", localRepositoryPath.getAbsolutePath()));
			configuration.add(new Element("createChecksum", "true"));
			configuration.add(new Element("updateReleaseInfo", "true"));
			configuration.add(new Element("groupId", artifact.getGroupId()));
			configuration.add(new Element("artifactId", artifact.getArtifactId()));
			configuration.add(new Element("version", artifact.getVersion()));
			configuration.add(new Element("file", artifact.getFile().getAbsolutePath()));
			File pomFile = new File(artifact.getFile().getParentFile(), artifact.getArtifactId() + "-" + artifact.getVersion() + ".pom");
			if (StringUtils.isNotEmpty(artifact.getClassifier())) {
				configuration.add(new Element("classifier", artifact.getClassifier()));				
				configuration.add(new Element("generatePom", "true"));
				configuration.add(new Element("packaging", "jar"));
				installPomSeparately = true;
			} else if (!installPomSeparately) {
				if (!pomFile.exists()) continue;
				configuration.add(new Element("pomFile", pomFile.getAbsolutePath()));
			}

			executeMojo(
				plugin(
					groupId("org.apache.maven.plugins"),
					artifactId("maven-install-plugin"),
					version("2.5.2") // version defined in pom.xml of this plugin
				),
				goal("install-file"),
				configuration(
					configuration.toArray(new Element[0])
				),
				getEnvironment(project, session, pluginManager),
				true
			);

			File artifactDirectory = new File(localRepositoryPath, artifact.getGroupId().replace(".", "/") + "/" + artifact.getArtifactId() + "/" + artifact.getVersion());
			Collection<File> bundleFiles = FileUtils.listFiles(artifactDirectory, new String[] {"bundle"}, false);
			if (!bundleFiles.isEmpty()) {
				for (File bundleFile : bundleFiles) {
					String filenameNoExt = FilenameUtils.removeExtension(bundleFile.getAbsolutePath());
					bundleFile.renameTo(new File(filenameNoExt + ".jar"));
					File md5File = new File(filenameNoExt + ".bundle.md5");
					File sha1File = new File(filenameNoExt + ".bundle.sha1");
					md5File.renameTo(new File(filenameNoExt + ".jar.md5"));
					sha1File.renameTo(new File(filenameNoExt + ".jar.sha1"));
				}
			}
			Collection<File> archetypeFiles = FileUtils.listFiles(artifactDirectory, new String[] {"maven-archetype"}, false);
			if (!archetypeFiles.isEmpty()) {
				for (File archetypeFile : archetypeFiles) {
					String filenameNoExt = FilenameUtils.removeExtension(archetypeFile.getAbsolutePath());
					archetypeFile.renameTo(new File(filenameNoExt + ".jar"));
					File md5File = new File(filenameNoExt + ".maven-archetype.md5");
					File sha1File = new File(filenameNoExt + ".maven-archetype.sha1");
					md5File.renameTo(new File(filenameNoExt + ".jar.md5"));
					sha1File.renameTo(new File(filenameNoExt + ".jar.sha1"));
				}
			}

			if (installPomSeparately) {
				configuration.clear();

				configuration.add(new Element("localRepositoryPath", localRepositoryPath.getAbsolutePath()));
				configuration.add(new Element("createChecksum", "true"));
				configuration.add(new Element("updateReleaseInfo", "true"));
				configuration.add(new Element("groupId", artifact.getGroupId()));
				configuration.add(new Element("artifactId", artifact.getArtifactId()));
				configuration.add(new Element("version", artifact.getVersion()));
				configuration.add(new Element("file", pomFile.getAbsolutePath()));
				configuration.add(new Element("packaging", "pom"));

				executeMojo(
					plugin(
						groupId("org.apache.maven.plugins"),
						artifactId("maven-install-plugin"),
						version("2.5.2") // version defined in pom.xml of this plugin
					),
					goal("install-file"),
					configuration(
						configuration.toArray(new Element[0])
					),
					executionEnvironment(project, session, pluginManager),
					true
				);
			}
		}

		List<String> goals = new ArrayList<String>();
		for (Plugin plugin : project.getBuild().getPlugins()) {
			for (PluginExecution execution : plugin.getExecutions()) {
				String prefix = execution.getId();
				for (String goal : execution.getGoals()) {
					goals.add(prefix + ":" + goal);
				}
			}
		}

		// create a default empty POM (because it's needed...)
		File tmpPom = new File(tmpDirectory, "pom.xml");
		try {
			POMManager.writeModelToPOM(project.getModel(), tmpPom);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
		List<Map.Entry<File, List<String>>> pomsWithGoal = getPOMsFromProject(project, tmpDirectory);
		
		PrintStream oldSystemErr = System.err;
		PrintStream oldSystemOut = System.out;
		try {
			silentSystemStreams();

			for (Entry<File, List<String>> pomWithGoals : pomsWithGoal) {
				executeGoal(pomWithGoals.getKey(), globalSettingsFile, localRepositoryPath, mavenVersion, pomWithGoals.getValue());
			}
		} finally {
			System.setErr(oldSystemErr);
			System.setOut(oldSystemOut);
		}
	}

	private void executeGoal(File pomWithGoal, File globalSettingsFile, File localRepositoryPath, String mavenVersion, List<String> goals) throws MojoExecutionException {
		goals.clear();

		goals.add("validate");

		// execute the goals to bootstrap the plugin in local repository path
		ConfigurationDistributionStage builder = EmbeddedMaven.forProject(pomWithGoal)
															  .setQuiet()
															  .setUserSettingsFile(globalSettingsFile)
															  .setGlobalSettingsFile(globalSettingsFile)
															  .setLocalRepositoryDirectory(localRepositoryPath)
															  .useMaven3Version(mavenVersion)
															  .setGoals(goals);
		Field requestField;
		try {
			requestField = builder.getClass().getDeclaredField("request");
			requestField.setAccessible(true);
			DefaultInvocationRequest request = (DefaultInvocationRequest) requestField.get(builder);
			request.setReactorFailureBehavior(ReactorFailureBehavior.FailAtEnd);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
		}

		BuiltProject result = builder.ignoreFailure().build();

		if (result == null) {
			throw new MojoExecutionException("Unable to execute plugins goals to go offline.");
		}
		else if (result.getMavenBuildExitCode() != 0) {
			File logOutput = new File(this.directory, "go-offline.log");
			try {
				FileUtils.writeStringToFile(logOutput, result.getMavenLog(), StandardCharsets.UTF_8);
			} catch (IOException e) {
				
			}

			if (result.getMavenLog().contains("[ERROR] " + Messages.ENFORCER_RULES_FAILURE) ||
				result.getMavenLog().contains("Nothing to merge.") ||
				result.getMavenLog().contains("Unable to load topology from file")) {
				return;
			}
			getLog().error("Something went wrong in Maven build to go offline. Log file is: '" + logOutput.getAbsolutePath() + "'");

			throw new MojoExecutionException("Unable to execute plugins goals to go offline.");
		}		
	}

	private List<Entry<File, List<String>>> getPOMsFromProject(MavenProject project, File tmpDirectory) throws MojoExecutionException {
		List<Entry<File, List<String>>> result = new ArrayList<Entry<File, List<String>>>();

		for (Plugin plugin : project.getModel().getBuild().getPlugins()) {
			Model model = project.getModel().clone();
			for (Iterator<Plugin> iterator = model.getBuild().getPlugins().iterator(); iterator.hasNext();) {
				Plugin p = iterator.next();
				if (!p.getKey().equals(plugin.getKey()) || (p.getExecutions().isEmpty())) {
					iterator.remove();
				}
			}

			if (model.getBuild().getPlugins().isEmpty()) {
				continue;
			}
			try {
				File tmpPom = File.createTempFile("pom", ".xml", tmpDirectory);
				POMManager.writeModelToPOM(model, tmpPom);
				List<String> goals = new ArrayList<String>();
				for (String goal : model.getBuild().getPlugins().get(0).getExecutions().get(0).getGoals()) {
					goals.add(model.getBuild().getPlugins().get(0).getExecutions().get(0).getId() + ":" + goal);
				}
				Entry<File, List<String>> entry = new AbstractMap.SimpleEntry<File, List<String>>(tmpPom, goals);
				result.add(entry);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}
		return result;
	}

	public void executeMojo(Plugin plugin, String goal, Xpp3Dom configuration, ExecutionEnvironment env) throws MojoExecutionException {
		executeMojo(plugin, goal, configuration, env, false);
	}

	public void executeMojo(Plugin plugin, String goal, Xpp3Dom configuration, ExecutionEnvironment env, boolean quiet) throws MojoExecutionException {
		PrintStream oldSystemErr = System.err;
		PrintStream oldSystemOut = System.out;
		Map<String, Integer> logLevels = new HashMap<String, Integer>(); // a Map to store initial values of log levels
		if (quiet) {
			try {
				silentSystemStreams();
				PluginManager.setSilentMojoInPluginManager(pluginManager, true);
				changeSlf4jLoggerLogLevel("org.codehaus.plexus.PlexusContainer", 50, logLevels);
				changeSlf4jLoggerLogLevel("org.apache.maven.cli.transfer.Slf4jMavenTransferListener", 50, logLevels);
	
				org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo(plugin, goal, configuration, env);
			} finally {
				System.setErr(oldSystemErr);
				System.setOut(oldSystemOut);
				restoreSlf4jLoggerLogLevel("org.codehaus.plexus.PlexusContainer", logLevels);
				restoreSlf4jLoggerLogLevel("org.apache.maven.cli.transfer.Slf4jMavenTransferListener", logLevels);
				PluginManager.setSilentMojoInPluginManager(pluginManager, false);
			}
		} else {			
			org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo(plugin, goal, configuration, env);
		}
	}

	private int getSlf4jLoggerLogLevel(String slf4jLoggerName) {
		org.slf4j.Logger logger = LoggerFactory.getLogger(slf4jLoggerName);
		Field currentLogLevelField;
		try {
			currentLogLevelField = logger.getClass().getDeclaredField("currentLogLevel");
			currentLogLevelField.setAccessible(true);
			return (int) currentLogLevelField.get(logger);
		} catch (Exception e) {
			// nothing
		}
		return -1;
	}

	private void restoreSlf4jLoggerLogLevel(String slf4jLoggerName, Map<String, Integer> logLevels) {
		int initialValue = logLevels.get(slf4jLoggerName); // retrieve initial value

		setSlf4jLoggerLogLevel(slf4jLoggerName, initialValue); // restore initial value

		logLevels.remove(slf4jLoggerName); // delete initial value for this logger
	}

	private void changeSlf4jLoggerLogLevel(String slf4jLoggerName, int logLevel, Map<String, Integer> logLevels) {
		logLevels.put(slf4jLoggerName, getSlf4jLoggerLogLevel(slf4jLoggerName)); // store initial value

		setSlf4jLoggerLogLevel(slf4jLoggerName, logLevel); // set new value
	}

	private void setSlf4jLoggerLogLevel(String slf4jLoggerName, int logLevel) {
		org.slf4j.Logger logger = LoggerFactory.getLogger(slf4jLoggerName);
		Field currentLogLevelField;
		try {
			currentLogLevelField = logger.getClass().getDeclaredField("currentLogLevel");
			currentLogLevelField.setAccessible(true);
			currentLogLevelField.set(logger, logLevel);
		} catch (Exception e) {
			// nothing
		}
	}

	private void silentSystemStreams() {	
		System.setErr(new PrintStream(new OutputStream(){
			public void write(int b) {}
		}));
		System.setOut(new PrintStream(new OutputStream(){
			public void write(int b) {}
		}));
	}

    /**
     * Add all files from the source directory to the destination zip file
     *
     * @param source      the directory with files to add
     * @param destination the zip file that should contain the files
     * @throws IOException      if the io fails
     * @throws ArchiveException if creating or adding to the archive fails
     */
    protected void addFilesToZip(File source, File destination) throws IOException, ArchiveException {
        OutputStream archiveStream = new FileOutputStream(destination);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);

        Collection<File> fileList = FileUtils.listFiles(source, null, true);

        for (File file : fileList) {
            String entryName = getEntryName(source, file);
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            archive.putArchiveEntry(entry);

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

            IOUtils.copy(input, archive);
            input.close();
            archive.closeArchiveEntry();
        }

        archive.finish();
        archiveStream.close();
    }

    /**
     * Remove the leading part of each entry that contains the source directory name
     *
     * @param source the directory where the file entry is found
     * @param file   the file that is about to be added
     * @return the name of an archive entry
     * @throws IOException if the io fails
     */
    private String getEntryName(File source, File file) throws IOException {
        int index = source.getAbsolutePath().length() + 1;
        String path = file.getCanonicalPath();

        return path.substring(index);
    }
}
