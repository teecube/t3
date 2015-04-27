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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;

/**
 * <p>
 * This inner-class extends java.util.Properties with all properties sorted
 * alphabetically. Also, the setProperty method is overridden to support
 * multiple input types and check for null values.
 * </p>
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class SortedProperties extends Properties {
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

    /**
     * <p>
     * This loads a properties file into a java.util.Properties object with
     * sorted keys.<br/><br/>
     *
     *  <b>NB</b>: always use keys() method to browse the properties
     * </p>
     *
     * @param propertiesFile
     * @return
     * @throws IOException
     */
	public static Properties loadPropertiesFile(File propertiesFile, String encoding) throws IOException {
		Properties properties = new SortedProperties();

		FileInputStream fileInputStream = new FileInputStream(propertiesFile);
		Reader reader = new InputStreamReader(fileInputStream, encoding);
		properties.load(reader);

		return properties;
	}

	/**
	 * <p>
	 * This saves a java.util.Properties to a file.<br />
	 *
	 * It is possible to add a comment at the beginning of the file.
	 * </p>
	 *
	 * @param outputFile, the File where to output the Properties
	 * @param properties, the Properties to save
	 * @param propertiesComment, the comment to add at the beginning of the file
	 * @param success, the success message
	 * @param failure, the failure message
	 * @throws MojoExecutionException
	 */
	public static void savePropertiesToFile(File outputFile, Properties properties, String encoding,
										String propertiesComment, String success, String failure, Boolean filterProperties,
										AbstractCommonMojo mojo)
										throws MojoExecutionException {
		OutputStream outputStream = null;

		try {
			outputFile.getParentFile().mkdirs();
			outputStream = new FileOutputStream(outputFile);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, encoding);
			properties.store(outputStreamWriter, propertiesComment);

			if (filterProperties) {
				mojo.getLog().debug("Filtering properties files");

				File tmpDir = new File(outputFile.getParentFile(), "tmp");
				tmpDir.mkdir();
				List<Resource> resources = new ArrayList<Resource>();
				Resource r = new Resource();
				r.setDirectory(outputFile.getParentFile().getAbsolutePath());
				r.addInclude("*.properties");
				r.setFiltering(true);
				resources.add(r);

				List<String> filters = new ArrayList<String>();
				List<String> nonFilteredFileExtensions = new ArrayList<String>();

				MavenResourcesExecution mre = new MavenResourcesExecution(resources, tmpDir, mojo.project, mojo.sourceEncoding, filters, nonFilteredFileExtensions, mojo.session);
				mojo.mavenResourcesFiltering.filterResources(mre);

				FileUtils.copyDirectory(tmpDir, outputFile.getParentFile());
				FileUtils.deleteDirectory(tmpDir);
			}

			mojo.getLog().info(success + " '" + outputFile + "'");
		} catch (Exception e) {
			throw new MojoExecutionException(failure + " '" + outputFile + "'", e);
		} finally {
			try {
				outputStream.close();
			} catch (Exception e) {
			}
		}
	}

	/**
     * <p>
     * This method sorts a {@link Properties} object.
     * </p>
     *
	 * @param properties
	 * @return
	 */
	public static Properties sortProperties(Properties properties) {
		Properties sp = new SortedProperties();
		sp.putAll(properties);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			sp.store(baos, null);
			properties.clear();
			properties.load(new ByteArrayInputStream(baos.toByteArray()));
		} catch (IOException e) {
			// was not sorted
		}

		return properties;
	}

}
