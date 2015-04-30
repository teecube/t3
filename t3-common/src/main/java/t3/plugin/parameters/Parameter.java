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


/**
*
* @author Mathieu Debove &lt;mad@t3soft.org&gt;
*
*/
public class Parameter {

	private String field;
	private String type;
	private String property;
	private String defaultValue;
	private boolean required;

	public Parameter(String field, String type, String property, String defaultValue, boolean required) {
		this.field = field;
		this.type = type;
		this.property = property;
		this.defaultValue = defaultValue;
		this.required = required;
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

}
