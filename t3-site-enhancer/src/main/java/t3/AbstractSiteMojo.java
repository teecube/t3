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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public abstract class AbstractSiteMojo extends AbstractMojo {

	@Parameter ( defaultValue = "${session}", readonly = true)
	protected MavenSession session;

	@Parameter ( defaultValue = "${project}", readonly = true)
	protected MavenProject project;

	@Parameter ( defaultValue = "${settings}", readonly = true)
	protected Settings settings;

	@Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Parameter(property = "siteOutputDirectory", defaultValue = "${project.reporting.outputDirectory}")
	protected File outputDirectory;

	@Parameter
	protected List<String> siteProperties;

	@Parameter
	protected List<String> fromRootParentProperties;

	@Parameter
	protected List<String> inOriginalModelProperties;

	@Parameter
	protected List<String> lookInSettingsProperties;

	private static String toCommaSeparatedString(List<String> strings) {
		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(string);
		}
		return sb.toString();
	}

	private static List<File> toFileList(FileSet fileSet) throws IOException {
		File directory = new File(fileSet.getDirectory());
		String includes = toCommaSeparatedString(fileSet.getIncludes());
		String excludes = toCommaSeparatedString(fileSet.getExcludes());
		return FileUtils.getFiles(directory, includes, excludes);
	}

	private List<File> getHTMLFiles() throws IOException {
		FileSet htmlFiles = new FileSet();
		htmlFiles.setDirectory(outputDirectory.getAbsolutePath());

		htmlFiles.addInclude("**/*.html");

		return toFileList(htmlFiles);
	}

	protected String formatHtml(String html) throws MojoExecutionException {
		try {
			InputSource src = new InputSource(new StringReader(html));
			Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
			Boolean keepDeclaration = Boolean.valueOf(html.startsWith("<?xml"));

			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			LSSerializer writer = impl.createLSSerializer();

			writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
			writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

			return writer.writeToString(document);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
    }

	@SuppressWarnings("unchecked")
	protected String getPropertyValueInSettings(String propertyName, Settings settings) {
		List<String> activeProfiles = settings.getActiveProfiles();
		
		getLog().info("profiles: " + activeProfiles.toString());
		
		for (Object _profileWithId : settings.getProfilesAsMap().entrySet()) {
			Entry<String, Profile> profileWithId = (Entry<String, Profile>) _profileWithId;
			getLog().info(profileWithId.getKey());
			if (activeProfiles.contains(profileWithId.getKey())) {
				Profile profile = profileWithId.getValue();
				getLog().info("active:" + profile.getId());

				String value = profile.getProperties().getProperty(propertyName);
				if (value != null) {
					return value;
				}
			}
		}

		return null;
	}

	protected String getPropertyValue(MavenProject mavenProject, String propertyName, boolean lookInSettingsProperties, boolean onlyInOriginalModel) {
		if (mavenProject == null) return null;

		String result = null;

		if (onlyInOriginalModel) {
			result = mavenProject.getOriginalModel().getProperties().getProperty(propertyName);
		} else {
			result = mavenProject.getModel().getProperties().getProperty(propertyName);
		}
		if (lookInSettingsProperties && (result == null || result.isEmpty())) {
			result = getPropertyValueInSettings(propertyName, session.getSettings());
		}

		return result;
	}

	protected String getPropertyValue(String propertyName, boolean onlyInOriginalModel) {
		return getPropertyValue(project, propertyName, true, onlyInOriginalModel);
	}

	protected String getPropertyValue(String propertyName) {
		return getPropertyValue(propertyName, false);
	}

	protected String getRootProjectProperty(MavenProject mavenProject, String propertyName) {
		return mavenProject == null ? "" : (mavenProject.getParent() == null ? getPropertyValue(mavenProject, propertyName, false, false) : getRootProjectProperty(mavenProject.getParent(), propertyName));
	}

	protected String getRootProjectProperty(MavenProject mavenProject, String propertyName, boolean onlyInOriginalModel) {
		return mavenProject == null ? "" : (mavenProject.getParent() == null ? getPropertyValue(mavenProject, propertyName, false, onlyInOriginalModel) : getRootProjectProperty(mavenProject.getParent(), propertyName, onlyInOriginalModel));
	}

	protected String getPropertyValue(String modelPropertyName, boolean propertyInRootProject, boolean onlyInOriginalModel, boolean lookInSettings) {
		String value = null;
		if (lookInSettings) {
			getLog().info("looking in settings: " + modelPropertyName);
			value = getPropertyValueInSettings(modelPropertyName, settings);
			getLog().info("result:" + value + "end");
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

	public abstract void processHTMLFile(File htmlFile) throws Exception;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (outputDirectory == null || !outputDirectory.exists() || !outputDirectory.isDirectory()) {
			return;
		}

		if (siteProperties == null) {
			siteProperties = new ArrayList<String>();
		}
		if (fromRootParentProperties == null) {
			fromRootParentProperties = new ArrayList<String>();
		}
		if (inOriginalModelProperties == null) {
			inOriginalModelProperties = new ArrayList<String>();
		}
		if (lookInSettingsProperties == null) {
			lookInSettingsProperties = new ArrayList<String>();
		}

		try {
			List<File> htmlFiles = getHTMLFiles();

			for (File htmlFile : htmlFiles) {
				getLog().debug(htmlFile.getAbsolutePath());
				processHTMLFile(htmlFile);
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
