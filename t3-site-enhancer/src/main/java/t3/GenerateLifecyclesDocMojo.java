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
package t3;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.rendersnake.HtmlCanvas;

@Mojo(name = "generate-lifecycles-doc", defaultPhase = LifecyclePhase.POST_SITE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateLifecyclesDocMojo extends AbstractNewPageMojo {

	@Parameter (property="t3.site.globalDocumentation.pageName", defaultValue="lifecycles")
	private String pageName;

	private File componentsFile;

	@Override
	public String getPageName() {
		return pageName;
	}

	private File getComponentsFile() {
		List<String> classpathElements;
		try {
			classpathElements = project.getRuntimeClasspathElements();
		} catch (DependencyResolutionRequiredException e) {
			return null;
		}
		File componentsFile = new File(classpathElements.get(0), "META-INF/plexus/components.xml");

		return componentsFile.exists() ? componentsFile : null;
	}

	private HtmlCanvas getLifecyclesDocumentation(HtmlCanvas html) throws IOException {
		html.p().write(componentsFile.getAbsolutePath().replace("\\", "/"))._p();
		return html;
	}

	@Override
	public HtmlCanvas getContent(HtmlCanvas html) throws IOException {
		componentsFile = getComponentsFile();

		if (componentsFile != null && componentsFile.exists()) {
			return getLifecyclesDocumentation(html);
		} else {
			return null;
		}
	}

}
