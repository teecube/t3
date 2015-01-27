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

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import java.io.File;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.tycho.core.maven.TychoMavenLifecycleParticipant;

import t3.tic.maven.prepare.EclipsePluginConvertor;
import t3.tic.maven.prepare.PluginBuilder;

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

	@Requirement
	protected BuildPluginManager pluginManager;

	private String tibcoHome;

	private String bw6Version;

	@Override
	public void afterProjectsRead(MavenSession session)	throws MavenExecutionException {
		enforceProperties(session);
		convertProjects(session.getProjects());

		super.afterProjectsRead(session);
		logger.info("");
		logger.info("~-> TIC is loaded.");

		session.getUserProperties().put("tycho.mode", "maven"); // to avoid duplicate call of TychoMavenLifecycleParticipant.afterProjectsRead()
	}

	/**
	 * <p>
	 *
	 * </p>
	 *
	 * @param projects
	 * @throws MavenExecutionException
	 */
	private void convertProjects(List<MavenProject> projects) throws MavenExecutionException {
		if (projects == null) {
			logger.warn("No projects to convert.");
			return;
		}

		EclipsePluginConvertor convertor = new EclipsePluginConvertor(logger);
		convertor.setArtifactRepositoryRepository(artifactRepositoryFactory);
		convertor.setTIBCOHome(this.tibcoHome);
		convertor.setBW6Version(this.bw6Version);

		for (MavenProject mavenProject : projects) {
			convertor.setMavenProject(mavenProject);
			try {
				if ("bw6-app-module".equals(mavenProject.getPackaging())) {
					convertor.prepareBW6AppModule();
				} else if ("bw6-shared-module".equals(mavenProject.getPackaging())) {
					convertor.prepareBW6SharedModule();
				}
			} catch (Exception e) {
				throw new MavenExecutionException(e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 * <p>
	 * 	The plugin will enforce custom rules before the actual build begins.
	 * </p>
	 *
	 * @param session
	 * @throws MavenExecutionException
	 */
	private void enforceProperties(MavenSession session) throws MavenExecutionException {
		logger.info(Messages.MESSAGE_SPACE);
		logger.info(Messages.ENFORCING_RULES);
		logger.info(Messages.MESSAGE_SPACE);

		session.getCurrentProject().getModel().addProperty("tibco.bw6.p2repository", "${tibco.home}/bw/${tibco.bw6.version}/maven/p2repo");

		File file = new File("plugins-configuration/org.apache.maven.plugins/maven-enforcer-plugin.xml");
		String artifactId = file.getName().replace(".xml", "");
		String groupId = file.getParentFile().getName();

		PluginBuilder pluginBuilder = new PluginBuilder(groupId, artifactId);
		try {
			pluginBuilder.addConfigurationFromClasspath();

			Plugin enforcerPlugin = pluginBuilder.getPlugin();
			Xpp3Dom configuration = (Xpp3Dom) enforcerPlugin.getConfiguration();

			executeMojo(
				enforcerPlugin,
				"enforce",
				configuration,
				executionEnvironment(session.getCurrentProject(), session, pluginManager)
			);
		} catch (MojoExecutionException e) {
			logger.fatalError(Messages.ENFORCER_RULES_FAILURE);
			logger.fatalError(Messages.MESSAGE_SPACE);
			throw new MavenExecutionException(e.getLocalizedMessage(), e);
		}

		this.tibcoHome = session.getCurrentProject().getModel().getProperties().getProperty("tibco.home");
		this.bw6Version = session.getCurrentProject().getModel().getProperties().getProperty("tibco.bw6.version");
	}
}