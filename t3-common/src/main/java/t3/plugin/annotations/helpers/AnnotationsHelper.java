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
package t3.plugin.annotations.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class AnnotationsHelper {

	public static <A extends Annotation> Set<Field> getFieldsAnnotatedWith(Class<?> fromClass, Class<A> annotationClass) {
		return getFieldsAnnotatedWith(fromClass, annotationClass, ClasspathHelper.contextClassLoader());
	}

	public static <A extends Annotation> Set<Field> getFieldsAnnotatedWith(Class<?> fromClass, Class<A> annotationClass, ClassLoader... classLoaders) {
//		Reflections.log = NOPLogger.NOP_LOGGER;

		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forClass(fromClass, classLoaders),
					 ClasspathHelper.forClass(annotationClass, classLoaders))
			.setScanners(new FieldAnnotationsScanner()).addClassLoaders(classLoaders)
		);

		Set<Field> fields = reflections.getFieldsAnnotatedWith(annotationClass);

		return fields;
	}

	public static <A extends Annotation> Set<Class<?>> getTypesAnnotatedWith(Class<?> fromClass, Class<A> annotationClass) {
		return getTypesAnnotatedWith(fromClass, annotationClass, ClasspathHelper.contextClassLoader());		
	}

	public static <A extends Annotation> Set<Class<?>> getTypesAnnotatedWith(Class<?> fromClass, Class<A> annotationClass, ClassLoader... classLoaders) {
//		Reflections.log = NOPLogger.NOP_LOGGER;

		Reflections reflections = new Reflections(new ConfigurationBuilder()
			.setUrls(ClasspathHelper.forClass(fromClass, classLoaders),
					 ClasspathHelper.forClass(annotationClass, classLoaders))
			.setScanners(new TypeAnnotationsScanner(), new SubTypesScanner()).addClassLoaders(classLoaders)
		);

		Set<Class<?>> types = reflections.getTypesAnnotatedWith(annotationClass);

		return types;
	}

}
