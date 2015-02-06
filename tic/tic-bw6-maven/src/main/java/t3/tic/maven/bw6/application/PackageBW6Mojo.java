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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.ManifestException;

import t3.tic.maven.bw6.AbstractBW6ArtifactMojo;
import t3.tic.maven.bw6.Messages;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
@Mojo(name="bw6-package", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class PackageBW6Mojo extends AbstractBW6ApplicationMojo implements AbstractBW6ArtifactMojo {

	private JarArchiver jarArchiver;
	private MavenArchiver mavenArchiver;
	private MavenArchiveConfiguration archiveConfiguration;

// TODO: mutualize "artifact" information
	@Parameter(property = "project.build.classifier")
	protected String classifier;

	/**
	 * Name of the generated artifact (without file extension).
	 */
	@Parameter(property = "project.build.finalName", required = true)
	protected String finalName;

	@Override
	public String getArtifactFileExtension() {
		return ".ear"; // TODO: externalize
	}

	/**
	 * Retrieves the full path of the artifact that will be created.
	 *
	 * @param basedir, the directory where the artifact will be created
	 * @param finalName, the name of the artifact, without file extension
	 * @param classifier
	 * @return a {@link File} object with the path of the artifact
	 */
	public File getArtifactFile(File basedir, String finalName,	String classifier) {
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
	public File getOutputFile() {
		return getArtifactFile(outputDirectory, finalName, classifier);
	}
//

	private void addApplication() throws ArchiverException, ManifestException, IOException, DependencyResolutionRequiredException, MojoExecutionException {
		jarArchiver.addDirectory(metaInf, metaInf.getName() + File.separator); // add "target/META-INF/", already prepared by "prepare-meta"

		mavenArchiver.setArchiver(jarArchiver);
		mavenArchiver.setOutputFile(getOutputFile());

		// Set the MANIFEST.MF to the JAR Archiver
		jarArchiver.setManifest(manifest);

		// Set the MANIFEST.MF to the Archive Configuration
		archiveConfiguration.setManifestFile(manifest);
		archiveConfiguration.setAddMavenDescriptor(true);

		// create the Archive
		mavenArchiver.createArchive(session, project, archiveConfiguration);
	}

	private void addModules() throws MojoExecutionException, FileNotFoundException, IOException {
		List<File> modulesJARs = getModulesJARs();

		for (File moduleJAR : modulesJARs) {
            // add the JAR file of the module to the EAR file
			getLog().info(Messages.APPLICATION_ADDING_MODULE + moduleJAR.getName() + ".");
			jarArchiver.addFile(moduleJAR, moduleJAR.getName());
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info(Messages.APPLICATION_PACKAGING);

		jarArchiver = new JarArchiver();
		mavenArchiver = new MavenArchiver();
		archiveConfiguration = new MavenArchiveConfiguration();

		try {
			addModules();
			addApplication();
		} catch (ArchiverException | ManifestException | IOException | DependencyResolutionRequiredException e) {
			e.printStackTrace();
		}
	}

}
