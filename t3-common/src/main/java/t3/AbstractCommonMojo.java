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
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import t3.plugin.parameters.GlobalParameter;

public class AbstractCommonMojo extends AbstractMojo {

	@GlobalParameter (property = "executables.extension", required = true, category = CommonMojoInformation.systemCategory)
	protected String executablesExtension;

	@GlobalParameter (property = "project.build.directory", defaultValue = "${basedir}/target", category = CommonMojoInformation.mavenCategory) // target
	protected File directory;

	@GlobalParameter (property = "project.output.directory", defaultValue = "${project.build.directory}/output", category = CommonMojoInformation.mavenCategory) // target/output (instead of target/classes)
	protected File outputDirectory;

	@GlobalParameter (property = "project.test.directory", defaultValue = "${project.build.directory}/test", category = CommonMojoInformation.mavenCategory) // target/test
	protected File testOutputDirectory;

	@GlobalParameter (property="project.build.sourceEncoding", defaultValue = "UTF-8", category = CommonMojoInformation.mavenCategory)
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
	private Settings settings;

	public static final Pattern mavenPropertyPattern = Pattern.compile("\\$\\{([^}]*)\\}"); // ${prop} regex pattern

	@Component
	protected ProjectBuilder builder;

	@Component (role=org.apache.maven.shared.filtering.MavenResourcesFiltering.class, hint="default")
	protected MavenResourcesFiltering mavenResourcesFiltering;

	@Component (role = BuildPluginManager.class)
	protected BuildPluginManager pluginManager;

	/**
	 * <p>
	 * Instantiate a minimalistic {@link AbstractCommonMojo} to use properties
	 * management as a standalone object.
	 * </p>
	 *
	 * @param session
	 * @param mavenProject
	 * @return
	 */
	public static AbstractCommonMojo propertiesManager(MavenSession session, MavenProject mavenProject) {
		AbstractCommonMojo mojo = new AbstractCommonMojo();
		mojo.setProject(mavenProject);
		mojo.setSession(session);
		mojo.setSettings(session.getSettings());

		return mojo;
	}

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

	// properties
	@SuppressWarnings("unchecked") // because of Maven poor typing
	public String getPropertyValueInSettings(String propertyName, Settings settings) {
		if (settings == null) {
			return null;
		}

		List<String> activeProfiles = settings.getActiveProfiles();

		for (Object _profileWithId : settings.getProfilesAsMap().entrySet()) {
			Entry<String, Profile> profileWithId = (Entry<String, Profile>) _profileWithId;
			if (activeProfiles.contains(profileWithId.getKey())) {
				Profile profile = profileWithId.getValue();

				String value = profile.getProperties().getProperty(propertyName);
				if (value != null) {
					return value;
				}
			}
		}

		return null;
	}

	private List<String> getActiveProfiles(Settings settings) {
		if (settings == null) return null;

		List<String> result = settings.getActiveProfiles();

		for (Profile profile : settings.getProfiles()) {
			if (profile.getActivation().isActiveByDefault() && !result.contains(profile.getId())) {
				result.add(profile.getId());
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked") // because of Maven poor typing
	public boolean propertyExistsInSettings(String propertyName, Settings settings) {
		if (settings == null) {
			return false;
		}

		List<String> activeProfiles = getActiveProfiles(settings);

		for (Object _profileWithId : settings.getProfilesAsMap().entrySet()) {
			Entry<String, Profile> profileWithId = (Entry<String, Profile>) _profileWithId;
			if (activeProfiles.contains(profileWithId.getKey())) {
				Profile profile = profileWithId.getValue();

				boolean result = profile.getProperties().containsKey(propertyName);
				if (result) {
					return result;
				}
			}
		}

		return false;
	}

	private String getPropertyValueInCommandLine(String propertyName, MavenSession session) {
		if (session == null) {
			return null;
		}

		return session.getRequest().getUserProperties().getProperty(propertyName);
	}

	public boolean propertyExistsInSettings(String propertyName) {
		return propertyExistsInSettings(propertyName, session.getSettings());
	}

	public boolean propertyExists(String propertyName) {
		return propertyExists(project, propertyName);
	}

	public boolean propertyExists(MavenProject mavenProject, String propertyName) {
		return mavenProject.getOriginalModel().getProperties().containsKey(propertyName) ||
			   mavenProject.getModel().getProperties().containsKey(propertyName) ||
			   session.getRequest().getUserProperties().containsKey(propertyName) ||
			   propertyExistsInSettings(propertyName, session.getSettings());
	}

	/**
	 * <p>
	 *
	 * </p>
	 *
	 * @param mavenProject
	 * @param propertyName
	 * @param lookInSettingsProperties
	 * @param lookInCommandLine
	 * @param onlyInOriginalModel
	 * @return
	 */
	public String getPropertyValue(MavenProject mavenProject, String propertyName, boolean lookInSettingsProperties, boolean lookInCommandLine, boolean onlyInOriginalModel) {
		if (mavenProject == null) return null;

		String result = null;

		if (onlyInOriginalModel) {
			result = mavenProject.getOriginalModel().getProperties().getProperty(propertyName);
		} else {
			result = mavenProject.getModel().getProperties().getProperty(propertyName);
		}
		if (lookInCommandLine && (result == null || result.isEmpty())) {			
			result = getPropertyValueInCommandLine(propertyName, session);
		}
		if (lookInSettingsProperties && (result == null || result.isEmpty())) {
			result = getPropertyValueInSettings(propertyName, settings);
		}

		return result;
	}

	public String getPropertyValue(String propertyName, boolean onlyInOriginalModel) {
		return getPropertyValue(project, propertyName, true, true, onlyInOriginalModel);
	}

	public String getPropertyValue(String propertyName) {
		return getPropertyValue(propertyName, false);
	}

	public String getRootProjectProperty(MavenProject mavenProject, String propertyName) {
		return mavenProject == null ? "" : (mavenProject.getParent() == null ? getPropertyValue(mavenProject, propertyName, false, false, false) : getRootProjectProperty(mavenProject.getParent(), propertyName));
	}

	public String getRootProjectProperty(MavenProject mavenProject, String propertyName, boolean onlyInOriginalModel) {
		return mavenProject == null ? "" : (mavenProject.getParent() == null ? getPropertyValue(mavenProject, propertyName, false, false, onlyInOriginalModel) : getRootProjectProperty(mavenProject.getParent(), propertyName, onlyInOriginalModel));
	}

	public String getPropertyValue(String modelPropertyName, boolean propertyInRootProject, boolean onlyInOriginalModel, boolean lookInSettings) {
		String value = null;
		if (lookInSettings) {
			value = getPropertyValueInSettings(modelPropertyName, settings);
		}
		if (value == null) {
			if (propertyInRootProject) {
				value = getRootProjectProperty(project, modelPropertyName, onlyInOriginalModel);
			} else {
				value = getPropertyValue(modelPropertyName, onlyInOriginalModel);
			}
		}
		return value;
	}

	public String replaceProperties(String string) {
		if (string == null) return null;

		Matcher m = mavenPropertyPattern.matcher(string);

		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String propertyName = m.group(1);
			String propertyValue = getPropertyValue(propertyName);
			if (propertyValue != null) {
			    m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
			}
		}
		m.appendTail(sb);
		string = sb.toString();

		return string;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException  {
		createOutputDirectory();
	}

	public void setSession(MavenSession session) {
		this.session = session;
	}

	public void setProject(MavenProject project) {
		this.project = project;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

}
