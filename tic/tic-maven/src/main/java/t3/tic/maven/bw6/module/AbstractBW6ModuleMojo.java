/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://www.t3soft.org) and others.
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
package t3.tic.maven.bw6.module;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

import t3.tic.maven.bw6.AbstractBW6ProjectMojo;

/**
 * <p>
 * A BW6 module can be:
 * </p>
 *  <ul>
 *   <li>an app module</li>
 *   <li>an shared module</li>
 *  </ul>
 * <p>
 *  They both have in common:
 * </p>
 *  <ul>
 *   <li>a build.properties file</li>
 *  </ul>
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public abstract class AbstractBW6ModuleMojo extends AbstractBW6ProjectMojo {

	@Parameter (property = "tibco.bw6.project.module.build.properties", defaultValue = "${project.build.directory}/build.properties", required = true)
	protected File buildProperties;

	@Parameter (property = "tibco.bw6.project.module.build.properties.source", defaultValue = "${basedir}/build.properties", required = true)
	protected File buildPropertiesSource;

}
