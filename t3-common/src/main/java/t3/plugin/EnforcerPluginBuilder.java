/**
 * (C) Copyright 2014-2015 teecube
 * (http://teecube.org) and others.
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
package t3.plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import t3.plugin.annotations.AnnotationsHelper;
import t3.plugin.annotations.ParametersHelper;
import t3.plugin.parameters.MojoRuntime;
import t3.plugin.parameters.Parameter;
import t3.plugin.parameters.ParameterRuntime;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
public class EnforcerPluginBuilder extends PluginBuilder {

	private final static String ENFORCER_GROUPID = "org.apache.maven.plugins";
	private final static String ENFORCER_ARTIFACTID = "maven-enforcer-plugin";
	private final static String ENFORCER_VERSION = "1.3.1";

	public EnforcerPluginBuilder() {
		super(ENFORCER_GROUPID, ENFORCER_ARTIFACTID, ENFORCER_VERSION);
	}

	public Xpp3Dom getDefaultEnforcerConfiguration() {
		Xpp3Dom configuration = new Xpp3Dom("configuration");
		Xpp3Dom fail = new Xpp3Dom("fail");
		fail.setValue("false");
		Xpp3Dom rules = new Xpp3Dom("rules");
		Xpp3Dom skip = new Xpp3Dom("skip");
		skip.setValue("true");
//		Xpp3Dom rules = new Xpp3Dom("rules");
//		Xpp3Dom alwaysPass = new Xpp3Dom("AlwaysPass");
//		rules.addChild(alwaysPass);
//		configuration.addChild(rules);
		configuration.addChild(fail);
		configuration.addChild(rules);
		configuration.addChild(skip);

		return configuration;
	}

	public boolean addConfigurationFromClasspathForPackaging(MavenSession session, MavenProject mavenProject, Class<?> fromClass) {
		if (mavenProject == null) return false;

		Set<Class<?>> mojos = AnnotationsHelper.getTypesAnnotatedWith(fromClass, MojoRuntime.class);
		for (Class<?> clazz : mojos) {			
			Set<Field> parametersAnnotatedFields = AnnotationsHelper.getFieldsAnnotatedWith(clazz, ParameterRuntime.class);
			Set<Parameter> parametersAnnotatations = ParametersHelper.getFieldsAnnotatedWith(parametersAnnotatedFields, ParameterRuntime.class);

			for (Parameter parameter : parametersAnnotatations) {
				if (parameter.isRequired()) {
					System.out.println(parameter.getField());
					System.out.println(parameter.getProperty());
				}
//				System.out.println(field.getName());
			}
		}

		List<String> goals = new ArrayList<String>();
		goals.add("enforce");

//		Xpp3Dom configuration = getDefaultEnforcerConfiguration();
		Xpp3Dom configuration = new Xpp3Dom("");

		PluginExecution pe = new PluginExecution();
		pe.setId("enforce-" + mavenProject.getPackaging());
		pe.setGoals(goals);
		pe.setConfiguration(configuration);

		if (pe != null) {
			this.plugin.getExecutions().add(pe);
//			this.plugin.setConfiguration(Xpp3Dom.mergeXpp3Dom((Xpp3Dom) this.plugin.getConfiguration(), configuration));
			addConfiguration(configuration);
			this.plugin.setVersion("1.3.1");
		}

		return false;
	}

}
