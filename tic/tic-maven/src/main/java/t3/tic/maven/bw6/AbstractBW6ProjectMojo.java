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
package t3.tic.maven.bw6;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

/**
 * <p>
 *  There are three kinds of BW6 projects:
 *  <ul>
 *   <li>BW6 app module</li>
 *   <li>BW6 shared module</li>
 *   <li>BW6 application</li>
 *  </ul>
 *  <br />
 *  They all have in common:
 *  <ul>
 *   <li>a META-INF folder</li>
 *   <li>a MANIFEST.MF file in the META-INF folder</li>
 *   <li>a .config file</li>
 *   <li>a .project file</li>
 *  </ul>
 *  <br />
 *  This abstract Mojo (which can be inherited my concrete ones) defines all
 * these common objects as Mojo parameters.
 * </p>
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
public abstract class AbstractBW6ProjectMojo extends AbstractBW6Mojo {

	@Parameter (property = "tibco.bw6.metainf.source", defaultValue = "${basedir}/META-INF", required = true)
	protected File metaInfSource;

	@Parameter (property = "tibco.bw6.metainf", defaultValue = "${project.build.directory}/META-INF", required = true)
	protected File metaInf;

	@Parameter (property = "tibco.bw6.manifest.source", defaultValue = "${basedir}/META-INF/MANIFEST.MF", required = true)
	protected File manifestSource;

	@Parameter (property = "tibco.bw6.manifest", defaultValue = "${project.build.directory}/META-INF/MANIFEST.MF", required = true)
	protected File manifest;

	@Parameter (property = "tibco.bw6.dot.config", defaultValue = "${basedir}/.config", required = true)
	protected File dotConfig;

	@Parameter (property = "tibco.bw6.dot.project", defaultValue = "${basedir}/.project", required = true)
	protected File dotProject;

}
