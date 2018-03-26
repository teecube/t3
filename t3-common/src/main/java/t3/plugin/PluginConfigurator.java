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
package t3.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import t3.CommonMojo;
import t3.plugin.annotations.GlobalParameter;
import t3.plugin.annotations.Parameter;
import t3.plugin.annotations.helpers.AnnotationsHelper;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class PluginConfigurator {

	public static final String projectActualBasedir = "project.actual.basedir";

	public static CommonMojo propertiesManager;

	private static boolean hasGoal(MavenSession session, String file, Class<?> fromClass, String pluginKey) {
		if (!file.startsWith("plugins-configuration/goals/")) return false;

		for (String goal : session.getRequest().getGoals()) {
			if (!goal.contains(":")) { // the goal is a phase (like "clean" "package" "install"...)
				goal = goal.trim().toUpperCase();
				LifecyclePhase lifecyclePhase = LifecyclePhase.valueOf(goal);
				if (lifecyclePhase.ordinal() >= LifecyclePhase.PRE_CLEAN.ordinal()) { // ignore phase which are not from default lifecycle
					continue;
				}
//				List<String> goals = MojosFactory.getMojosGoalsForLifecyclePhase(fromClass, lifecyclePhase, true);
				List<String> goalsToBeCalled = getGoalsToBeCalled(session, pluginKey, lifecyclePhase);
				for (String goalToBeCalled : goalsToBeCalled) {
					String dir = file.substring(0, file.lastIndexOf("/"));
					if (file.startsWith("plugins-configuration/goals/") && dir.endsWith(goalToBeCalled)) {
						return true;
					}
				}
			} else {
				goal = goal.replace(":", "_");
				if (file.startsWith("plugins-configuration/goals/" + goal)) {
					return true;
				}
				String dir = file.substring(0, file.lastIndexOf("/"));
				if (dir.endsWith("-")) {
					if (("plugins-configuration/goals/" + goal).startsWith(dir)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static List<String> getGoalsToBeCalled(MavenSession session, String pluginKey, LifecyclePhase lifecyclePhase) {
		List<String> goals = new ArrayList<String>();
		for (MavenProject mavenProject : session.getProjects()) {
			Plugin plugin = mavenProject.getPlugin(pluginKey);
			for (PluginExecution execution : plugin.getExecutions()) {
				for (String goal : execution.getGoals()) {
					if (!goals.contains(goal)) {
						String phase = execution.getPhase();
						LifecyclePhase goalLifecylePhase = LifecyclePhase.NONE;
						if (phase != null) {
							phase = phase.trim().replace("-", "_").toUpperCase();
							goalLifecylePhase = LifecyclePhase.valueOf(phase);
						}
						if (goalLifecylePhase != LifecyclePhase.NONE && goalLifecylePhase.ordinal() <= lifecyclePhase.ordinal()) {
							goals.add(goal);
						}
					}
				}
			}
		}

		return goals;
	}

	/**
	 * <p>
	 * Look for XML files inside "plugins-configuration/" folder of the current
	 * plugin.
	 * </p>
	 *
	 * @param logger
	 * @param fromClass
	 * @return
	 */
	public static <T> List<File> getPluginsConfigurationFromClasspath(MavenSession session, Logger logger, Class<T> fromClass, String pluginKey) {
		MavenProject mavenProject = session.getCurrentProject();

		List<File> result = new ArrayList<File>();

		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forClass(fromClass))
			.setScanners(new ResourcesScanner())
		);

		Set<String> _files = reflections.getResources(Pattern.compile(".*\\.xml"));
		List<String> files = new ArrayList<String>(_files);

		for (ListIterator<String> it = files.listIterator(); it.hasNext();) {
			String file = (String) it.next();
			if (!file.startsWith("plugins-configuration/default") &&
				!file.startsWith("plugins-configuration/packaging/" + mavenProject.getPackaging().trim()) &&
				!hasGoal(session, file, fromClass, pluginKey)
			   ) {
				it.remove();
			}
		}

		for (String file : files) {
			result.add(new File(file));
		}

		Collections.sort(result);

		logger.debug("Adding plugins from classpath: " + result.toString());

		return result;
	}

	private static Plugin getPluginFromMavenProject(MavenProject mavenProject, String pluginKey) {
		if (mavenProject == null || pluginKey == null || pluginKey.isEmpty()) return null;

		for (Plugin plugin : mavenProject.getModel().getBuild().getPlugins()) {
			if (pluginKey.equals(plugin.getKey())) {
				return plugin;
			}
		}

		return null;
	}

	/**
	 * <p>
	 * Merge configuration in "plugins-configuration" of existing plugins.
	 * </p>
	 *
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	public static <T> void updatePluginsConfiguration(MavenProject mavenProject, MavenSession session, boolean createIfNotExists, Class<T> fromClass, Logger logger, String pluginKey) throws MojoExecutionException, IOException {
		if (session == null) return;
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
			List<File> pluginsConfiguration = PluginConfigurator.getPluginsConfigurationFromClasspath(session, logger, fromClass, pluginKey);
			for (File file : pluginsConfiguration) {
				String artifactId = file.getName().replace(".xml", "");
				String groupId = file.getParentFile().getName();
				String currentPluginKey = groupId + ":" + artifactId;

				Plugin plugin = getPluginFromMavenProject(mavenProject, currentPluginKey);

				PluginBuilder pluginBuilder;
				if (plugin == null) {
					pluginBuilder = new PluginBuilder(groupId, artifactId);
					mavenProject.getBuild().addPlugin(pluginBuilder.getPlugin());
				} else {
					pluginBuilder = new PluginBuilder(plugin);
				}
				pluginBuilder.addConfigurationFromClasspath(file.getPath());
			}
		}
	}

	/**
	 * <p>
	 * Retrieve the property value of a property.
	 * The order is:
	 *  <ul>
	 *   <li>
	 *    the original model (the POM file)
	 *   </li>
	 *   <li>
	 *    the calculated model (POM file + parents + injected properties)
	 *   </li>
	 *   <li>
	 *    built-in Maven properties ("basedir", "project.build.directory"...)
	 *   </li>
	 *  </ul>
	 * </p>
	 *
	 * @param mavenProject
	 * @param propertyName
	 * @return
	 */
	private static String getPropertyValue(MavenProject mavenProject, String propertyName) {
		assert(propertiesManager != null);

		String value = propertiesManager.getPropertyValue(propertyName);

		if (value == null) {
			if ("basedir".equals(propertyName) || "project.basedir".equals(propertyName)) {
				value = mavenProject.getBasedir().getAbsolutePath();
			} else if ("project.build.directory".equals(propertyName)) {
				value = mavenProject.getBuild().getDirectory();
			} else if ("project.build.finalName".equals(propertyName)) {
				value = mavenProject.getBuild().getFinalName();
			}
		}
		return value;
	}

	/**
	 * <p>
	 *  Proxy to call {@code updateProperty} for a {@link Parameter}.
	 * </p>
	 *
	 * @param mavenProject
	 * @param parameter
	 * @return
	 */
	public static String updateProperty(MavenProject mavenProject, Parameter parameter) {
		if (parameter == null) return null;

		String property = parameter.property();
		String defaultValue = parameter.defaultValue();
		if (defaultValue.isEmpty() && parameter.required()) {
			defaultValue = null;
		}

		return updateProperty(mavenProject, property, defaultValue);
	}

	/**
	 * <p>
	 *  Proxy to call {@code updateProperty} for a {@link GlobalParameter}.
	 * </p>
	 *
	 * @param mavenProject
	 * @param globalParameter
	 * @return
	 */
	public static String updateProperty(MavenProject mavenProject, GlobalParameter globalParameter) {
		if (globalParameter == null) return null;

		String property = globalParameter.property();
		String defaultValue = globalParameter.defaultValue();
		if (defaultValue.isEmpty() && (globalParameter.required() || !globalParameter.valueGuessedByDefault())) {
			defaultValue = null;
		}

		return updateProperty(mavenProject, property, defaultValue);
	}

	/**
	 *
	 * @param mavenProject
	 * @param propertyName
	 * @param defaultValue
	 * @return
	 */
	public static String updateProperty(MavenProject mavenProject, String propertyName, String defaultValue) {
		if (propertyName != null && !propertyName.isEmpty()) { // && defaultValue != null) {
			assert(propertiesManager != null);

			if (!propertiesManager.propertyExists(propertyName) && defaultValue != null && !defaultValue.isEmpty()) { // do not overwrite with default value if the property exists in model (i.e. in POM, command-line or settings.xml)
				mavenProject.getProperties().put(propertyName, defaultValue);
			} else {
				String value = getPropertyValue(mavenProject, propertyName);

				if (value != null) {
					String oldValue = null;
					assert(propertiesManager != null);

					while (CommonMojo.mavenPropertyPattern.matcher(value).find() && (!value.equals(oldValue))) {
						oldValue = value;
						value = propertiesManager.replaceProperties(value);
					}

					mavenProject.getProperties().put(propertyName, value);
				}

				defaultValue = value;
			}
		}

		if ("project.build.directory".equals(propertyName)) {
			String directory = mavenProject.getBuild().getDirectory();
			if (directory != null && CommonMojo.mavenPropertyPattern.matcher(directory).find()) {
				mavenProject.getBuild().setDirectory(defaultValue);
			}
		}

		return defaultValue;
	}

	/**
	 * <p>
	 * Inject values for fields annotated with {@link GlobalParameter} or
	 * {@link Parameter} into the Maven model (as properties).
	 * </p>
	 *
	 * @param mavenProject
	 * @param fromClass
	 * @param logger
	 */
	public static <T> void addPluginsParameterInModel(MavenProject mavenProject, Class<T> fromClass, Logger logger) {
		PluginConfigurator.propertiesManager.setProject(mavenProject);

		setProjectActualBasedir(mavenProject);

		Set<Field> parameters = AnnotationsHelper.getFieldsAnnotatedWith(fromClass, Parameter.class);

		for (Field field : parameters) {
			Parameter parameter = field.getAnnotation(Parameter.class);

			updateProperty(mavenProject, parameter);
		}

		Set<Field> globalParameters = AnnotationsHelper.getFieldsAnnotatedWith(fromClass, GlobalParameter.class);

		for (Field field : globalParameters) {
			GlobalParameter globalParameter = field.getAnnotation(GlobalParameter.class);

			updateProperty(mavenProject, globalParameter);
		}
	}

	private static void setProjectActualBasedir(MavenProject mavenProject) {
		MavenProject currentProject = mavenProject;
		while (currentProject != null) {
			Properties props = currentProject.getOriginalModel().getProperties();
			for (Object k : props.keySet()) {
				String key = (String) k;
				String value = props.getProperty(key);
				if (value != null && value.contains(projectActualBasedir)) {
					value = PluginConfigurator.propertiesManager.replaceProperty(value, projectActualBasedir, currentProject.getFile().getParentFile().getAbsolutePath());
					value = PluginConfigurator.propertiesManager.replaceProperties(value);
					mavenProject.getModel().getProperties().setProperty(key, value);
				}
			}
			currentProject = currentProject.getParent();
		}
	}

}
