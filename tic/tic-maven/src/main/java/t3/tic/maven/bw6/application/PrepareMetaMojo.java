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
package t3.tic.maven.bw6.application;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import t3.tic.maven.bw6.BW6Utils;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
@Mojo(name="prepare-meta", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresProject = true)
public class PrepareMetaMojo extends AbstractBW6ApplicationMojo {

	private void updatedManifest(String version) throws IOException {
		FileInputStream is = new FileInputStream(manifest);
		Manifest mf = new Manifest(is);
		is.close();

		// update the Bundle-Version
		Attributes attr = mf.getMainAttributes();
		attr.putValue("Bundle-Version", version);

		//Write the updated file and return the same.
		FileOutputStream os = new FileOutputStream(manifest);
		mf.write(os);
		os.close();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		FileFilter filters = new FileFilter() {
			@Override
			public boolean accept(File file) {
				String name = file.getName().toUpperCase();
				String extension = FilenameUtils.getExtension(file.getAbsolutePath());

				return "MANIFEST.MF".equals(name) ||
						"TIBCO.XML".equals(name) ||
						"substvar".equals(extension);
			}
		};
		try {
			FileUtils.copyDirectory(metaInfSource, metaInf, filters);

			Map<String, String> versions = getModulesVersions();
			if (versions.size() > 0) {
				updatedManifest((String) versions.values().toArray()[0]);
				BW6Utils.updateTIBCOXMLVersion(tibcoXML, versions);
			}
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

}
