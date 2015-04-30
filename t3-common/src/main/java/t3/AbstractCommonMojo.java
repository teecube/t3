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
package t3;

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
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import t3.plugin.parameters.GlobalParameter;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class AbstractCommonMojo extends AbstractMojo {

	@GlobalParameter (property = "tibco.home", required = true)
	protected File tibcoHOME;

	@GlobalParameter (property = "executables.extension", required = true)
	protected String executablesExtension;

	@GlobalParameter (property = "project.build.directory", defaultValue = "${basedir}/target" ) // target
	protected File directory;

	@GlobalParameter (property = "project.output.directory", defaultValue = "${project.build.directory}/output" ) // target/output (instead of target/classes)
	protected File outputDirectory;

	@GlobalParameter (property = "project.test.directory", defaultValue = "${project.build.directory}/test" ) // target/test
	protected File testOutputDirectory;

	@GlobalParameter (property="project.build.sourceEncoding", defaultValue = "UTF-8")
	protected String sourceEncoding;

	@Parameter (defaultValue = "${basedir}", readonly = true)
	protected File projectBasedir;

	@Parameter (defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	@Parameter (defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	@Parameter (defaultValue = "${mojoExecution}", readonly = true)
	protected MojoExecution mojoExecution;

	@Parameter (defaultValue = "${plugin}", readonly = true)
	protected PluginDescriptor pluginDescriptor; // plugin descriptor of this plugin

	@Parameter (defaultValue = "${settings}", readonly = true)
	protected Settings settings;

	@Component
	protected ProjectBuilder builder;

	@Component( role=org.apache.maven.shared.filtering.MavenResourcesFiltering.class, hint="default")
	protected MavenResourcesFiltering mavenResourcesFiltering;

	/**
	 * <p>
	 * Create the output directory (usually "target/") if it doesn't exist yet.
	 * </p>
	 */
	protected void createOutputDirectory() {
		if (directory == null || directory.exists()) {
			return;
		}

		directory.mkdirs();
	}

	protected void enableTestScope() {
		directory = testOutputDirectory; // set directory to "target/test" instead of "target"
	}

	protected boolean isCurrentGoal(String goal) {
		return session.getRequest().getGoals().contains(goal);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException  {
		createOutputDirectory();
	}

//	public <T extends AbstractCommonMojo> AbstractCommonMojo(T mojo) {
//		System.out.println("deep copy AbstractCommonMojo");
//	}
}
