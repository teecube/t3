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

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
@Mojo(name="bw6-package", defaultPhase = LifecyclePhase.PACKAGE)
public class PackageBW6Mojo extends AbstractBW6Mojo {

	@Requirement
	@Component(role = ArtifactResolver.class)
	protected ArtifactResolver resolver;

	@Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true, required = true)
	protected List<ArtifactRepository> remoteRepositories;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Packaging BW6");

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
				getLog().info(artifact.getFile().getAbsolutePath());
			}
		}
	}

}
