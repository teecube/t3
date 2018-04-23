/**
 * (C) Copyright 2016-2018 teecube
 * (http://teecu.be) and others.
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
package t3;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.classrealm.ClassRealmManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import t3.plugin.PluginConfigurator;
import t3.plugin.PropertiesEnforcer;

import java.io.File;

public abstract class CommonMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant implements AdvancedMavenLifecycleParticipant {

    @Requirement
    protected ArtifactHandler artifactHandler;

    @Requirement
    protected ArtifactRepositoryFactory artifactRepositoryFactory;

    @Requirement
    protected ArtifactResolver artifactResolver;

    @Requirement
    protected Logger logger;

    @Requirement
    protected PlexusContainer plexus;

    @Requirement
    protected BuildPluginManager pluginManager;

    @Requirement
    protected ProjectBuilder projectBuilder;

    protected CommonMojo propertiesManager;

    @Requirement
    private ClassRealmManager classRealmManager;

    @Override
    public void setArtifactHandler(ArtifactHandler artifactHandler) {
        this.artifactHandler = artifactHandler;
    }

    @Override
    public void setArtifactRepositoryFactory(ArtifactRepositoryFactory artifactRepositoryFactory) {
        this.artifactRepositoryFactory = artifactRepositoryFactory;
    }

    @Override
    public void setArtifactResolver(ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    @Override
    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void setPlexus(PlexusContainer plexus) {
        this.plexus = plexus;
    }

    @Override
    public void setPluginManager(BuildPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    @Override
    public void setProjectBuilder(ProjectBuilder projectBuilder) {
        this.projectBuilder = projectBuilder;
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        fixStandalonePOM(session.getCurrentProject(), new File(session.getRequest().getBaseDirectory()));
        PropertiesEnforcer.setPlatformSpecificProperties(session);

        // plugin manager and properties manager
        propertiesManager = CommonMojo.propertiesManager(session, session.getCurrentProject());
        PluginConfigurator.propertiesManager = propertiesManager;

        initProjects(session);

        String loadedMessage = loadedMessage();
        if (StringUtils.isNotEmpty(loadedMessage)) {
            logger.info(loadedMessage);
        }

        session.getUserProperties().put(CommonMojo.mojoInitialized, "true");
    }

    protected abstract String getPluginGroupId();
    protected abstract String getPluginArtifactId();
    protected abstract String loadedMessage();
    protected abstract void initProjects(MavenSession session) throws MavenExecutionException;

    protected String getPluginKey() {
        return getPluginGroupId() + ":" + getPluginArtifactId();
    }

    private void fixStandalonePOM(MavenProject mavenProject, File requestBaseDirectory) {
        if (mavenProject == null) return;

        if ("standalone-pom".equals(mavenProject.getArtifactId()) && requestBaseDirectory != null) {
            mavenProject.setFile(new File(requestBaseDirectory, "pom.xml"));
        }
    }

}
