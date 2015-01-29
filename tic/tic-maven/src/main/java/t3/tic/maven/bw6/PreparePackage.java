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
package t3.tic.maven.bw6;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
@Mojo( name="bw6-clean-build-properties", defaultPhase=LifecyclePhase.PREPARE_PACKAGE  )
public class PreparePackage extends AbstractBW6Mojo {

	@Override
	public void execute() throws MojoExecutionException {
		try {
			updateBuildProperties();
		} catch (IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * <p>
	 * This updates the build.properties.
	 * </p>
	 * @throws IOException
	 */
	private void updateBuildProperties() throws IOException {
		Properties properties = new Properties();

		File file = new File(this.projectBasedir, "build.properties");
		InputStream is = new FileInputStream(file);
		properties.load(is);
		is.close();

		String binIncludes = (String) properties.get("bin.includes");
		List<String> includes = new ArrayList<String>();
		includes.addAll(Arrays.asList(binIncludes.split(",")));
		for (Iterator<String> it = includes.iterator(); it.hasNext();) {
			String include = it.next();
			if (include.startsWith("target/")) {
				it.remove();
			}
		}
		binIncludes = StringUtils.join(includes, ",");

		properties.put("bin.includes", binIncludes);

		properties.store(new FileOutputStream(file), null);
	}

}
