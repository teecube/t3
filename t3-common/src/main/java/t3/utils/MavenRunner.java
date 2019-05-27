/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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
package t3.utils;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.lifecycle.NoGoalSpecifiedException;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.invoker.*;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.BuiltProject;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.EmbeddedMaven;
import org.jboss.shrinkwrap.resolver.api.maven.embedded.pom.equipped.ConfigurationDistributionStage;
import t3.log.LoggerPrintStream;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static t3.CommonMojo.silentSystemStreams;

public class MavenRunner {

    public File getPomFile() {
        return pomFile;
    }

    public void setPomFile(File pomFile) {
        this.pomFile = pomFile;
    }

    public Boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(Boolean quiet) {
        this.quiet = quiet;
    }

    public Boolean isQuietForErrors() {
        return quietErrors;
    }

    public void setQuietForErrors(Boolean quietErrors) {
        this.quietErrors = quietErrors;
    }

    public Boolean getDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public InvokerLogger getInvokerLogger() {
        return invokerLogger;
    }

    public void setInvokerLogger(InvokerLogger invokerLogger) {
        this.invokerLogger = invokerLogger;
    }

    public File getUserSettingsFile() {
        return userSettingsFile;
    }

    public void setUserSettingsFile(File userSettingsFile) {
        this.userSettingsFile = userSettingsFile;
    }

    public File getGlobalSettingsFile() {
        return globalSettingsFile;
    }

    public void setGlobalSettingsFile(File globalSettingsFile) {
        this.globalSettingsFile = globalSettingsFile;
    }

    public File getLocalRepositoryDirectory() {
        return localRepositoryDirectory;
    }

    public void setLocalRepositoryDirectory(File localRepositoryDirectory) {
        this.localRepositoryDirectory = localRepositoryDirectory;
    }

    public List<String> getGoals() {
        return goals;
    }

    public void setGoals(String... goals) {
        setGoals(Arrays.asList(goals));
    }

    public void setGoals(List<String> goals) {
        this.goals = goals;
    }

    public List<String> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<String> profiles) {
        this.profiles = profiles;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getMavenVersion() {
        return mavenVersion;
    }

    public void setMavenVersion(String mavenVersion) {
        this.mavenVersion = mavenVersion;
    }

    public Boolean getFailAtEnd() {
        return failAtEnd;
    }

    public void setFailAtEnd(Boolean failAtEnd) {
        this.failAtEnd = failAtEnd;
    }

    public Boolean getIgnoreFailure() {
        return ignoreFailure;
    }

    public void setIgnoreFailure(Boolean ignoreFailure) {
        this.ignoreFailure = ignoreFailure;
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        setLog(log, InvokerLogger.INFO);
    }

    public void setLog(Log log, int thresold) {
        this.log = log;

        PrintStream outPrintStream = new LoggerPrintStream(getLog());
        PrintStreamLogger printStreamLogger = new PrintStreamLogger(outPrintStream, thresold);
        this.setInvokerLogger(printStreamLogger);
    }

    public String getDefaultGroupId() {
        return defaultGroupId;
    }

    public void setDefaultGroupId(String defaultGroupId) {
        this.defaultGroupId = defaultGroupId;
    }

    public String getDefaultArtifactId() {
        return defaultArtifactId;
    }

    public void setDefaultArtifactId(String defaultArtifactId) {
        this.defaultArtifactId = defaultArtifactId;
    }

    public String getDefaultVersion() {
        return defaultVersion;
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public String getDefaultProjectName() {
        return defaultProjectName;
    }

    public void setDefaultProjectName(String defaultProjectName) {
        this.defaultProjectName = defaultProjectName;
    }

    private File pomFile;
    private Boolean quiet;
    private Boolean quietErrors;
    private Boolean debug;
    private InvokerLogger invokerLogger;
    private File userSettingsFile;
    private File globalSettingsFile;
    private File localRepositoryDirectory;
    private List<String> goals;
    private List<String> profiles;
    private Properties properties;
    private String mavenVersion;
    private Boolean failAtEnd;
    private Boolean ignoreFailure;
    private Log log;
    private String defaultGroupId;
    private String defaultArtifactId;
    private String defaultVersion;
    private String defaultProjectName;

    public MavenRunner() {
        failAtEnd = false;
        goals = new ArrayList<String>();
        ignoreFailure = false;
        profiles = new ArrayList<String>();
        properties = new Properties();
        quiet = false;
        quietErrors = false;
        debug = false;
    }

    private File getDefaultPomFile(String groupId, String artifactId, String version, String projectName) {
        // create a minimalist POM
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId(groupId);
        model.setArtifactId(artifactId);
        model.setVersion(version);
        model.setPackaging("pom");
        if (projectName != null) {
            model.setName(projectName);
        }

        File pomFile = null;
        try {
            pomFile = File.createTempFile("pom", ".xml");
            POMManager.writeModelToPOM(model, pomFile);
        } catch (IOException e) {
            return null;
        }

        return pomFile;
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

    private DefaultInvoker getInvokerFromBuilder(ConfigurationDistributionStage builder) {
        Field invokerField;
        try {
            invokerField = builder.getClass().getDeclaredField("invoker");
            invokerField.setAccessible(true);
            DefaultInvoker invoker = (DefaultInvoker) invokerField.get(builder);
            return invoker;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
        }

        return null;
    }

    private void setLogBufferInBuilder(ConfigurationDistributionStage builder) {
        Field logBufferInBuilder;
        try {
            logBufferInBuilder = builder.getClass().getDeclaredField("logBuffer");
            logBufferInBuilder.setAccessible(true);
            logBufferInBuilder.set(builder, null);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
        }
    }

    private void enableFailAtEnd(ConfigurationDistributionStage builder) {
        getRequestFromBuilder(builder).setReactorFailureBehavior(InvocationRequest.ReactorFailureBehavior.FailAtEnd);
    }

    public ConfigurationDistributionStage getBuilder() {
        if (pomFile == null || !pomFile.exists()) {
            pomFile = getDefaultPomFile(StringUtils.isNotEmpty(defaultGroupId) ? defaultGroupId : "maven-task",
                                        StringUtils.isNotEmpty(defaultArtifactId) ? defaultArtifactId : "maven-task",
                                        StringUtils.isNotEmpty(defaultVersion) ? defaultVersion : "1",
                                        StringUtils.isNotEmpty(defaultProjectName) ? defaultProjectName : null);
        }

        ConfigurationDistributionStage builder = EmbeddedMaven.forProject(pomFile);
        if (quiet) {
            builder = builder.setQuiet(quiet);
            PrintStream silentPrintStream = new PrintStream(new OutputStream() {
                public void write(int b) {
                }
            });
            builder = builder.setLogger(new PrintStreamLogger(silentPrintStream, InvokerLogger.DEBUG));
        } else {
            if (log != null) {
                DefaultInvoker invoker = getInvokerFromBuilder(builder);
                InvocationOutputHandler outputHandler = new PrintStreamHandler(new LoggerPrintStream(log), true);
                invoker.setOutputHandler(outputHandler);
            }
            setLogBufferInBuilder(builder);
            if (invokerLogger != null) {
                builder.setLogger(invokerLogger);
            }
        }

        builder.setInputStream(new NullInputStream(0));

        if (userSettingsFile != null && userSettingsFile.exists()) {
            builder = builder.setUserSettingsFile(userSettingsFile);
        }
        if (globalSettingsFile != null && globalSettingsFile.exists()) {
            builder = builder.setGlobalSettingsFile(globalSettingsFile);
        }
        if (localRepositoryDirectory != null && localRepositoryDirectory.exists()) {
            builder = builder.setLocalRepositoryDirectory(localRepositoryDirectory);
        }

        builder = builder.setDebug(debug);
        builder = builder.setGoals(goals);
        builder = builder.setProfiles(profiles);
        builder = builder.setProperties(properties);

        if (StringUtils.isNotEmpty(mavenVersion)) {
            PrintStream oldSystemErr = System.err;
            PrintStream oldSystemOut = System.out;

            try {
                silentSystemStreams();
                builder = (ConfigurationDistributionStage) builder.useMaven3Version(mavenVersion);
            } finally {
                System.setErr(oldSystemErr);
                System.setOut(oldSystemOut);
            }
        }

        if (failAtEnd) {
            enableFailAtEnd(builder);
        }
        if (ignoreFailure) {
            builder = (ConfigurationDistributionStage) builder.ignoreFailure();
        }

        return builder;
    }

    public BuiltProject run() throws MojoExecutionException {
        if (goals.size() <= 0) {
            throw new MojoExecutionException("No Maven goal was specified.", new NoGoalSpecifiedException("No Maven goal was specified."));
        }

        ConfigurationDistributionStage builder = getBuilder();

        BuiltProject result;

        PrintStream oldSystemErr = System.err;
        PrintStream oldSystemOut = System.out;
        try {
            if (quiet) {
                silentSystemStreams();
            } else {
                // ignore lines starting with ===
                System.setOut(new PrintStream(System.out) {
                    @Override
                    public void println(String line) {
                        if (!line.startsWith("===")) {
                            super.println(line);
                        }
                    }
                });

                if (quietErrors) {
                    System.setErr(new PrintStream(new OutputStream() {
                        public void write(int b) {
                        }
                    }));
                }
            }
            result = builder.build();
        } finally {
            System.setErr(oldSystemErr);
            System.setOut(oldSystemOut);
        }

        return result;
    }
}
