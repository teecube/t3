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
package t3.site.parameters;

import org.apache.maven.model.Dependency;

public class DependencyWithAdditionalArguments extends Dependency {
	private static final long serialVersionUID = -2314389014736859116L;

	private String archetypeAdditionalArguments;

	public String getArchetypeAdditionalArguments() {
		return archetypeAdditionalArguments;
	}

	public void setArchetypeAdditionalArguments(String archetypeAdditionalArguments) {
		this.archetypeAdditionalArguments = archetypeAdditionalArguments;
	}
}
