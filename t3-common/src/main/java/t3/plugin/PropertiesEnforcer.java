/**
 * (C) Copyright 2014-2015 teecube
 * (http://teecube.org) and others.
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

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import t3.Messages;

/**
*
* @author Mathieu Debove &lt;mad@teecube.org&gt;
*
*/
public class PropertiesEnforcer {
	private static void setExecutablesExtension(MavenSession session) {
		String executablesExtension = "";
		if (SystemUtils.IS_OS_WINDOWS) {
			executablesExtension = ".exe";
		}

		for (MavenProject mavenProject : session.getProjects()) {
			if (!mavenProject.getProperties().contains("executables.extension")) {
				mavenProject.getProperties().put("executables.extension", executablesExtension);
			}
		}
	}

	/**
	 * <p>
	 * 	The plugin will enforce custom rules before the actual build begins.
	 * </p>
	 *
	 * @param session
	 * @param pluginManager
	 * @param logger
	 * @param fromClass 
	 * @param pluginDescriptor 
	 * @throws MavenExecutionException
	 */
	public static <T> void enforceProperties(MavenSession session, BuildPluginManager pluginManager, Logger logger, List<String> projectPackagings, Class<T> fromClass) throws MavenExecutionException {
		logger.info(Messages.MESSAGE_SPACE);
		logger.info(Messages.ENFORCING_RULES);

		setExecutablesExtension(session);

//		PluginBuilder pluginBuilder = new PluginBuilder(ENFORCER_GROUPID, ENFORCER_ARTIFACTID);
		EnforcerPluginBuilder pluginBuilder = new EnforcerPluginBuilder();
		enforceGlobalProperties(session, pluginManager, logger, pluginBuilder);

		for (MavenProject mavenProject : session.getProjects()) {
			if (projectPackagings.contains(mavenProject.getPackaging())) {
				// enforce
//				pluginBuilder = new PluginBuilder(ENFORCER_GROUPID, ENFORCER_ARTIFACTID); // reset
				pluginBuilder = new EnforcerPluginBuilder(); // reset
				enforceProjectProperties(session, pluginManager, mavenProject, logger, pluginBuilder, fromClass);
			}
		}

		logger.info(Messages.ENFORCED_RULES);
		logger.info(Messages.MESSAGE_SPACE);
		
	}
	
	private static void enforceGlobalProperties(MavenSession session, BuildPluginManager pluginManager, Logger logger, EnforcerPluginBuilder pluginBuilder) throws MavenExecutionException {
		try {
			if (pluginBuilder.addConfigurationFromClasspath()) {
				Plugin enforcerPlugin = pluginBuilder.getPlugin();
				Xpp3Dom configuration = (Xpp3Dom) enforcerPlugin.getConfiguration();

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
			logger.fatalError(Messages.ENFORCER_RULES_FAILURE);
			logger.fatalError(Messages.MESSAGE_SPACE);
			String message = e.getCause().getLocalizedMessage();
			if (message != null) {
				message = message.substring(message.indexOf("\n")+1);
			}
			throw new MavenExecutionException(message, new MojoExecutionException(message, new Throwable(message)));
		}
	}

	private static <T> void enforceProjectProperties(MavenSession session, BuildPluginManager pluginManager, MavenProject mavenProject, Logger logger, EnforcerPluginBuilder pluginBuilder, Class<T> fromClass) throws MavenExecutionException {
		try {
			if (pluginBuilder.addConfigurationFromClasspathForPackaging(session, mavenProject, fromClass)) {
				Plugin enforcerPlugin = pluginBuilder.getPlugin();
				Xpp3Dom configuration = (Xpp3Dom) enforcerPlugin.getConfiguration();

				if (configuration != null) {
					executeMojo(
						enforcerPlugin,
						"enforce",
						configuration,
						executionEnvironment(mavenProject, session, pluginManager)
					);
				}
			}
		} catch (MojoExecutionException e) {
			logger.fatalError(Messages.ENFORCER_RULES_FAILURE);
			logger.fatalError(Messages.MESSAGE_SPACE);
			String message = e.getCause().getLocalizedMessage();
			if (message != null) {
				message = message.substring(message.indexOf("\n")+1);
			}
			throw new MavenExecutionException(message, new MojoExecutionException(message, new Throwable(message)));
		}
	}

}
