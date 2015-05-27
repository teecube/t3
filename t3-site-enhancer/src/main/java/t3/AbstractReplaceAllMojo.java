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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

public abstract class AbstractReplaceAllMojo extends AbstractSiteMojo {

	public abstract void processHTMLFile(File htmlFile) throws Exception;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

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
