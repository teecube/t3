/**
 * (C) Copyright 2016-2016 teecube
 * (http://teecu.be) and others.
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

import static org.apache.commons.io.FileUtils.copyFile;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;

import t3.plugin.PropertiesEnforcer;
import t3.plugin.annotations.GlobalParameter;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class CommonTIBCOMojo extends CommonMojo {

	@GlobalParameter (property = CommonMojoInformation.tibcoHome, required = true, description = CommonMojoInformation.tibcoHome_description, category = CommonMojoInformation.tibcoCategory, valueGuessedByDefault = false)
	protected File tibcoHOME;

	@GlobalParameter (property = CommonMojoInformation.jreHome, defaultValue = CommonMojoInformation.jreHome_default, description = CommonMojoInformation.jreHome_description, required = true, category = CommonMojoInformation.tibcoCategory)
	protected File tibcoJREHome;

	@GlobalParameter (property = CommonMojoInformation.jreVersion, description = CommonMojoInformation.jreVersion_description, category = CommonMojoInformation.tibcoCategory)
	protected File tibcoJREVersion;

	@GlobalParameter (property = CommonMojoInformation.jre64Home, defaultValue = CommonMojoInformation.jre64Home_default, description = CommonMojoInformation.jre64Home_description, required = true, category = CommonMojoInformation.tibcoCategory)
	protected File tibcoJRE64Home;

	@GlobalParameter (property = CommonMojoInformation.jre64Version, description = CommonMojoInformation.jre64Version_description, category = CommonMojoInformation.tibcoCategory)
	protected File tibcoJRE64Version;

	public CommonTIBCOMojo() {}

	public CommonTIBCOMojo(CommonMojo mojo) {
		super(mojo);
	}

	protected void prepareTRAs(List<File> tras, HashMap<File, File> trasMap) throws IOException {
		// to be overridden in child classes
	}

	public static void setJreVersions(MavenSession session, CommonMojo propertiesManager) {
		String tibcoHome = propertiesManager.getPropertyValue(CommonMojoInformation.tibcoHome);
		File tibcoJre = new File(tibcoHome, "tibcojre");
		File tibcoJre64 = new File(tibcoHome, "tibcojre64");

		FileFilter jreFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() && file.getName().matches("\\d\\.\\d\\.\\d"); // is directory matching "x.y.z" pattern (such as "1.7.0", "1.8.0" and so on)
			}
		};

		if (tibcoJre.exists()) {
			File[] jres = tibcoJre.listFiles(jreFilter);
			if (jres.length > 0) {
				PropertiesEnforcer.setCustomProperty(session, CommonMojoInformation.jreVersion, jres[0].getName());
			}
		}

		if (tibcoJre64.exists()) {
			File[] jres64 = tibcoJre64.listFiles(jreFilter);
			if (jres64.length > 0) {
				PropertiesEnforcer.setCustomProperty(session, CommonMojoInformation.jre64Version, jres64[0].getName());
			}
		}

	}

	protected int executeTIBCOBinary(File binary, List<File> tras, List<String> arguments, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
		if (tras == null) { // no value specified as Mojo parameter, we use the .tra in the same directory as the binary
			String traPathFileName = binary.getAbsolutePath();
			traPathFileName = FilenameUtils.removeExtension(traPathFileName);
			traPathFileName += ".tra";
			tras = new ArrayList<File>();
			tras.add(new File(traPathFileName));
		}

		HashMap<File, File> trasMap = new HashMap<File, File>();
		for (File tra : tras) {
			// copy of ".tra" file in the working directory
			File tmpTRAFile = new File(directory, tra.getName());
			trasMap.put(tra, tmpTRAFile);
			copyFile(tra, tmpTRAFile);
		}

		prepareTRAs(tras, trasMap);

		return super.executeBinary(binary, arguments, workingDir, errorMsg, fork, synchronous);
	}

	/**
	 * <p>
	 * Default behaviour is synchronous and no fork.
	 * </p>
	 */
	protected int executeTIBCOBinary(File binary, List<File> tras, List<String> arguments, File workingDirectory, String errorMsg) throws IOException, MojoExecutionException {
		return executeTIBCOBinary(binary, tras, arguments, workingDirectory, errorMsg, false, true);
	}

}
