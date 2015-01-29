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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.settings.Settings;

/**
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
public abstract class AbstractBW6Mojo extends AbstractMojo {

	@Parameter( property = "project.build.directory", required = true )
	protected File outputDirectory;

	@Parameter( defaultValue = "${basedir}", readonly = true)
	protected File projectBasedir;

	@Parameter ( defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	@Parameter ( defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	@Parameter ( defaultValue = "${mojoExecution}", readonly = true)
	protected MojoExecution mojoExecution;

	@Parameter ( defaultValue = "${plugin}", readonly = true)
	protected PluginDescriptor pluginDescriptor; // plugin descriptor of this plugin

	@Parameter ( defaultValue = "${settings}", readonly = true)
	protected Settings settings;

	@Component
	protected ProjectBuilder builder;

	/**
	 * <p>Create the output directory ("target/") if it doesn't exist yet.</p>
	 */
	protected void createOutputDirectory() {
		if (outputDirectory == null || outputDirectory.exists()) {
			return;
		}

		outputDirectory.mkdirs();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException  {
		createOutputDirectory();
	}
}
