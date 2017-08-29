/**
 * (C) Copyright 2016-2017 teecube
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
package t3.plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.Parameter;
import t3.plugin.annotations.helpers.AnnotationsHelper;
import t3.plugin.annotations.helpers.ParametersHelper;
import t3.plugin.annotations.impl.ParameterImpl;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class EnforcerPluginBuilder extends PluginBuilder {

	private final static String ENFORCER_GROUPID = "org.apache.maven.plugins";
	private final static String ENFORCER_ARTIFACTID = "maven-enforcer-plugin";
	private final static String ENFORCER_VERSION = "1.4.1";

	public EnforcerPluginBuilder() {
		super(ENFORCER_GROUPID, ENFORCER_ARTIFACTID, ENFORCER_VERSION);
	}

	public Xpp3Dom getDefaultEnforcerConfiguration() {
		Xpp3Dom configuration = new Xpp3Dom("configuration");
//		Xpp3Dom fail = new Xpp3Dom("fail");
//		fail.setValue("false");
		Xpp3Dom rules = new Xpp3Dom("rules");
//		Xpp3Dom skip = new Xpp3Dom("skip");
//		skip.setValue("true");
//		configuration.addChild(fail);
		configuration.addChild(rules);
//		configuration.addChild(skip);

		return configuration;
	}

	public Xpp3Dom addConfigurationFromClasspathForProject(MavenSession session, MavenProject mavenProject, Class<?> fromClass) {
		boolean enabled = false;
		if (mavenProject == null) return null;

		Xpp3Dom configuration = getDefaultEnforcerConfiguration();

		Set<Class<?>> mojos = AnnotationsHelper.getTypesAnnotatedWith(fromClass, Mojo.class);

		List<String> parameters = new ArrayList<String>();

		for (Class<?> clazz : mojos) {			
			Set<Field> parametersAnnotatedFields = AnnotationsHelper.getFieldsAnnotatedWith(clazz, Parameter.class);
			Set<ParameterImpl> parametersAnnotatations = ParametersHelper.getFieldsAnnotatedWith(parametersAnnotatedFields, Parameter.class);

			for (ParameterImpl parameter : parametersAnnotatations) {
				if (parameter.isRequired() && !parameters.contains(parameter.getProperty())) {
					List<String> packagings = parameter.getRequiredForPackagings();
					if (packagings != null && !packagings.isEmpty() && !(packagings.size() == 1 && packagings.get(0).isEmpty()) && !packagings.contains(mavenProject.getPackaging())) {
						continue;
					}
					parameters.add(parameter.getProperty());
					Xpp3Dom rule = new Xpp3Dom("requireProperty");
					Xpp3Dom property = new Xpp3Dom("property");
					property.setValue(parameter.getProperty());
					Xpp3Dom message = new Xpp3Dom("message");
					message.setValue(parameter.getDescription());
					Xpp3Dom regex = new Xpp3Dom("regex");
					regex.setValue(".*");
					Xpp3Dom regexMessage = new Xpp3Dom("regexMessage");
					regexMessage.setValue(parameter.getDescription());
					rule.addChild(property);
					rule.addChild(message);
					rule.addChild(regex);
					rule.addChild(regexMessage);
					configuration.getChild("rules").addChild(rule);
					enabled = true;
				}
//				System.out.println(field.getName());
			}
		}
		
		if (enabled) {
			addEnforcerPluginExecution(mavenProject, configuration);
			return configuration;
		}
		else {
			return null;
		}
	}

	private void addEnforcerPluginExecution(MavenProject mavenProject, Xpp3Dom configuration) {
		List<String> goals = new ArrayList<String>();
		goals.add("enforce");

		PluginExecution pe = new PluginExecution();
		pe.setId("enforce-" + mavenProject.getPackaging());
		pe.setGoals(goals);
		pe.setConfiguration(configuration);

		if (pe != null) {
			this.plugin.getExecutions().add(pe);
		}		
	}

}
