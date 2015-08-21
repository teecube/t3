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

import java.io.File;

import t3.plugin.annotations.GlobalParameter;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
public class AbstractTIBCOMojo extends AbstractCommonMojo {

	@GlobalParameter (property = CommonMojoInformation.tibcoHome, required = true, description = CommonMojoInformation.tibcoHome_description, category = CommonMojoInformation.tibcoCategory, valueGuessedByDefault = false)
	protected File tibcoHOME;

}
