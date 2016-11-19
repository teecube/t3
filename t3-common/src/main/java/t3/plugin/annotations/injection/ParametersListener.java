/**
 * (C) Copyright 2016-2016 teecube
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
package t3.plugin.annotations.injection;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import com.google.inject.AbstractModule;
import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import t3.CommonMojo;
import t3.plugin.PluginConfigurator;
import t3.plugin.annotations.GlobalParameter;
import t3.plugin.annotations.Parameter;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 * @param <T>
 */
public class ParametersListener<T> implements TypeListener {

	private T originalObject;
	private MavenProject mavenProject;
	private List<Map.Entry<String,String>> ignoredParameters;

	public ParametersListener(T originalObject, MavenProject mavenProject, MavenSession session) {
		this(originalObject, mavenProject, session, null);
	}

	public ParametersListener(T originalObject, MavenProject mavenProject, MavenSession session, List<Map.Entry<String,String>> ignoredParameters) {
		if (ignoredParameters == null) {
			ignoredParameters = new ArrayList<Map.Entry<String,String>>();
		}

		this.originalObject = originalObject;
		this.mavenProject = mavenProject;
		this.ignoredParameters = ignoredParameters;

		PluginConfigurator.propertiesManager = CommonMojo.propertiesManager(session, mavenProject);
	}

	@SuppressWarnings("unchecked")
	public ParametersListener(AbstractModule originalObject, MavenProject mavenProject, MavenSession session, List<Map.Entry<String,String>> ignoredParameters) {
		this((T) originalObject, mavenProject, session, ignoredParameters);
	}

	@SuppressWarnings("unchecked")
	public ParametersListener(AbstractModule originalObject, MavenProject mavenProject, MavenSession session) {
		this((T) originalObject, mavenProject, session, null);
	}

	@SuppressWarnings("unchecked")
	public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
		Class<?> clazz = typeLiteral.getRawType();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				String value;
				if (ignoredParameters.contains(new AbstractMap.SimpleEntry<>(field.getName(), clazz.getCanonicalName()))) {
					continue;
				}
				if (field.isAnnotationPresent(GlobalParameter.class)) {
					GlobalParameter globalParameter = field.getAnnotation(GlobalParameter.class);
					value = PluginConfigurator.updateProperty(mavenProject, globalParameter);
				} else if (field.isAnnotationPresent(Parameter.class)) {
					Parameter mojoParameter = field.getAnnotation(Parameter.class);
					value = PluginConfigurator.updateProperty(mavenProject, mojoParameter);
				} else {
					continue;
				}
				typeEncounter.register((MembersInjector<? super I>) new ParametersMembersInjector<T>(field, value, (T) originalObject));
			}
			clazz = clazz.getSuperclass();
		}
	}
}