/**
 * (C) Copyright 2016-2017 teecube
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import t3.plugin.annotations.Categories;
import t3.plugin.annotations.Category;
import t3.plugin.annotations.impl.CategoryImpl;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class CategoriesHelper {

	private static String getTitle(Annotation annotation) {
		return (String) getObject(annotation, "title");
	}

	private static String getDescription(Annotation annotation) {
		return (String) getObject(annotation, "description");
	}

	private static Object[] getCategories(Annotation annotation) {
		return (Object[]) getObject(annotation, "value");
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

	public static <A extends Annotation> Set<CategoryImpl> getCategories(Set<Class<?>> types) {
		Set<CategoryImpl> result = new HashSet<CategoryImpl>();

		String title = null;
		String description = null;

		for (Class<?> type : types) {
			Categories categories = type.getAnnotation(Categories.class);
			if (categories == null) {
				for (Annotation a1 : type.getAnnotations()) {
					if (a1.annotationType().getCanonicalName().equals(Categories.class.getCanonicalName())) {
						Object[] categories_ = getCategories(a1);
						for (Object category : categories_) {
							Annotation a2 = (Annotation) category;
							title = getTitle(a2);
							description = getDescription(a2);
							result.add(new CategoryImpl(title, description));
						}
					}
				}
			} else {
				for (Category category : categories.value()) {
					title = getTitle(category);
					description = getDescription(category);
					result.add(new CategoryImpl(title, description));
				}
			}
		}

		return result;
	}

}
