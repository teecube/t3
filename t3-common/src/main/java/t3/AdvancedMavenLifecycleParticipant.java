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

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.project.ProjectBuilder;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.logging.Logger;

/**
* <p>
* This interface enforces that classes define setters for requirements in order
* to set them manually when the lifecycle participant is called manually (when
* it cannot be triggered by Maven callbacks, for instance when running a goal
* without POM).
* </p>
*
* <p>
* The "afterProjectsRead()" method comes from
* org.apache.maven.AbstractMavenLifecycleParticipant
* because classes implementing this interface also extends this class.
* </p>
*
* @author Mathieu Debove &lt;mad@teecu.be&gt;
*
*/
public interface AdvancedMavenLifecycleParticipant {
    // setters
    void setArtifactRepositoryFactory(ArtifactRepositoryFactory artifactRepositoryFactory);
    void setArtifactHandler(ArtifactHandler artifactHandler);
    void setArtifactResolver(ArtifactResolver artifactResolver);
    void setLogger(Logger logger);
    void setPlexus(PlexusContainer plexus);
    void setPluginManager(BuildPluginManager pluginManager);
    void setProjectBuilder(ProjectBuilder projectBuilder);

    // method from org.apache.maven.AbstractMavenLifecycleParticipant
    void afterProjectsRead(MavenSession session) throws MavenExecutionException;
}
