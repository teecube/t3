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
package t3;

import t3.plugin.annotations.Categories;
import t3.plugin.annotations.Category;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
@Categories({
	@Category(title = CommonMojoInformation.mavenCategory, description = CommonMojoInformation.mavenCategory_description),
	@Category(title = CommonMojoInformation.systemCategory, description = CommonMojoInformation.systemCategory_description),
	@Category(title = CommonMojoInformation.tibcoCategory, description = CommonMojoInformation.tibcoCategory_description),
})
public class CommonMojoInformation {

	/* Categories */
	public static final String mavenCategory = "Standard Maven";
	public static final String mavenCategory_description = "Default built-in Maven properties belong to the Standard Maven category";

	public static final String systemCategory = "System";
	public static final String systemCategory_description = "System properties depends on environment (Linux, Windows...)";

	public static final String tibcoCategory = "TIBCO";
	public static final String tibcoCategory_description = "Properties are specific to TIBCO settings";

	/* TIBCO home */
	public static final String tibcoHome = "tibco.home";
	public static final String tibcoHome_description = "The path of a valid TIBCO installation to use with the plugin.";

}
