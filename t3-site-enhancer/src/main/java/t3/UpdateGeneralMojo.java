/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://t3soft.org) and others.
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

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;

@Mojo(name = "update-general", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateGeneralMojo extends AbstractSiteMojo {

	private void replaceProperty(File htmlFile, String propertyName) {
		replaceProperty(htmlFile, propertyName, propertyName);
	}

	private void replaceProperty(File htmlFile, String propertyName, String modelPropertyName) {
		replaceProperty(htmlFile, propertyName, propertyName, false);
	}

	private void replaceProperty(File htmlFile, String propertyName, String modelPropertyName, boolean propertyInRootProject) {
		ReplaceRegExp replaceRegExp = new ReplaceRegExp();
		replaceRegExp.setFile(htmlFile);
		replaceRegExp.setMatch("\\$\\{" + propertyName + "\\}");
		if (propertyInRootProject) {
			replaceRegExp.setReplace(getRootProjectProperty(project, modelPropertyName));
		} else {
			replaceRegExp.setReplace(getPropertyValue(modelPropertyName));
		}
		replaceRegExp.setByLine(true);
		replaceRegExp.execute();
	}

	@Override
	public void processHTMLFile(File htmlFile) throws Exception {
		replaceProperty(htmlFile, "ecosystemSiteVersion", "ecosystemVersion", true);
		replaceProperty(htmlFile, "ticVersion");
		replaceProperty(htmlFile, "tacVersion");
		replaceProperty(htmlFile, "toeVersion");
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
	}

}
