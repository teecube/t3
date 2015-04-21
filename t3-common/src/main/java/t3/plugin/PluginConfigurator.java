/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://t3soft.org) and others.
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

import t3._Parameter;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class PluginConfigurator {

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
	 *
	 * </p>
	 *
	 * @param mavenProject
	 * @param fromClass
	 * @param logger
	 */
	public static <T> void addPluginsParameterInModel(MavenProject mavenProject, Class<T> fromClass, Logger logger) {
		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forClass(fromClass),
					 ClasspathHelper.forClass(_Parameter.class)) // clone of org.apache.maven.plugins.annotations.Parameter annotation (with RUNTIME retention policy)
			.setScanners(new FieldAnnotationsScanner())
		);

		Set<Field> parameters = reflections.getFieldsAnnotatedWith(_Parameter.class);

		for (Field field : parameters) {
			_Parameter anno = field.getAnnotation(_Parameter.class);
			String property = anno.property();
			String defaultValue = anno.defaultValue();

			if (property != null && !property.isEmpty() && defaultValue != null) {
				if (!mavenProject.getProperties().containsKey(property)) { // do not overwrite with default value if the property exists in model (i.e. in POM)
					mavenProject.getProperties().put(property, defaultValue);
				} else if ("project.build.directory".equals(property)) {
					mavenProject.getBuild().setDirectory(mavenProject.getProperties().getProperty(property, defaultValue));
//					mavenProject.getProperties().put(property, mavenProject.getProperties().getProperty(property, defaultValue));
				} else {
					String value = mavenProject.getOriginalModel().getProperties().getProperty(property);

					if (value != null) {
						Pattern p = Pattern.compile("\\$\\{([^}]*)\\}");
						Matcher m = p.matcher(value);

						StringBuffer sb = new StringBuffer();

						while (m.find()) {
							String propertyName = m.group(1);
							String propertyValue = mavenProject.getModel().getProperties().getProperty(propertyName);
							if (propertyValue != null) {
							    m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
							}
						}
						m.appendTail(sb);
						value = sb.toString();

						mavenProject.getProperties().put(property, value);
					}
				}
			}
		}
	}

}
