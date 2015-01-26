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

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.tycho.core.maven.TychoMavenLifecycleParticipant;

import t3.tic.maven.prepare.EclipsePluginConvertor;

/**
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "BW6LifecycleListener")
public class BW6LifecycleParticipant extends TychoMavenLifecycleParticipant {

	@Requirement
	private Logger logger;

	@Requirement
	private ArtifactRepositoryFactory artifactRepositoryFactory;

	@Override
	public void afterProjectsRead(MavenSession session)	throws MavenExecutionException {
		List<MavenProject> projects = session.getProjects();

		EclipsePluginConvertor convertor = new EclipsePluginConvertor(logger);
		convertor.setArtifactRepositoryRepository(artifactRepositoryFactory);

		for (MavenProject mavenProject : projects) {
			convertor.setMavenProject(mavenProject);
			if ("bw6-app-module".equals(mavenProject.getPackaging())) {
				try {
					convertor.prepareBW6AppModule();
				} catch (Exception e) {
					throw new MavenExecutionException(e.getLocalizedMessage(), e);
				}
			}
		}

		super.afterProjectsRead(session);
		logger.info("");
		logger.info("~-> TIC is loaded.");

        session.getUserProperties().put("tycho.mode", "maven"); // to avoid duplicate call of TychoMavenLifecycleParticipant.afterProjectsRead()
	}
}