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
package t3.plugin.annotations.injection;

import com.google.inject.MembersInjector;

import java.io.File;
import java.lang.reflect.Field;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 * @param <T>
 */
public class ParametersMembersInjector<T> implements MembersInjector<T> {
	private final Field field;
	private final String value;
	private T originalObject;

	public ParametersMembersInjector(Field field, String value) {
		this.field = field;
		this.field.setAccessible(true);
		this.value = value;
	}

	public ParametersMembersInjector(Field field, String value, T originalObject) {
		this(field, value);
		this.originalObject = originalObject;
	}

	public static Field getDeclaredField(String fieldName, Class<?> type) {
	    Field result = null;
		try {
			result = type.getDeclaredField(fieldName);
		} catch (NoSuchFieldException | SecurityException e) {
		    if (type.getSuperclass() != null) {
		        return getDeclaredField(fieldName, type.getSuperclass());
		    }
		}
		return result;
	}

	public void injectMembers(T t) {
		String type = field.getType().getSimpleName();
		Object finalValue;
		switch (type) {
		case "File":
			if (value != null) {
				finalValue = new File(value);
			} else {
				finalValue = null;
			}
			break;
		case "Boolean":
		case "boolean":
			finalValue = Boolean.parseBoolean(value);
			break;
		case "Integer":
		case "integer":
		case "int":
			finalValue = Integer.parseInt(value);
			break;
		default:
			finalValue = value;
			break;
		}
		try {
			field.set(t, finalValue);
			if (originalObject != null) {
				Field originalField = getDeclaredField(field.getName(), originalObject.getClass());
				if (originalField != null) {
					originalField.setAccessible(true);
					originalField.set(originalObject, finalValue);
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}