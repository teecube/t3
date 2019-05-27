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
package t3.plugin;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import t3.CommonMojo;
import t3.CommonMojoInformation;
import t3.Messages;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class PropertiesEnforcer {
    // Arch 
    public static String ARCH_PPC = "ppc";
    public static String ARCH_X86 = "x86";
    public static String ARCH_X86_64 = "amd64";

    // OS
    public static String OS_WINDOWS = "windows";
    public static String OS_OSX = "osx";
    public static String OS_AIX = "aix";
    public static String OS_SOLARIS = "solaris";
    public static String OS_LINUX = "linux";

    public static void setPlatformSpecificProperties(MavenSession session) {
        // CommonMojoInformation.executablesExtension
        String executablesExtension = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            executablesExtension = ".exe";
        }

        // CommonMojoInformation.platformArch
        String osArch;
        Map<String, String> archMap = new HashMap<String, String>();
        archMap.put("x86", ARCH_X86);
        archMap.put("i386", ARCH_X86);
        archMap.put("i486", ARCH_X86);
        archMap.put("i586", ARCH_X86);
        archMap.put("i686", ARCH_X86);
        archMap.put("x86_64", ARCH_X86_64);
        archMap.put("amd64", ARCH_X86_64);
        archMap.put("powerpc", ARCH_PPC);
        osArch = archMap.get(SystemUtils.OS_ARCH);
        if (osArch == null) {
            throw new IllegalArgumentException("Unknown architecture " + SystemUtils.OS_ARCH);
        }

        // CommonMojoInformation.platformOs
        String osName;
        if (SystemUtils.IS_OS_WINDOWS) {
            osName = OS_WINDOWS;
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            osName = OS_OSX;
        } else if (SystemUtils.IS_OS_AIX) {
            osName = OS_AIX;
        } else if (SystemUtils.IS_OS_SOLARIS) {
            osName = OS_SOLARIS;
        } else if (SystemUtils.IS_OS_LINUX) {
            osName = OS_LINUX;
        } else {
            throw new IllegalArgumentException("Unknown operating system " + SystemUtils.OS_NAME);
        }

        for (MavenProject mavenProject : session.getProjects()) {
            if (!mavenProject.getProperties().contains(CommonMojoInformation.executablesExtension)) {
                mavenProject.getProperties().put(CommonMojoInformation.executablesExtension, executablesExtension);
            }
            if (!mavenProject.getProperties().contains(CommonMojoInformation.platformArch)) {
                mavenProject.getProperties().put(CommonMojoInformation.platformArch, osArch);
            }
            if (!mavenProject.getProperties().contains(CommonMojoInformation.platformOs)) {
                mavenProject.getProperties().put(CommonMojoInformation.platformOs, osName);
            }
        }
    }

    public static void setCustomProperty(MavenSession session, String property, List<String> lines) {
        for (MavenProject mavenProject : session.getProjects()) {
            if (!mavenProject.getProperties().contains(property)) {
                mavenProject.getProperties().put(property, StringUtils.join(lines.toArray(), "\n"));
            }
        }
    }

    public static void setCustomProperty(MavenSession session, String property, String value) {
        for (MavenProject mavenProject : session.getProjects()) {
            if (!mavenProject.getProperties().contains(property)) {
                mavenProject.getProperties().put(property, value);
            }
        }
    }

    /**
     * <p>
     *     The plugin will enforce custom rules before the actual build begins.
     * </p>
     *
     * @param session
     * @param pluginManager
     * @param logger
     * @param fromClass 
     * @throws MavenExecutionException
     */
    public static <T> void enforceProperties(MavenSession session, BuildPluginManager pluginManager, Logger logger, List<String> projectPackagings, Class<T> fromClass, String pluginKey) throws MavenExecutionException {
        logger.info(Messages.MESSAGE_SPACE);
        logger.info(Messages.ENFORCING_RULES);

        setPlatformSpecificProperties(session);

        logger.info(Messages.ENFORCING_GLOBAL_RULES);
        enforceGlobalProperties(session, pluginManager, logger, fromClass, pluginKey);
        logger.info(Messages.ENFORCED_GLOBAL_RULES);
        logger.info(Messages.MESSAGE_SPACE);

        boolean projectRules = false;

        for (MavenProject mavenProject : session.getProjects()) {
            if (projectPackagings.contains(mavenProject.getPackaging())) {
                if (!projectRules) {
                    projectRules = true;
                }
                logger.info(Messages.ENFORCING_PER_PROJECT_RULES);

                // enforce
                enforceProjectProperties(session, pluginManager, mavenProject, logger, fromClass);
            }
        }
        if (projectRules) {
            logger.info(Messages.ENFORCED_PER_PROJECT_RULES);
            logger.info(Messages.MESSAGE_SPACE);
        }
    }

    private static <T> void enforceGlobalProperties(MavenSession session, BuildPluginManager pluginManager, Logger logger, Class<T> fromClass, String pluginKey) throws MavenExecutionException {
        EnforcerPluginBuilder pluginBuilder = new EnforcerPluginBuilder();

        try {
            List<File> pluginsConfiguration = PluginConfigurator.getPluginsConfigurationFromClasspath(session, logger, fromClass, pluginKey);

            if (pluginBuilder.addConfigurationFromClasspath()) {
                for (File file : pluginsConfiguration) {
                    if (file.getAbsolutePath().endsWith("maven-enforcer-plugin.xml")) {
                        pluginBuilder.addConfigurationFromClasspath(file.getPath());
                    }
                }

                Plugin enforcerPlugin = pluginBuilder.getPlugin();
                Xpp3Dom configuration = (Xpp3Dom) enforcerPlugin.getConfiguration();

                checkForSkippedRules(session, configuration);

                if (configuration != null) {
                    executeMojo(
                        enforcerPlugin,
                        "enforce",
                        configuration,
                        executionEnvironment(session.getTopLevelProject(), session, pluginManager)
                    );
                }
            }
        } catch (MojoExecutionException e) {
            enforceFailure(e, logger);
        }
    }

    private static void checkForSkippedRules(MavenSession session, Xpp3Dom configuration) {
        CommonMojo propertiesManager = PluginConfigurator.propertiesManager;
        if (propertiesManager == null) {
            propertiesManager = CommonMojo.propertiesManager(session, session.getCurrentProject());
        }

        Integer i = 0;
        Xpp3Dom rules = configuration.getChildren("rules")[0];
        for (Xpp3Dom rule : rules.getChildren()) {
            if (rule.getChild("skip") != null) {
                String value = rule.getChild("skip").getValue();
                Integer j = 0;
                for (Xpp3Dom c : rule.getChildren()) {
                    if ("skip".equals(c.getName())) {
                        rule.removeChild(j);
                    }
                    j++;
                }
                if (value != null) {
                    value = propertiesManager.replaceProperties(value);
                    if ("true".equals(value)) { // TODO : parse boolean value
                        rules.removeChild(i);
                        i--;
                    }
                }
            }
            i++;
        }
    }

    private static <T> void enforceProjectProperties(MavenSession session, BuildPluginManager pluginManager, MavenProject mavenProject, Logger logger, Class<T> fromClass) throws MavenExecutionException {
        EnforcerPluginBuilder pluginBuilder = new EnforcerPluginBuilder();

        try {
            Xpp3Dom configuration = pluginBuilder.addConfigurationFromClasspathForProject(session, mavenProject, fromClass);
            if (configuration != null) {
                Plugin enforcerPlugin = pluginBuilder.getPlugin();

                MavenProject oldCurrentProject = session.getCurrentProject();
                try {
                    session.setCurrentProject(mavenProject);
                    executeMojo(
                        enforcerPlugin,
                        "enforce",
                        configuration,
                        executionEnvironment(mavenProject, session, pluginManager)
                    );
                } finally {
                    session.setCurrentProject(oldCurrentProject);
                }
            }
        } catch (MojoExecutionException e) {
            logger.error(Messages.MESSAGE_SPACE);
            logger.error("Project '" + mavenProject.getGroupId() + ":" + mavenProject.getArtifactId() + "' failed.");
            enforceFailure(e, logger);
        }
    }

    private static String formatMessage(String message) {
        if (message == null) return null;

        if (message.contains("Some required files are missing:")) {
            String[] lines = message.split("\n");
            for (int i=0; i < lines.length; i++){
                lines[i] += "\n";
                if (lines[i].contains("Some required files are missing:")) {
                    for (int j = i; j < lines.length; j++) {
                        lines[j] = "";
                    }
                    break;
                }
            }
            message = StringUtils.join(lines);
        }
        if (message.contains("Some files should not exist:")) {
            String[] lines = message.split("\n");
            for (int i=0; i < lines.length; i++){
                lines[i] += "\n";
                if (lines[i].contains("Some files should not exist:")) {
                    for (int j = i; j < lines.length; j++) {
                        lines[j] = "";
                    }
                    break;
                }
            }
            message = StringUtils.join(lines);
        }

        return message;
    }

    private static void enforceFailure(Exception e, Logger logger) throws MavenExecutionException {
        logger.fatalError(Messages.MESSAGE_SPACE);
        logger.fatalError(Messages.ENFORCER_RULES_FAILURE);
        logger.fatalError(Messages.MESSAGE_SPACE);
        String message = "";
        Throwable cause = e.getCause();
        if (cause == null) {
            cause = e;
        }
        if (cause != null && cause.getLocalizedMessage() != null) {
            message = cause.getLocalizedMessage();
            message = formatMessage(message);
            //        if (message != null) {
            //            message = message.substring(message.indexOf("\n")+1);
            //            message = "\n" + message;
            //        }
        }
        throw new MavenExecutionException(message, new MojoExecutionException(message));
    }
}
