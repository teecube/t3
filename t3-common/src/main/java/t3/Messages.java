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
package t3;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public abstract class Messages {

    public static final String MESSAGE_SPACE = "";

    public static final String SKIPPING = "Skipping.";

    public static final String ENFORCING_RULES = "Enforcing rules...";
    public static final String ENFORCING_GLOBAL_RULES = "Global Rules...";
    public static final String ENFORCED_GLOBAL_RULES = "Global Rules are validated.";
    public static final String ENFORCING_PER_PROJECT_RULES = "Project Rules...";
    public static final String ENFORCED_PER_PROJECT_RULES = "Projects Rules are validated.";
    public static final String ENFORCER_RULES_FAILURE = "The required rules are invalid.";

    public static final String ARTIFACT_NOT_FOUND = "The artifact was not found."; // improve this message
}
