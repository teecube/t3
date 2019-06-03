/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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

import t3.plugin.annotations.GlobalParameter;
import t3.plugin.annotations.Parameter;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class ParameterImpl implements Parameter, GlobalParameter {

    private String property;
    private String defaultValue;
    private boolean required;

    private String description;
    private List<String> requiredForPackagings;
    private boolean hideDocumentation;

    private String category; // only for GlobalParameter
    private boolean valueGuessedByDefault; // only for GlobalParameter

    private String type;
    private String field;

    public ParameterImpl(String field, String type, String property, String defaultValue, boolean required, List<String> requiredForPackagings, String description, boolean hideDocumentation, String category, boolean valueGuessedByDefault) {
        this.field = field;
        this.type = type;
        this.property = property;
        this.defaultValue = defaultValue;
        this.required = required;
        this.requiredForPackagings = requiredForPackagings;
        this.description = description;
        this.hideDocumentation = hideDocumentation;
        this.category = category;
        this.valueGuessedByDefault = valueGuessedByDefault;
    }

    // fields from org.apache.maven.plugins.annotations.Parameter
    public String alias() {
        return "";
    }

    public String property() {
        return property;
    }
    public void setProperty(String property) {
        this.property = property;
    }

    public String defaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean required() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean readonly() {
        return false;
    }

    // additional fields
    public String description() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String[] requiredForPackagings() {
        return requiredForPackagings.toArray(new String[0]);
    }

    public boolean hideDocumentation() {
        return hideDocumentation;
    }
    public void setDescription(boolean hideDocumentation) {
        this.hideDocumentation = hideDocumentation;
    }

    public String category() {
        return category;
    }
    public boolean valueGuessedByDefault() {
        return valueGuessedByDefault;
    }

    // reflection fields
    public String field() {
        return field;
    }
    public void setField(String field) {
        this.field = field;
    }

    public String type() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Parameter.class;
    }

}
