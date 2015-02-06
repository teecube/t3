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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.LifecycleMappingDelegate;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoNotFoundException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.project.MavenProject;

@Named("post")
public class DeployLifecycleMappingDelegate implements LifecycleMappingDelegate {

	@Override
	public Map<String, List<MojoExecution>> calculateLifecycleMappings(MavenSession session,
																		MavenProject project,
																		Lifecycle lifecycle,
																		String lifecyclePhase)
	throws PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException, MojoNotFoundException, InvalidPluginDescriptorException {
		List<MojoExecution> mojoExecutions = new ArrayList<MojoExecution>();

		for (Plugin plugin : project.getBuild().getPlugins()) {
			for (PluginExecution execution : plugin.getExecutions()) {
//				if (POST_PHASE.equals(execution.getPhase())) {
					for (String goal : execution.getGoals()) {
						MojoExecution mojoExecution = new MojoExecution(plugin, goal, execution.getId());
						mojoExecution.setLifecyclePhase(execution.getPhase());
						mojoExecutions.add(mojoExecution);
					}
//				}
			}
		}

		return Collections.singletonMap("post", mojoExecutions);
	}

}
