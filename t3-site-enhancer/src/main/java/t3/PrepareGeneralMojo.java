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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

@Mojo(name = "prepare-general", defaultPhase = LifecyclePhase.PRE_SITE)
public class PrepareGeneralMojo extends AbstractSiteMojo {

	private String rootVersion;
	private String parentUrl;
	private String parentSiteUrl;

	private String replaceURL(String url) {
		if (url == null) return null;

		if (rootVersion != null) {
			url = url.replaceAll("\\$\\{ecosystemSiteVersion\\}", rootVersion);
		}
		if (parentUrl != null) {
			url = url.replaceAll("\\$\\{parent.project.url\\}", parentUrl);
		}
		if (parentSiteUrl != null) {
			url = url.replaceAll("\\$\\{parent.project.siteUrl\\}", parentSiteUrl);
		}

		return url;
	}

	private String prepareURL(String url) {
		if (url == null) return null;

		Pattern p = Pattern.compile("\\$\\{([^}]*)\\}");
		Matcher m = p.matcher(url);

		StringBuffer sb = new StringBuffer();

		while (m.find()) {
			String propertyName = m.group(1);
			String propertyValue = getPropertyValue(propertyName);
			if (propertyValue != null) {
				propertyValue = replaceURL(propertyValue);
			    m.appendReplacement(sb, Matcher.quoteReplacement(propertyValue));
			}
		}
		m.appendTail(sb);
		url = sb.toString();
		url = replaceURL(url);
		
		return url;
	}

	private String getParentUrl(String url, MavenProject currentProject) {
		if (currentProject == null) {
			return url;
		}
		String tmp = getParentUrl(currentProject.getUrl(), currentProject.getParent());
		tmp = prepareURL(tmp);
		url = url.replaceAll("\\$\\{parent.project.url\\}", tmp);

		return url;
	}

	private String getParentSiteUrl(String url, MavenProject currentProject) {
		if (currentProject == null) {
			return url;
		}
		String _siteUrl = null;
		if (currentProject.getDistributionManagement() != null
		&&  currentProject.getDistributionManagement().getSite() != null) {
			_siteUrl = currentProject.getDistributionManagement().getSite().getUrl();
		}
		String tmp = getParentSiteUrl(_siteUrl, currentProject.getParent());
		tmp = prepareURL(tmp);
		url = url.replaceAll("\\$\\{parent.project.siteUrl\\}", tmp);
		
		return url;
	}
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		// set rootVersion
		rootVersion = getRootProjectProperty(project, "ecosystemVersion");
		project.getModel().getProperties().put("ecosystemSiteVersion", rootVersion);
		MavenProject parent = project;
//		while (parent.getParent() != null) { // udpate the property in parent projects too (to avoid bug in maven-site-plugin:deploy goal)
//			parent = parent.getParent();
//			if (parent.getDistributionManagement() != null && parent.getDistributionManagement().getSite() != null) {
//				parent.getDistributionManagement().setSite(null);
//			}
//        }

		String siteURL = getPropertyValue("siteURL");
		siteURL = prepareURL(siteURL);
		project.getModel().getProperties().put("siteURL", siteURL);

		// set parentUrl
		if (project.hasParent()) {
			parentUrl = getParentUrl(project.getParent().getUrl(), project.getParent());
			parentUrl = prepareURL(parentUrl);

			String _siteUrl = null;
			if (project.getParent().getDistributionManagement() != null
			&&  project.getParent().getDistributionManagement().getSite() != null) {
				_siteUrl = project.getParent().getDistributionManagement().getSite().getUrl();
			}

			parentSiteUrl = getParentSiteUrl(_siteUrl, project.getParent());
			parentSiteUrl = prepareURL(parentSiteUrl);
		}

		String url = project.getUrl();
		url = prepareURL(url);
		url = FilenameUtils.normalize(url);
		url = url.replace("\\", "/");

		String siteUrl = project.getDistributionManagement().getSite().getUrl();
		siteUrl = prepareURL(siteUrl);
		
		siteUrl = FilenameUtils.normalize(siteUrl);
		siteUrl = siteUrl.replace("\\", "/");

		updateSiteUrl(url, siteUrl);
	}

	private void updateSiteUrl(String url, String siteUrl) {
		project.getDistributionManagement().getSite().setUrl(siteUrl);
		project.setUrl(url);
		if (project.getOriginalModel() != null
			&& project.getOriginalModel().getDistributionManagement() != null
			&& project.getOriginalModel().getDistributionManagement().getSite() != null) {
			project.getOriginalModel().getDistributionManagement().getSite().setUrl(siteUrl);
			project.getOriginalModel().setUrl(url);
		}
	}

	@Override
	public void processHTMLFile(File htmlFile) throws Exception {

	}

}
