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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.jar.JarArchiver;
import org.codehaus.plexus.archiver.jar.ManifestException;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
@Mojo(name="bw6-package", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true)
public class PackageBW6Mojo extends AbstractBW6ArtifactMojo {

	@Requirement
	@Component(role = ArtifactResolver.class)
	protected ArtifactResolver resolver;

	@Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
	protected List<ArtifactRepository> remoteRepositories;

	private MavenArchiver mavenArchiver;

	private JarArchiver jarArchiver;

	private MavenArchiveConfiguration archiveConfiguration;

	private List<File> getModulesJARs() throws MojoExecutionException {
		List<File> result = new ArrayList<File>();

		for (Object a : project.getDependencyArtifacts()) {
			Artifact artifact = (Artifact) a;
			if (isBW6(artifact)) {
				ArtifactResolutionRequest request = new ArtifactResolutionRequest();
				request.setArtifact(artifact);
				request.setLocalRepository(localRepository);
				// TODO: manage remote repositories
//				request.setRemoteRepostories(remoteRepositories);
				/*ArtifactResolutionResult result = */resolver.resolve(request);
				// TODO: check result!

				if (artifact.getFile() == null || !artifact.getFile().exists()) {
					throw new MojoExecutionException(Messages.DEPENDENCY_RESOLUTION_FAILED, new FileNotFoundException());
				}
				getLog().debug(artifact.getFile().getAbsolutePath());
				
				result.add(artifact.getFile());
			}
		}

		return result;
	}

	private File getMetaInfDirectory() {
		File result = new File(projectBasedir, "META-INF");
		if (result == null || !result.exists() || !result.isDirectory()) {
			getLog().error("Unable to find META-INF/ directory");
			return null;
		}
		return result;
	}
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Packaging BW6");

	    jarArchiver = new JarArchiver();
	    mavenArchiver = new MavenArchiver();
	    archiveConfiguration = new MavenArchiveConfiguration();
	    
		File metaInfDirectory = getMetaInfDirectory();
		metaInfDirectory = prepareMetaInf(metaInfDirectory);
//		File appManifest = updateManifest(appManifest);
		File appManifest = null;

		List<File> modulesJARs = getModulesJARs();

		mavenArchiver.setArchiver(jarArchiver);
		mavenArchiver.setOutputFile(getOutputFile());

		// Set the MANIFEST.MF to the JAR Archiver
		jarArchiver.setManifest(appManifest);

		// Set the MANIFEST.MF to the Archive Configuration
		archiveConfiguration.setManifestFile(appManifest);
		archiveConfiguration.setAddMavenDescriptor(true);

		// create the Archive
		try {
			mavenArchiver.createArchive(session, project, archiveConfiguration);
		} catch (ArchiverException | ManifestException | IOException | DependencyResolutionRequiredException e) {
			e.printStackTrace();
		}
	}

	private File updateManifest(File appManifest) {
		// TODO Auto-generated method stub
		return null;
	}

	private File prepareMetaInf(File metaInfDirectory) {
		File manifestFile = null;

		File [] fileList = metaInfDirectory.listFiles();

		for (int i = 0 ; i < fileList.length; i++) {
			// If the File is MANIFEST.MF then the Version needs to be updated in the File and added to the Archiver
			if (fileList[i].getName().indexOf("MANIFEST") != -1) {
//				manifestFile = getUpdatedManifest(fileList[i]);
				manifestFile = fileList[i];
				jarArchiver.addFile(manifestFile, "META-INF/" + fileList[i].getName());
			}

			// If the File is TIBCO.xml then the each Module Version needs to be updated in the File.
			else if (fileList[i].getName().indexOf("TIBCO.xml") != -1) {
//				File tibcoXML = getUpdatedTibcoXML(fileList[i]);
				File tibcoXML = fileList[i];
				jarArchiver.addFile(tibcoXML, "META-INF/" + fileList[i].getName());
			}

			// The substvar files needs to be added as it is.
			else if (fileList[i].getName().indexOf(".substvar") != -1) {
				jarArchiver.addFile(fileList[i], "META-INF/" + fileList[i].getName());
			}

			// The rest of the files can be ignored.
			else {
				continue;
			}
		}

		return metaInfDirectory;
	}

	@Override
	protected String getArtifactFileExtension() {
		return ".ear"; // TODO: externalize
	}

}
