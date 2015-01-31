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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class PluginBuilder {

	private Plugin plugin;

	public PluginBuilder(Plugin plugin) {
		this.plugin = plugin;
	}

	public PluginBuilder(String groupId, String artifactId, String version) {
		Plugin plugin = new Plugin();
		plugin.setArtifactId(artifactId);
		plugin.setGroupId(groupId);
		plugin.setVersion(version);

		this.plugin = plugin;
	}

	public PluginBuilder(String groupId, String artifactId) {
		this(groupId, artifactId, "");
	}

	public void addConfiguration(Xpp3Dom configuration) {
		// TODO: take care of priority (dominant vs recessive)
		this.plugin.setConfiguration(Xpp3Dom.mergeXpp3Dom(configuration, (Xpp3Dom) this.plugin.getConfiguration()));
	}

	public boolean addConfigurationFromClasspath() throws MojoExecutionException {
		String filename = "/plugins-configuration/" +
						  this.plugin.getGroupId() + "/" +
						  this.plugin.getArtifactId()  + ".xml";

		InputStream configStream = PluginBuilder.class.getResourceAsStream(filename);
		if (configStream == null) return false;

		try {
			String configString = IOUtils.toString(configStream);
			Xpp3Dom pluginConfiguration = Xpp3DomBuilder.build(new ByteArrayInputStream(configString.getBytes()), "UTF-8"); // FIXME: encoding

			if (pluginConfiguration != null) {
				Xpp3Dom version = pluginConfiguration.getChild("version");
				if (version != null) {
					this.plugin.setVersion(version.getValue());
				}

				Xpp3Dom configuration = pluginConfiguration.getChild("configuration");
				if (configuration != null) {
					this.plugin.setConfiguration(Xpp3Dom.mergeXpp3Dom((Xpp3Dom) this.plugin.getConfiguration(), configuration));
				}

				Xpp3Dom executions = pluginConfiguration.getChild("executions");
				if (executions != null) {
					List<PluginExecution> pluginExecutions = new ArrayList<PluginExecution>();
					pluginExecutions.addAll(this.plugin.getExecutions());
					for (Xpp3Dom execution : executions.getChildren()) {
						if ("execution".equals(execution.getName())) {
							PluginExecution ex = new PluginExecution();

							Xpp3Dom inherited = execution.getChild("inherited");
							if (inherited != null && inherited.getValue() != null && !inherited.getValue().isEmpty()) {
								ex.setInherited(inherited.getValue());
							} else {
								ex.setInherited(false);
							}
							Xpp3Dom idDom = execution.getChild("id");
							String id = null;
							if (idDom != null && idDom.getValue() != null && !idDom.getValue().isEmpty()) {
								id = idDom.getValue();
								ex.setId(id);
							}
							Xpp3Dom goalsDom = execution.getChild("goals");
							if (goalsDom != null) {
								List<String> goals = new ArrayList<String>();
								for (Xpp3Dom goal : goalsDom.getChildren()) {
									if (goal.getValue() != null && !goal.getValue().isEmpty()) {
										goals.add(goal.getValue());
									}
								}
								ex.setGoals(goals);
							}
							Xpp3Dom phase = execution.getChild("phase");
							if (phase != null && phase.getValue() != null && !phase.getValue().isEmpty()) {
								ex.setPhase(phase.getValue());
							}

							configuration = execution.getChild("configuration");
							if (configuration != null) {
								ex.setConfiguration(configuration);
							}

							if (id != null && !id.isEmpty()) {
								PluginExecution oldEx = this.plugin.getExecutionsAsMap().get(id);
								if (oldEx != null) {
									ex.setConfiguration(Xpp3Dom.mergeXpp3Dom((Xpp3Dom) oldEx.getConfiguration(), (Xpp3Dom) ex.getConfiguration()));
									ex.setInherited(oldEx.getInherited());
									ex.setPriority(oldEx.getPriority());

									this.plugin.getExecutionsAsMap().put(id, ex);
									for (ListIterator<PluginExecution> it = this.plugin.getExecutions().listIterator(); it.hasNext();) {
										PluginExecution pluginExecution = it.next();
										if (id.equals(pluginExecution.getId())) {
											it.set(ex);
										}
									}
								} else {
									pluginExecutions.add(ex);
									this.plugin.setExecutions(pluginExecutions);
								}
							} else {
								pluginExecutions.add(ex);
								this.plugin.setExecutions(pluginExecutions);
							}
						}
					}
				}
			}
		} catch (IOException | XmlPullParserException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		return true;
	}

	public Plugin getPlugin() {
		return this.plugin;
	}

}
