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
import java.math.BigInteger;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TreeSet;

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
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class AbstractCommonMojo extends AbstractMojo {

	@Parameter (property = "tibco.home", required = true)
	protected File tibcoHOME;

	@Parameter (property = "executables.extension", required = true)
	protected String executablesExtension;

	@Parameter (property = "project.build.directory", required = true, defaultValue = "${basedir}/target" ) // target
	@_Parameter (property = "project.build.directory", required = true, defaultValue = "${basedir}/target" ) // target
	protected File directory;

	@Parameter (property = "project.output.directory", required = true, defaultValue = "${project.build.directory}/output" ) // target/output (instead of target/classes)
	@_Parameter (property = "project.output.directory", required = true, defaultValue = "${project.build.directory}/output" ) // target/output (instead of target/classes)
	protected File outputDirectory;

	@Parameter (property = "project.test.directory", required = true, defaultValue = "${project.build.directory}/test" ) // target/test
	@_Parameter (property = "project.test.directory", required = true, defaultValue = "${project.build.directory}/test" ) // target/test
	protected File testOutputDirectory;

	@Parameter (property="project.build.sourceEncoding", required=true, defaultValue = "UTF-8")
	@_Parameter (property="project.build.sourceEncoding", required=true, defaultValue = "UTF-8")
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

	/**
	 * <p>
	 * This inner-class extends java.util.Properties with all properties sorted
	 * alphabetically. Also, the setProperty method is overridden to support
	 * multiple input types and check for null values.
	 * </p>
	 *
	 */
	public static class SortedProperties extends Properties {
		private static final long serialVersionUID = 3733070302160913988L;
		
		@Override
	    public synchronized Enumeration<Object> keys() {
	        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
	    }

		@Override
		public synchronized Object setProperty(String key, String value) {
			if (value != null) {
				return super.setProperty(key, value);
			}
			return null;
		}

		public synchronized Object setProperty(String key, BigInteger value) {
			if (value != null) {
				return super.setProperty(key, value.toString());
			}
			return null;
		}

		public synchronized Object setProperty(String key, Boolean value) {
			if (value != null) {
				return super.setProperty(key, value.toString());
			}
			return null;
		}

	}

	/**
	 * <p>
	 * Create the output directory ("target/") if it doesn't exist yet.
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

}
