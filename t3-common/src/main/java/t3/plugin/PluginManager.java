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

import java.lang.reflect.Field;

import org.apache.maven.classrealm.ClassRealmManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MavenPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.PluginConfigurationException;
import org.apache.maven.plugin.PluginContainerException;
import org.apache.maven.plugin.PluginDescriptorCache;
import org.apache.maven.plugin.PluginRealmCache;
import org.apache.maven.plugin.internal.DefaultMavenPluginManager;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

import t3.AbstractCommonMojo;
import t3.MojosFactory;
import t3.plugin.parameters.ParametersListener;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
@Component( role = MavenPluginManager.class )
public class PluginManager extends DefaultMavenPluginManager {

	// actual Mojos factory is implemented in each Maven plugin
	private MojosFactory mojosFactory;

	public static DefaultMavenPluginManager getDefaultMavenPluginManager(BuildPluginManager pluginManager) {
		DefaultMavenPluginManager result = null;
		try {
			Field f = pluginManager.getClass().getDeclaredField("mavenPluginManager");
			f.setAccessible(true);
			result = (DefaultMavenPluginManager) f.get(pluginManager);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static PluginManager getCustomMavenPluginManager(BuildPluginManager pluginManager) {
		PluginManager result = null;
		try {
			Field f = pluginManager.getClass().getDeclaredField("mavenPluginManager");
			f.setAccessible(true);
			result = (PluginManager) f.get(pluginManager);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void registerCustomPluginManager(BuildPluginManager pluginManager, MojosFactory mojosFactory) {
		try {
			Field f = pluginManager.getClass().getDeclaredField("mavenPluginManager");
			f.setAccessible(true);
			DefaultMavenPluginManager oldMavenPluginManager = (DefaultMavenPluginManager) f.get(pluginManager);
			PluginManager mavenPluginManager = new PluginManager(oldMavenPluginManager, mojosFactory);
			f.set(pluginManager, mavenPluginManager);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			// no trace
		}
	}

	private <T> void copyField(String fieldName, Class<T> fieldType, DefaultMavenPluginManager defaultMavenPluginManager) throws Exception {
		Field oldField = defaultMavenPluginManager.getClass().getDeclaredField(fieldName);
		oldField.setAccessible(true);

		Class<?> parentClass = this.getClass().getSuperclass();
		Field newField = parentClass.getDeclaredField(fieldName);
		newField.setAccessible(true);

		T fieldValue = fieldType.cast(oldField.get(defaultMavenPluginManager));
		newField.set(this, fieldValue);
	}

	public PluginManager(DefaultMavenPluginManager defaultMavenPluginManager, MojosFactory mojosFactory) {
		if (defaultMavenPluginManager == null) return;

		this.mojosFactory = mojosFactory;

		// quick shallow copy of the original MavenPluginManager
		try {
			copyField("logger", Logger.class, defaultMavenPluginManager);
			copyField("loggerManager", LoggerManager.class, defaultMavenPluginManager);
			copyField("container", PlexusContainer.class, defaultMavenPluginManager);
			copyField("classRealmManager", ClassRealmManager.class, defaultMavenPluginManager);
			copyField("pluginDescriptorCache", PluginDescriptorCache.class, defaultMavenPluginManager);
			copyField("pluginRealmCache", PluginRealmCache.class, defaultMavenPluginManager);
			copyField("pluginDependenciesResolver", PluginDependenciesResolver.class, defaultMavenPluginManager);
			copyField("runtimeInformation", RuntimeInformation.class, defaultMavenPluginManager);
		} catch (Exception e) {
			// no trace
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getConfiguredMojo(Class<T> mojoInterface, final MavenSession session, MojoExecution mojoExecution) throws PluginConfigurationException, PluginContainerException {
		final T configuredMojo = super.getConfiguredMojo(mojoInterface, session, mojoExecution); // retrieve configuredMojo to know the actual type of the Mojo to configure

		Class<? extends AbstractCommonMojo> type;
		try {
			type = (Class<? extends AbstractCommonMojo>) configuredMojo.getClass();
		} catch (ClassCastException e) {
			return configuredMojo;
		}
		T rawMojo = (T) mojosFactory.getMojo(type); // retrieve a brand new and unconfigured Mojo of the actual type (with annotations)

		if (rawMojo != null) { // inject @MojoParameter and @GlobalParameter annotations
	        Injector i = Guice.createInjector(new AbstractModule() {
	            @Override
	            protected void configure() {
					bindListener(Matchers.any(), new ParametersListener<T>(configuredMojo, session.getCurrentProject(), session)); // WARN: using getCurrentProject() ?
	            }
	        });

	        i.injectMembers(rawMojo); // will also inject in configuredMojo
		}
		return configuredMojo;
	}

}
