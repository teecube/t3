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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Requirement;

import t3.tic.maven.Messages;
import t3.tic.maven.bw6.AbstractBW6ProjectMojo;
import t3.tic.maven.bw6.BW6Utils;

/**
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
public abstract class AbstractBW6ApplicationMojo extends AbstractBW6ProjectMojo {

	@Parameter (property = "tibco.bw6.tibco.xml", defaultValue = "${project.build.directory}/META-INF/TIBCO.xml", required = true)
	protected File tibcoXML;

	@Parameter (property = "tibco.bw6.tibco.xml.source", defaultValue = "${basedir}/META-INF/TIBCO.xml", required = true)
	protected File tibcoXMLSource;

	@Requirement
	@Component(role = ArtifactResolver.class)
	protected ArtifactResolver resolver;

	@Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
	protected List<ArtifactRepository> remoteRepositories;

	protected String getUnqualifiedVersion() {
		return project.getProperties().getProperty("unqualifiedVersion");
	}

	protected List<File> getModulesJARs() throws MojoExecutionException {
		List<File> result = new ArrayList<File>();

		for (Object a : project.getDependencyArtifacts()) {
			Artifact artifact = (Artifact) a;
			if (BW6Utils.isBW6(artifact)) {
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

	private Manifest getModuleManifest(File moduleFile) throws IOException {
		JarInputStream jarStream = new JarInputStream(new FileInputStream(moduleFile));
		Manifest moduleManifest = jarStream.getManifest();
		jarStream.close();

		return moduleManifest;
	}

	private String getManifesSymbolicName(Manifest manifest) throws FileNotFoundException, IOException {
		return manifest.getMainAttributes().getValue("Bundle-SymbolicName");
	}

	private String getManifestVersion(Manifest manifest) throws FileNotFoundException, IOException {
		return manifest.getMainAttributes().getValue("Bundle-Version");
	}

	protected Map<String, String> getModulesVersions() throws MojoExecutionException {
		Map<String, String> result = new HashMap<String, String>();

		List<File> modulesJARs = getModulesJARs();

		for (File moduleJAR : modulesJARs) {
            String version;
            String symbolicName;
			try {
				Manifest manifest_ = getModuleManifest(moduleJAR);
				symbolicName = getManifesSymbolicName(manifest_);
				version = getManifestVersion(manifest_);

				result.put(symbolicName, version);
			} catch (IOException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}

		}

		return result;
	}

}
