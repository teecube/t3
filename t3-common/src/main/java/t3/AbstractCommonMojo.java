/**
 * (C) Copyright 2014-2015 teecube
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

import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.exec.launcher.CommandLauncher;
import org.apache.commons.exec.launcher.CommandLauncherFactory;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
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
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.Logger;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

import t3.plugin.annotations.GlobalParameter;
import t3.plugin.annotations.injection.ParametersListener;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class AbstractCommonMojo extends AbstractMojo {

	@GlobalParameter (property = "executables.extension", required = true, category = CommonMojoInformation.systemCategory)
	protected String executablesExtension;

	@GlobalParameter (property = "project.build.directory", defaultValue = "${basedir}/target", category = CommonMojoInformation.mavenCategory) // target
	protected File directory;

	@GlobalParameter (property = "project.output.directory", defaultValue = "${project.build.directory}/output", category = CommonMojoInformation.mavenCategory) // target/output (instead of target/classes)
	protected File outputDirectory;

	@GlobalParameter (property = "project.test.directory", defaultValue = "${project.build.directory}/test", category = CommonMojoInformation.mavenCategory) // target/test
	protected File testOutputDirectory;

	@GlobalParameter (property="project.build.sourceEncoding", defaultValue = "UTF-8", category = CommonMojoInformation.mavenCategory)
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
	private Settings settings;

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

	private static AbstractCommonMojo propertiesManager = null;
	/**
	 * <p>
	 * Instantiate a minimalistic {@link AbstractCommonMojo} to use properties
	 * management as a standalone object.
	 * </p>
	 *
	 * @param session
	 * @param mavenProject
	 * @return
	 */
	public static AbstractCommonMojo propertiesManager(MavenSession session, MavenProject mavenProject) {
		if (propertiesManager != null) {
			return propertiesManager;
		}
		propertiesManager = new AbstractCommonMojo();
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
			result = getPropertyValueInCommandLine(propertyName, session);
		}
		if (lookInSettingsProperties && (result == null || result.isEmpty())) {
			result = getPropertyValueInSettings(propertyName, settings);
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

	protected ExecutionEnvironment getEnvironment(BuildPluginManager pluginManager) {
		if (environment == null) {
			environment = executionEnvironment(project, session, pluginManager);
		}

		return environment;
	}

	// execution of binaries
	protected int getTimeOut() {
		// to be overridden in child classes
		return 180; // 3 minutes
	}

	protected int executeBinary(File binary, ArrayList<String> arguments, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
		Integer result = 0;

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

		ByteArrayOutputStream stdOutAndErr = new ByteArrayOutputStream();
		executor.setStreamHandler(new PumpStreamHandler(stdOutAndErr));

		getLog().info(Messages.MESSAGE_EMPTY_PREFIX + cmdLine.toString());

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
				// TODO manage default errors
				getLog().info(cmdLine.toString());
				getLog().info(stdOutAndErr.toString());
				if (!"0".equals(result.toString())) {
					getLog().info("Result code was: " + result.toString());
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

	protected <T> void initStandalonePOM() throws MojoExecutionException {
		if (project != null && "standalone-pom".equals(project.getArtifactId()) && !standalonePOMInitialized) {
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

			Injector i = Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bindListener(Matchers.any(), new ParametersListener<T>(this, session.getCurrentProject(), session)); // WARN: using getCurrentProject() ?
				}
			});

			i.injectMembers(this); // will also inject in configuredMojo

			standalonePOMInitialized = true;
		}
	}

	protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() throws MojoExecutionException {
		return null; // to be overridden in children classes
	}
	//

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException  {
		createOutputDirectory();
	}

	public void setSession(MavenSession session) {
		this.session = session;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

}
