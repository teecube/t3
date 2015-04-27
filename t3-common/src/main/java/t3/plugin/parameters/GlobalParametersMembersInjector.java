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

public class GlobalParametersMembersInjector<T> implements MembersInjector<T> {
	private final Field field;
	private final String value;
	private T originalObject;

	public GlobalParametersMembersInjector(Field field, String value) {
		this.field = field;
		this.field.setAccessible(true);
		this.value = value;
	}

	public GlobalParametersMembersInjector(Field field, String value, T originalObject) {
		this(field, value);
		this.originalObject = originalObject;
	}

	public void injectMembers(T t) {
		String type = field.getType().getSimpleName();
		Object finalValue;
		switch (type) {
		case "File":
			finalValue = new File(value);
			break;

		default:
			finalValue = value;
			break;
		}
		try {
			field.set(t, finalValue);
			if (originalObject != null) {
				try {
					Field originalField = originalObject.getClass()
							.getDeclaredField(field.getName());
					originalField.setAccessible(true);
					originalField.set(originalObject, finalValue);
				} catch (IllegalArgumentException | NoSuchFieldException
						| SecurityException e) {
					// no trace
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}