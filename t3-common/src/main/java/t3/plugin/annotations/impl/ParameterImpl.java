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
package t3.plugin.annotations.impl;

import java.util.List;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class ParameterImpl {

	private String field;
	private String type;
	private String property;
	private String defaultValue;
	private boolean required;
	private List<String> requiredForPackagings;
	private String description;
	private String category; // only for GlobalParameter
	private boolean valueGuessedByDefault; // only for GlobalParameter

	public ParameterImpl(String field, String type, String property, String defaultValue, boolean required, List<String> requiredForPackagings, String description, String category, boolean valueGuessedByDefault) {
		this.field = field;
		this.type = type;
		this.property = property;
		this.defaultValue = defaultValue;
		this.required = required;
		this.setRequiredForPackagings(requiredForPackagings);
		this.description = description;
		this.category = category;
		this.valueGuessedByDefault = valueGuessedByDefault;
	}

	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}

	public List<String> getRequiredForPackagings() {
		return requiredForPackagings;
	}
	public void setRequiredForPackagings(List<String> requiredForPackagings) {
		this.requiredForPackagings = requiredForPackagings;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}

	public boolean isValueGuessedByDefault() {
		return valueGuessedByDefault;
	}
	public void setCategory(boolean valueGuessedByDefault) {
		this.valueGuessedByDefault = valueGuessedByDefault;
	}

}
