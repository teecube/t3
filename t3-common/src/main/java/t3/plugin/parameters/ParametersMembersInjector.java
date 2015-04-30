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

import java.io.File;
import java.lang.reflect.Field;

import com.google.inject.MembersInjector;

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
			finalValue = new File(value);
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