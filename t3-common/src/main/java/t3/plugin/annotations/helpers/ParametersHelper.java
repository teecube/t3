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
package t3.plugin.annotations.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import t3.plugin.annotations.impl.ParameterImpl;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
public class ParametersHelper {

	private static String getProperty(Annotation annotation) {
		return (String) getObject(annotation, "property");
	}

	private static String getDefaultValue(Annotation annotation) {
		return (String) getObject(annotation, "defaultValue");
	}

	private static boolean getRequired(Annotation annotation) {
		return (boolean) getObject(annotation, "required");
	}

	private static String getDescription(Annotation annotation) {
		return (String) getObject(annotation, "description");
	}

	private static String getCategory(Annotation annotation) {
		Object o = getObject(annotation, "category");
		return o != null ? (String) o : null;
	}

	private static boolean getValueGuessedByDefault(Annotation annotation) {
		Object o = getObject(annotation, "valueGuessedByDefault");
		return o != null ? (Boolean) o : true;
	}

	private static Object getObject(Annotation annotation, String methodName) {
		InvocationHandler handler = Proxy.getInvocationHandler(annotation);
		Method method;
		try {
			method = annotation.annotationType().getMethod(methodName, (Class<?>[]) null);
			return handler.invoke(annotation, method, null);
		} catch (Throwable e) {
			return null;
		}
	}

	public static <A extends Annotation> Set<ParameterImpl> getFieldsAnnotatedWith(Set<Field> fields, Class<A> parameterAnnotation) {
		Set<ParameterImpl> result = new HashSet<ParameterImpl>();

		for (Field field : fields) {
			field.setAccessible(true);
			A annotation = field.getAnnotation(parameterAnnotation);

			String property = null;
			String defaultValue = null;
			boolean required = false;
			String description = null;
			String category = null;
			boolean valueGuessedByDefault = true;

			if (annotation != null) {
				property = getProperty(annotation);
				defaultValue = getDefaultValue(annotation);
				required = getRequired(annotation);
				description = getDescription(annotation);
				category = getCategory(annotation);
				valueGuessedByDefault = getValueGuessedByDefault(annotation);
			} else {
				for (Annotation a : field.getAnnotations()) {
					if (a.annotationType().getCanonicalName().equals(parameterAnnotation.getCanonicalName())) {
						property = getProperty(a);
						defaultValue = getDefaultValue(a);
						required = getRequired(a);
						description = getDescription(a);
						category = getCategory(a);
						valueGuessedByDefault = getValueGuessedByDefault(a);
					}
				}
			}
			if (property != null) {
				result.add(new ParameterImpl(field.getName(), field.getType().getCanonicalName(), property, defaultValue, required, description, category, valueGuessedByDefault));
			}
		}

		return result;
	}

}
