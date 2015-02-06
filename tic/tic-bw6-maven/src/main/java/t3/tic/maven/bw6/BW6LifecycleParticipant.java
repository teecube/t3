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

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.tycho.core.maven.TychoMavenLifecycleParticipant;

import t3.PluginBuilder;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "TICBW6LifecycleParticipant")
public class BW6LifecycleParticipant extends TychoMavenLifecycleParticipant {

	@Requirement
	private Logger logger;

	@Requirement
	private ArtifactRepositoryFactory artifactRepositoryFactory;

	@Requirement
	protected BuildPluginManager pluginManager;

	@Requirement
	protected ProjectBuilder projectBuilder;

	private String tibcoHome;

	private String bw6Version;

	@Override
	public void afterProjectsRead(MavenSession session)	throws MavenExecutionException {
		enforceProperties(session); // check that all mandatory properties are correct
		List<MavenProject> projects = convertProjects(session.getProjects(), session.getProjectBuildingRequest());
		session.setProjects(projects);

		logger.info(Messages.RESOLVING_BW6_DEPENDENCIES);
		logger.info(Messages.MESSAGE_SPACE);
		super.afterProjectsRead(session);
		logger.info(Messages.MESSAGE_SPACE);
		logger.info(Messages.RESOLVED_BW6_DEPENDENCIES);

		logger.info(Messages.MESSAGE_SPACE);
		logger.info(Messages.LOADED);

		session.getUserProperties().put("tycho.mode", "maven"); // to avoid duplicate call of TychoMavenLifecycleParticipant.afterProjectsRead()
	}

	/**
	 * <p>
	 *
	 * </p>
	 *
	 * @param projects
	 * @param projectBuildingRequest
	 * @throws MavenExecutionException
	 */
	private List<MavenProject> convertProjects(List<MavenProject> projects, ProjectBuildingRequest projectBuildingRequest) throws MavenExecutionException {
		List<MavenProject> result = new ArrayList<MavenProject>();

		if (projects == null) {
			logger.warn("No projects to convert.");
			return result;
		}

		BW6PackagingConvertor convertor = new BW6PackagingConvertor(logger);
		convertor.setBW6Version(this.bw6Version);
		convertor.setTIBCOHome(this.tibcoHome);
		convertor.setArtifactRepositoryRepository(artifactRepositoryFactory);
		convertor.setProjectBuilder(projectBuilder);
		convertor.setProjectBuildingRequest(projectBuildingRequest);

		for (MavenProject mavenProject : projects) {
			convertor.setMavenProject(mavenProject);
			try {
				switch (mavenProject.getPackaging()) {
				case "bw6-app-module":
					result.add(convertor.prepareBW6AppModule());
					break;
				case "bw6-shared-module":
					result.add(convertor.prepareBW6SharedModule());
					break;
				case "pom":
					if (convertor.hasBW6ModuleOrDependency()) {
						// a BW6 application has a "pom" packaging and at least one module or dependency with "bw6-app-module" packaging
						result.add(convertor.prepareBW6Application());
					}
					break;
				default:
					logger.debug("No conversion for : " + mavenProject.getName());
					break;
				}
			} catch (Exception e) {
				throw new MavenExecutionException(e.getLocalizedMessage(), e);
			}
		}

		return result;
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

		logger.info(Messages.ENFORCED_RULES);
		logger.info(Messages.MESSAGE_SPACE);
	}
}