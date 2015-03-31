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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "prepare-general", defaultPhase = LifecyclePhase.PRE_SITE)
public class PrepareGeneralMojo extends AbstractSiteMojo {

	private String prepareURL(String url) {
		if (url == null) return null;

		Pattern p = Pattern.compile("\\$\\{([^}]*)\\}");
		Matcher m = p.matcher(url);

		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String propertyName = m.group(1);
			String propertyValue = getPropertyValue(propertyName);
			if (propertyValue != null) {
			    m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
			}
		}
		m.appendTail(sb);
		url = sb.toString();
		
		return url;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		String siteURL = getRootProjectProperty(project, "siteURL");
		siteURL = prepareURL(siteURL);
		project.getModel().getProperties().put("siteURL", siteURL);

		updateSiteUrl(prepareURL(project.getUrl()), prepareURL(project.getDistributionManagement().getSite().getUrl()));
	}

	private void updateSiteUrl(String url, String siteUrl) {
		project.getDistributionManagement().getSite().setUrl(siteUrl);
		project.setUrl(url);
	}

	@Override
	public void processHTMLFile(File htmlFile) throws Exception {

	}

}
