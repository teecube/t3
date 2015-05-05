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

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import t3.plugin.annotations.FieldsHelper;
import t3.plugin.parameters.GlobalParameter;
import t3.plugin.parameters.MojoParameter;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
public class PluginConfigurator {

	public static final Pattern mavenPropertyPattern = Pattern.compile("\\$\\{([^}]*)\\}");

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
	public static <T> List<File> getPluginsConfigurationFromClasspath(Logger logger, Class<T> fromClass) {
		List<File> result = new ArrayList<File>();

		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forClass(fromClass))
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

	/**
	 * <p>
	 * Merge configuration in "plugins-configuration" of existing plugins.
	 * </p>
	 *
	 * @throws MojoExecutionException
	 * @throws IOException
	 */
	public static <T> void updatePluginsConfiguration(MavenProject mavenProject, boolean createIfNotExists, Class<T> fromClass, Logger logger) throws MojoExecutionException, IOException {
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
			List<File> pluginsConfiguration = PluginConfigurator.getPluginsConfigurationFromClasspath(logger, fromClass);
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
		String value = mavenProject.getOriginalModel().getProperties().getProperty(propertyName);

		if (value == null) {
			value = mavenProject.getModel().getProperties().getProperty(propertyName);
		}

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
	 *  Proxy to call {@code updateProperty} for a {@link MojoParameter}.
	 * </p>
	 *
	 * @param mavenProject
	 * @param parameter
	 * @return
	 */
	public static String updateProperty(MavenProject mavenProject, MojoParameter parameter) {
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
		if (defaultValue.isEmpty() && globalParameter.required()) {
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
			if (!mavenProject.getProperties().containsKey(propertyName) && defaultValue != null) { // do not overwrite with default value if the property exists in model (i.e. in POM)
				mavenProject.getProperties().put(propertyName, defaultValue);
			} else {
				String value = getPropertyValue(mavenProject, propertyName);

				if (value != null) {
					String oldValue = null;
					while (mavenPropertyPattern.matcher(value).find() && (!value.equals(oldValue))) {
						oldValue = value;
						value = replaceProperties(value, mavenProject);
					}

					mavenProject.getProperties().put(propertyName, value);
				}

				defaultValue = value;
			}
		}

		if ("project.build.directory".equals(propertyName)) {
			mavenProject.getBuild().setDirectory(defaultValue);
		}

		return defaultValue;
	}

	public static String replaceProperties(String value, MavenProject mavenProject) {
		Matcher m = mavenPropertyPattern.matcher(value);

		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String propertyName = m.group(1);
			String propertyValue = getPropertyValue(mavenProject, propertyName);

			if (propertyValue != null) {
			    m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
			}
		}
		m.appendTail(sb);
		value = sb.toString();

		return value;
	}

	/**
	 * <p>
	 * Inject values for fields annotated with {@link GlobalParameter} or
	 * {@link MojoParameter} into the Maven model (as properties).
	 * </p>
	 *
	 * @param mavenProject
	 * @param fromClass
	 * @param logger
	 */
	public static <T> void addPluginsParameterInModel(MavenProject mavenProject, Class<T> fromClass, Logger logger) {
		Set<Field> parameters = FieldsHelper.getFieldsAnnotatedWith(fromClass, MojoParameter.class);

		for (Field field : parameters) {
			MojoParameter parameter = field.getAnnotation(MojoParameter.class);

			updateProperty(mavenProject, parameter);
		}

		Set<Field> globalParameters = FieldsHelper.getFieldsAnnotatedWith(fromClass, GlobalParameter.class);

		for (Field field : globalParameters) {
			GlobalParameter globalParameter = field.getAnnotation(GlobalParameter.class);

			updateProperty(mavenProject, globalParameter);
		}
	}

}
