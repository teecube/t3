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
package t3.tic.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
public abstract class AbstractBW6ArtifactMojo extends AbstractBW6Mojo {

	@Parameter(property = "project.build.classifier")
	protected String classifier;

    /**
     * Name of the generated artifact (without file extension).
     */
    @Parameter(property = "project.build.finalName", required = true)
    protected String finalName;

    protected abstract String getArtifactFileExtension(); // abstract because it can be ".ear", ".projlib", ".xml", ".pom" ...

	/**
	 * Retrieves the full path of the artifact that will be created.
	 * 
	 * @param basedir, the directory where the artifact will be created
	 * @param finalName, the name of the artifact, without file extension
	 * @param classifier
	 * @return a {@link File} object with the path of the artifact
	 */
	protected File getArtifactFile(File basedir, String finalName, String classifier) {
		if (classifier == null) {
			classifier = "";
		} else if (classifier.trim().length() > 0 && !classifier.startsWith("-")) {
			classifier = "-" + classifier;
		}

		return new File(basedir, finalName + classifier + getArtifactFileExtension());
	}
	
    /**
     * @return the Maven artefact as a {@link File}
     */
    protected File getOutputFile() {
        return getArtifactFile(outputDirectory, finalName, classifier);
    }

}
