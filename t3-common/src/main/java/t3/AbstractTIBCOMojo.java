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

import static org.apache.commons.io.FileUtils.copyFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;

import t3.plugin.annotations.GlobalParameter;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
public class AbstractTIBCOMojo extends AbstractCommonMojo {

	@GlobalParameter (property = CommonMojoInformation.tibcoHome, required = true, description = CommonMojoInformation.tibcoHome_description, category = CommonMojoInformation.tibcoCategory, valueGuessedByDefault = false)
	protected File tibcoHOME;

	protected void prepareTRAs(List<File> tras, HashMap<File, File> trasMap) throws IOException {
		// to be overridden in child classes
	}

	protected int executeTIBCOBinary(File binary, List<File> tras, ArrayList<String> arguments, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
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
	protected int executeTIBCOBinary(File binary, List<File> tras, ArrayList<String> arguments, File workingDirectory, String errorMsg) throws IOException, MojoExecutionException {
		return executeTIBCOBinary(binary, tras, arguments, workingDirectory, errorMsg, false, true);
	}

}
