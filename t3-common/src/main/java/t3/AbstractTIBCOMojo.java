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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.apache.commons.exec.launcher.CommandLauncher;
import org.apache.commons.exec.launcher.CommandLauncherFactory;
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

	protected int executeTIBCOBinary(File binary, List<File> tras, ArrayList<String> arguments, File workingDir, String errorMsg, boolean fork, boolean synchronous) throws IOException, MojoExecutionException {
		Integer result = 0;

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

		CommandLine cmdLine = new CommandLine(binary);

		for (String argument : arguments) {
			cmdLine.addArgument(argument);
		}
		getLog().debug("command line : " + cmdLine.toString());
		getLog().debug("working directory : " + workingDir);

		DefaultExecutor executor = new DefaultExecutor();
		executor.setWorkingDirectory(workingDir);

		if (getTimeOut() > 0) {
			ExecuteWatchdog watchdog = new ExecuteWatchdog(getTimeOut() * 1000);
			executor.setWatchdog(watchdog);
		}

		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());

		ByteArrayOutputStream stdOutAndErr = new ByteArrayOutputStream();
		executor.setStreamHandler(new PumpStreamHandler(stdOutAndErr));

		getLog().info(Messages.MESSAGE_EMPTY_PREFIX + cmdLine.toString());

		if (fork) {
			CommandLauncher commandLauncher = CommandLauncherFactory.createVMLauncher();
			commandLauncher.exec(cmdLine, null, workingDir);
		} else {
			try {
				if (synchronous) {
					result = executor.execute(cmdLine);
				} else {
					executor.execute(cmdLine, new DefaultExecuteResultHandler());
				}
			} catch (ExecuteException e) {
				// TODO manage default errors
				getLog().info(cmdLine.toString());
				getLog().info(stdOutAndErr.toString());
				getLog().info(result.toString());
				throw new MojoExecutionException(errorMsg, e);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}

		return result;
	}

	protected int getTimeOut() {
		// to be overridden in child classes
		return 120; // 2 minutes
	}

	protected void prepareTRAs(List<File> tras, HashMap<File, File> trasMap) throws IOException {
		// to be overridden in child classes
	}

	/**
	 * <p>
	 * Default behaviour is synchronous and no fork.
	 * </p>
	 */
	protected void executeTIBCOBinary(File binary, List<File> tras, ArrayList<String> arguments, File workingDirectory, String errorMsg) throws IOException, MojoExecutionException {
		executeTIBCOBinary(binary, tras, arguments, workingDirectory, errorMsg, false, true);
	}

}
