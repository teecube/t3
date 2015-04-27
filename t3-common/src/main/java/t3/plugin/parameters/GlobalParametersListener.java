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
package t3.plugin.parameters;

import java.lang.reflect.Field;

import org.apache.maven.project.MavenProject;

import t3.GlobalParameter;

import com.google.inject.MembersInjector;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GlobalParametersListener<T> implements TypeListener {

	private T originalObject;
	private MavenProject mavenProject;

	public GlobalParametersListener(T originalObject, MavenProject mavenProject) {
		this.originalObject = originalObject;
		this.mavenProject = mavenProject;
	}

	@SuppressWarnings("unchecked")
	public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
		Class<?> clazz = typeLiteral.getRawType();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(GlobalParameter.class)) {
					GlobalParameter globalParameter = field.getAnnotation(GlobalParameter.class);
					String property = globalParameter.property();
					String defaultValue = globalParameter.defaultValue();
					String value = mavenProject.getModel().getProperties().getProperty(property);
					if (value == null) {
						value = defaultValue;
					}
					typeEncounter.register((MembersInjector<? super I>) new GlobalParametersMembersInjector<T>(field, value, (T) originalObject));
				}
			}
			clazz = clazz.getSuperclass();
		}
	}
}