/**
 * (C) Copyright 2016-2018 teecube
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

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import org.apache.commons.exec.*;
import org.apache.commons.exec.launcher.CommandLauncher;
import org.apache.commons.exec.launcher.CommandLauncherFactory;
import org.apache.commons.io.IOUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.dependency.resolvers.ResolveDependenciesMojo;
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
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.ConfigurationDistributionStage;
import org.slf4j.LoggerFactory;
import org.twdata.maven.mojoexecutor.MojoExecutor.*;
import t3.plugin.PluginManager;
import t3.plugin.annotations.GlobalParameter;
import t3.plugin.annotations.injection.ParametersListener;
import t3.utils.POMManager;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * This abstract Mojo provides basic helpers methods to simplify Maven mojos creation.
 *
 * These helpers are divided in several categories:
 *
 *   I. Advanced Maven lifecycle management
 *  II. Advanced Maven properties management
 * III. System command launcher
 *  IV. Custom Mojo executor
 *   V. Advanded Maven dependencies management
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class CommonMojo extends AbstractMojo {

    @GlobalParameter(property = CommonMojoInformation.executablesExtension, required = true, description = CommonMojoInformation.executablesExtension_description, category = CommonMojoInformation.systemCategory)
    protected String executablesExtension;

    @GlobalParameter(property = CommonMojoInformation.platformArch, required = true, description = CommonMojoInformation.platformArch_description, category = CommonMojoInformation.systemCategory)
    protected String platformArch;

    @GlobalParameter(property = CommonMojoInformation.platformOs, required = true, description = CommonMojoInformation.platformOs_description, category = CommonMojoInformation.systemCategory)
    protected String platformOs;

    @GlobalParameter(property = "project.build.directory", description = CommonMojoInformation.directory_description, defaultValue = "${basedir}/target", category = CommonMojoInformation.mavenCategory)
    // target
    protected File directory;

    @GlobalParameter(property = "project.output.directory", description = CommonMojoInformation.outputDirectory_description, defaultValue = "${project.build.directory}/output", category = CommonMojoInformation.mavenCategory)
    // target/output (instead of target/classes)
    protected File outputDirectory;

    @GlobalParameter(property = "project.test.directory", description = CommonMojoInformation.testOutputDirectory_description, defaultValue = "${project.build.directory}/test", category = CommonMojoInformation.mavenCategory)
    // target/test
    protected File testOutputDirectory;

    @GlobalParameter(property = "project.build.sourceEncoding", defaultValue = "UTF-8", description = CommonMojoInformation.sourceEncoding_description, category = CommonMojoInformation.mavenCategory)
    protected String sourceEncoding;

    @Component
    protected ArtifactRepositoryFactory artifactRepositoryFactory;

    @Component
    protected Logger logger;

    @Component(role = org.apache.maven.shared.filtering.MavenResourcesFiltering.class, hint = "default")
    protected MavenResourcesFiltering mavenResourcesFiltering;

    @Parameter(defaultValue = "${mojoExecution}", readonly = true)
    protected MojoExecution mojoExecution;

    @Component
    protected PlexusContainer plexus;

    @Parameter(defaultValue = "${plugin}", readonly = true)
    protected PluginDescriptor pluginDescriptor; // plugin descriptor of this plugin

    @Component(role = BuildPluginManager.class)
    protected BuildPluginManager pluginManager;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter(defaultValue = "${basedir}", readonly = true)
    protected File projectBasedir;

    @Component
    protected ProjectBuilder projectBuilder;

    @Parameter(defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @Parameter(defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    @Component
    protected RepositorySystem system;

    @org.apache.maven.plugins.annotations.Parameter(defaultValue = "${repositorySystemSession}", readonly = true)
    protected RepositorySystemSession systemSession;

    public static final Pattern mavenPropertyPattern = Pattern.compile("\\$\\{([^}]*)\\}"); // ${prop} regex pattern

    public CommonMojo() {
    }

    /**
     * <p>
     * A copy-constructor to create a CommonMojo from another, keeping the pluginManager, project, session and settings.
     * In fact this constructor is to create a CommonMojo to be called by another CommonMojo.
     * </p>
     *
     * @param mojo
     */
    public CommonMojo(CommonMojo mojo) {
        if (mojo == null) return;

        this.artifactRepositoryFactory = mojo.artifactRepositoryFactory;
        this.setLog(mojo.getLog()); // from parent class AbstractMojo
        this.logger = mojo.logger;
        this.mavenResourcesFiltering = mojo.mavenResourcesFiltering;
        this.mojoExecution = mojo.mojoExecution;
        this.plexus = mojo.plexus;
        this.setPluginContext(mojo.getPluginContext()); // from parent class AbstractMojo
        this.pluginDescriptor = mojo.pluginDescriptor;
        this.pluginManager = mojo.pluginManager;
        this.project = mojo.project;
        this.projectBasedir = mojo.projectBasedir;
        this.projectBuilder = mojo.projectBuilder;
        this.session = mojo.session;
        this.settings = mojo.settings;
        this.system = mojo.system;
        this.systemSession = mojo.systemSession;
    }

    //<editor-fold desc="Getters and setters">
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public MavenResourcesFiltering getMavenResourcesFiltering() {
        return mavenResourcesFiltering;
    }

    public PluginDescriptor getPluginDescriptor() {
        return pluginDescriptor;
    }

    public BuildPluginManager getPluginManager() {
        return pluginManager;
    }

    public void setPluginManager(BuildPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MavenSession getSession() {
        return session;
    }

    public void setSession(MavenSession session) {
        this.session = session;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public String getSourceEncoding() {
        return sourceEncoding;
    }
    //</editor-fold>

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        createOutputDirectory();
    }

    //<editor-fold desc="Basic methods">
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

    protected void skipGoal(String skipParameterName) {
        getLog().info(Messages.SKIPPING + " Set '" + skipParameterName + "' to 'false' to execute this goal");
    }
    //</editor-fold>

    /* --------------------------- */
    /* I. Advanced Maven lifecycle */
    /* --------------------------- */
    //<editor-fold desc="Advanced Maven lifecycle">

    // initialization of standalone POMs (ie no POM) because they are not included in a lifecycle
    private static boolean standalonePOMInitialized = false;
    public final static String mojoInitialized = "t3.initialized";

    @Parameter
    private Map<String, String> ignoredParameters;

    public Map<String, String> getIgnoredParameters() {
        return ignoredParameters;
    }

    /**
     * @param ignoredParameters, the list to set
     * @return old ignoredParameters list
     */
    public Map<String, String> setIgnoredParameters(Map<String, String> ignoredParameters) throws MojoExecutionException {
        Map<String, String> oldIgnoredParameters = this.ignoredParameters;

        this.ignoredParameters = ignoredParameters;

        PluginManager.addIgnoredParametersInPluginManager(this.pluginManager, ignoredParameters);
        return oldIgnoredParameters;
    }

    protected <T> void initStandalonePOM() throws MojoExecutionException {
        if (!standalonePOMInitialized && !"true".equals(session.getUserProperties().get(mojoInitialized))) {
            AdvancedMavenLifecycleParticipant lifecycleParticipant = getLifecycleParticipant();
            lifecycleParticipant.setPlexus(plexus);
            lifecycleParticipant.setLogger(logger);
            lifecycleParticipant.setArtifactRepositoryFactory(artifactRepositoryFactory);
            lifecycleParticipant.setPluginManager(pluginManager);
            lifecycleParticipant.setProjectBuilder(projectBuilder);
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

    protected AdvancedMavenLifecycleParticipant getLifecycleParticipant() {
        logger.warn("getLifecycleParticipant() is not overridden!"); // this warning is for developers only
        return null; // to be overridden in children classes
    }
    //</editor-fold>

    /* ------------------------------- */
    /* II. Advanced Maven properties management */
    /* ------------------------------- */
    //<editor-fold desc="Maven properties management">
    private static CommonMojo propertiesManager = null;

    /**
     * <p>
     * Instantiate a minimalistic {@link CommonMojo} to use properties management in a standalone object.
     * </p>
     *
     * @param session
     * @param mavenProject
     * @return an initialized {@link CommonMojo} to use for properties management
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

    private String getPropertyValueInCommandLine(String propertyName, MavenSession session) {
        if (session == null) {
            return null;
        }

        return session.getRequest().getUserProperties().getProperty(propertyName);
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
//            result = mavenProject.getOriginalModel().getProperties().getProperty(propertyName);
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

    /**
     * Replace all properties in the ${maven.format} with their actual values based on current project and session.
     *
     * @param string
     * @return string with properties replaced
     */
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

    /**
     * Replace only a given property in the ${maven.format} with the provided value
     *
     * @param string
     * @param propertyKey
     * @param propertyValue
     * @return string with property replaced by its value
     */
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
    //</editor-fold>

    /* ---------------------------- */
    /* III. System command launcher */
    /* ---------------------------- */
    //<editor-fold desc="System command launcher">
    private static ExecutionEnvironment executionEnvironment = null;

    protected ExecutionEnvironment getEnvironment() {
        return getEnvironment(pluginManager);
    }

    protected ExecutionEnvironment getEnvironment(BuildPluginManager pluginManager) {
        return getEnvironment(project, session, pluginManager);
    }

    protected ExecutionEnvironment getEnvironment(MavenProject project, MavenSession session, BuildPluginManager pluginManager) {
        if (executionEnvironment == null) {
            executionEnvironment = executionEnvironment(project, session, pluginManager);
        }

        return executionEnvironment;
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

    public class CollectingLogOutputStream extends LogOutputStream {
        private Log logger;
        private String prefix;
        private boolean first;

        public CollectingLogOutputStream(Log log, String prefix, boolean addBlankBeforeFirstLine) {
            this.logger = log;
            this.prefix = prefix;
            this.first = addBlankBeforeFirstLine;
        }

        public CollectingLogOutputStream(Log log, String prefix) {
            this(log, prefix, true);
        }

        public CollectingLogOutputStream(Log log) {
            this(log, "");
        }

        @Override
        protected void processLine(String line, int level) {
            if (first) {
                first = false;
                logger.info("");
            }
            logger.info(prefix + line);
        }
    }

    protected int executeBinary(File binary, List<String> arguments, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
        CommandLine cmdLine = new CommandLine(binary);

        for (String argument : arguments) {
            cmdLine.addArgument(argument);
        }

        return executeBinary(cmdLine, workingDir, errorMsg, fork, synchronous);
    }

    protected int executeBinary(String commandLine, File workingDir, String errorMsg) throws IOException, MojoExecutionException {
        return executeBinary(commandLine, workingDir, errorMsg, false, true);
    }

    protected int executeBinary(String commandLine, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
        CommandLine cmdLine = CommandLine.parse(commandLine);

        return executeBinary(cmdLine, workingDir, errorMsg, fork, synchronous);
    }

    protected int executeBinary(CommandLine cmdLine, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
        Integer result = -666;

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
                if (stdOutAndErr instanceof ByteArrayOutputStream) {
                    getLog().error("\n" + stdOutAndErr.toString());
                }
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
    //</editor-fold>

    /* ------------------------ */
    /* IV. Custom Mojo executor */
    /* ------------------------ */
    //<editor-fold desc="Custom Mojo executor">
    public void executeMojo(Plugin plugin, String goal, Xpp3Dom configuration, ExecutionEnvironment executionEnvironment) throws MojoExecutionException {
        executeMojo(plugin, goal, configuration, executionEnvironment, false);
    }

    public void executeMojo(Plugin plugin, String goal, Xpp3Dom configuration, ExecutionEnvironment executionEnvironment, boolean quiet) throws MojoExecutionException {
        PrintStream oldSystemErr = System.err;
        PrintStream oldSystemOut = System.out;
        Map<String, Integer> logLevels = new HashMap<String, Integer>(); // a Map to store initial values of log levels
        if (quiet) {
            try {
                silentSystemStreams();
                PluginManager.setSilentMojoInPluginManager(pluginManager, true);
                changeSlf4jLoggerLogLevel("org.codehaus.plexus.PlexusContainer", 50, logLevels);
                changeSlf4jLoggerLogLevel("org.apache.maven.cli.transfer.Slf4jMavenTransferListener", 50, logLevels);

                org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo(plugin, goal, configuration, executionEnvironment);
            } finally {
                System.setErr(oldSystemErr);
                System.setOut(oldSystemOut);
                restoreSlf4jLoggerLogLevel("org.codehaus.plexus.PlexusContainer", logLevels);
                restoreSlf4jLoggerLogLevel("org.apache.maven.cli.transfer.Slf4jMavenTransferListener", logLevels);
                PluginManager.setSilentMojoInPluginManager(pluginManager, false);
            }
        } else {
            org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo(plugin, goal, configuration, executionEnvironment);
        }
    }

    private void changeSlf4jLoggerLogLevel(String slf4jLoggerName, int logLevel, Map<String, Integer> logLevels) {
        logLevels.put(slf4jLoggerName, getSlf4jLoggerLogLevel(slf4jLoggerName)); // store initial value

        setSlf4jLoggerLogLevel(slf4jLoggerName, logLevel); // set new value
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
        if (logLevels == null || logLevels.size() == 0) return;

        int initialValue = logLevels.get(slf4jLoggerName); // retrieve initial value

        setSlf4jLoggerLogLevel(slf4jLoggerName, initialValue); // restore initial value

        logLevels.remove(slf4jLoggerName); // delete initial value for this logger
    }

    private void setSlf4jLoggerLogLevel(String slf4jLoggerName, int logLevel) {
        org.slf4j.Logger logger = LoggerFactory.getLogger(slf4jLoggerName);
        Field currentLogLevelField;
        try {
            try {
                currentLogLevelField = logger.getClass().getDeclaredField("currentLogLevel");
            } catch (NoSuchFieldException e) { // for Maven 3.5.0+
                currentLogLevelField = logger.getClass().getSuperclass().getDeclaredField("currentLogLevel");
            }
            currentLogLevelField.setAccessible(true);
            currentLogLevelField.set(logger, logLevel);
        } catch (Exception e) {
            // nothing
        }
    }

    protected void silentSystemStreams() {
        System.setErr(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
            }
        }));
    }
    //</editor-fold>

    /* ------------------------------------------ */
    /* V. Advanded Maven dependencies management */
    /* ------------------------------------------ */
    //<editor-fold desc="Advanded Maven dependencies management">
    public static final String mavenPluginsGroupId = "org.apache.maven.plugins";
    public static final String mavenPluginDependencyArtifactId = "maven-dependency-plugin";
    public static final String mavenPluginDependencyVersion = "3.0.2";
    public static final String mavenPluginDeployArtifactId = "maven-deploy-plugin";
    public static final String mavenPluginDeployVersion = "2.8.2";
    public static final String mavenPluginInstallArtifactId = "maven-install-plugin";
    public static final String mavenPluginInstallVersion = "2.5.2";

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
                groupId(mavenPluginsGroupId),
                artifactId(mavenPluginDependencyArtifactId),
                version(mavenPluginDependencyVersion) // version defined in pom.xml of this plugin
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
                groupId(mavenPluginsGroupId),
                artifactId(mavenPluginDeployArtifactId),
                version(mavenPluginDeployVersion) // version defined in pom.xml of this plugin
            ),
            goal("deploy-file"),
            configuration(
                configuration.toArray(new Element[0])
            ),
            getEnvironment(),
            silent
        );
    }

    public File getDependency(String groupId, String artifactId, String version, String type, String classifier, boolean silent) throws ArtifactNotFoundException, ArtifactResolutionException {
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
                    groupId(mavenPluginsGroupId),
                    artifactId(mavenPluginDependencyArtifactId),
                    version(mavenPluginDependencyVersion) // version defined in pom.xml of this plugin
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

    public File getDependency(String groupId, String artifactId, String version, String type, boolean silent) throws MojoExecutionException, ArtifactNotFoundException, ArtifactResolutionException {
        return getDependency(groupId, artifactId, version, type, null, silent);
    }

    protected File installDependency(String groupId, String artifactId, String version, String type, String classifier, File file, File localRepositoryPath, boolean silent) throws MojoExecutionException {
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
                groupId(mavenPluginsGroupId),
                artifactId(mavenPluginInstallArtifactId),
                version(mavenPluginInstallVersion) // version defined in pom.xml of this plugin
            ),
            goal("install-file"),
            configuration(
                configuration.toArray(new Element[0])
            ),
            getEnvironment(),
            silent
        );

        return new File(localRepositoryPath.getAbsolutePath().replace("\\", "/") + "/" + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + (classifier != null ? "-" + classifier : "") + "." + type.toLowerCase());
    }

        //<editor-fold desc="Generic dependencies resolver">
    /* Generic dependecies resolver */
    protected List<org.apache.maven.model.Dependency> resolvedDependencies;

    protected class ResolveDependenciesWithProjectMojo extends ResolveDependenciesMojo {

        public ResolveDependenciesWithProjectMojo(MavenProject project) throws MojoExecutionException {
            super();

            // inject Maven project
            Field projectField = null;
            try {
                projectField = this.getClass().getSuperclass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("project");
                projectField.setAccessible(true);
                projectField.set(this, project);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }

            // make the goal silent
            this.setSilent(true);
        }

    }

    protected List<org.apache.maven.model.Dependency> getDependencies(Predicate<org.apache.maven.model.Dependency> dependencyPredicate) throws MojoExecutionException {
        if (resolvedDependencies != null) {
            if (resolvedDependencies.isEmpty()) {
                return resolvedDependencies;
            } else {
                return Lists.newArrayList(Collections2.filter(resolvedDependencies, dependencyPredicate));
            }
        }

        resolvedDependencies = new ArrayList<org.apache.maven.model.Dependency>();
        ResolveDependenciesWithProjectMojo rdm = new ResolveDependenciesWithProjectMojo(project);

        try {
            rdm.execute();
            Set<org.apache.maven.artifact.Artifact> dependencies = rdm.getResults().getResolvedDependencies();

            for (org.apache.maven.artifact.Artifact artifact : dependencies) {
                org.apache.maven.model.Dependency d = new org.apache.maven.model.Dependency();
                d.setGroupId(artifact.getGroupId());
                d.setArtifactId(artifact.getArtifactId());
                d.setVersion(artifact.getVersion());
                d.setType(artifact.getType());
                d.setClassifier(artifact.getClassifier());
                resolvedDependencies.add(d);
            }
        } catch (MojoFailureException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        if (resolvedDependencies.isEmpty()) {
            return resolvedDependencies;
        } else {
            return Lists.newArrayList(Collections2.filter(resolvedDependencies, dependencyPredicate));
        }
    }
        //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Maven offline mode generator">
    protected List<ArtifactResult> getPomArtifact(String coords) {
        Artifact artifact = new DefaultArtifact(coords);

        DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, JavaScopes.COMPILE));
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

    protected void copyResourceToFile(String resourcePath, File outputFile) {
        InputStream inputStream = this.getClass().getResourceAsStream(resourcePath); // retrieve resource from path
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e1) {
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    protected void writeMavenMetadata(File localRepository, String groupIdPath, String fileName, String resourcePath) {
        // create a maven-metadata-local.xml for plugin group
        File groupDirectory = new File(localRepository, groupIdPath);
        groupDirectory.mkdirs();
        File metadataFile = new File(groupDirectory, fileName);

        copyResourceToFile(resourcePath, metadataFile);
    }

    protected void writeLocalMavenMetadata(File localRepository, String groupIdPath, String resourcePath) {
        writeMavenMetadata(localRepository, groupIdPath, "maven-metadata-local.xml", resourcePath);
    }

    public BuiltProject executeGoal(File pomWithGoal, File globalSettingsFile, File userSettingsFile, File localRepositoryPath, String mavenVersion) throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();

        goals.add("validate");

        return executeGoal(pomWithGoal, goals, globalSettingsFile, userSettingsFile, localRepositoryPath, mavenVersion);
    }

    public BuiltProject executeGoal(List<String> goals, File globalSettingsFile, File userSettingsFile, File localRepositoryPath, String mavenVersion) throws MojoExecutionException {
        // create a minimalist POM (required)
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("maven-task");
        model.setArtifactId("maven-task");
        model.setVersion("1");
        model.setPackaging("pom");

        File pomFile = null;
        try {
            pomFile = File.createTempFile("pom", ".xml");
            POMManager.writeModelToPOM(model, pomFile);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        return executeGoal(pomFile, goals, globalSettingsFile, userSettingsFile, localRepositoryPath, mavenVersion);
    }

    protected BuiltProject executeGoal(File pomFile, List<String> goals, File globalSettingsFile, File userSettingsFile, File localRepositoryPath, String mavenVersion) throws MojoExecutionException {
        BuiltProject result = null;

        PrintStream oldSystemErr = System.err;
        PrintStream oldSystemOut = System.out;
        try {
            silentSystemStreams();

            // execute the goals to bootstrap the plugin in local repository path
            ConfigurationDistributionStage builder = EmbeddedMaven.forProject(pomFile)
                    .setQuiet()
                    .setUserSettingsFile(globalSettingsFile)
                    .setGlobalSettingsFile(globalSettingsFile)
                    .setUserSettingsFile(userSettingsFile)
                    .setLocalRepositoryDirectory(localRepositoryPath)
                    .useMaven3Version(mavenVersion)
                    .setGoals(goals);

            enableFailAtEnd(builder);

            result = builder.ignoreFailure().build();
        } finally {
            System.setErr(oldSystemErr);
            System.setOut(oldSystemOut);
        }

        return result;
    }

    private DefaultInvocationRequest getRequestFromBuilder(ConfigurationDistributionStage builder) {
        Field requestField;
        try {
            requestField = builder.getClass().getDeclaredField("request");
            requestField.setAccessible(true);
            DefaultInvocationRequest request = (DefaultInvocationRequest) requestField.get(builder);
            return request;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
        }

        return null;
    }

    private void enableFailAtEnd(ConfigurationDistributionStage builder) {
        getRequestFromBuilder(builder).setReactorFailureBehavior(ReactorFailureBehavior.FailAtEnd);
    }
    //</editor-fold>

}