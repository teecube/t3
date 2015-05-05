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

import static org.rendersnake.HtmlAttributesFactory.class_;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

@Mojo(name = "update-brand", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateBrandMojo extends AbstractReplaceAllMojo {

	@Override
	public void processHTMLFile(File htmlFile) throws Exception {
		HtmlCanvas html = new HtmlCanvas();
		html
			.div(class_("brand"));

		updateTop(project, html, true);

		html
			._div();

		ReplaceRegExp replaceRegExp = new ReplaceRegExp();
		replaceRegExp.setFile(htmlFile);
		replaceRegExp.setMatch("<div class=\"brand\".*</div>");
		replaceRegExp.setReplace(formatHtml(html.toHtml()));
		replaceRegExp.setByLine(true);
		replaceRegExp.execute();
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
	}

	private void updateTop(MavenProject project, HtmlCanvas html, boolean last) throws IOException {
		if (project == null) return;

		updateTop(project.getParent(), html, false);

		SiteTop siteTop = new SiteTop(project);
		html.render(siteTop);

		if (!last) {
			html
				.span(class_("brand")
						)
					.write("/")
				._span()
			;
		}
	}

	protected class SiteTop implements Renderable {

		private MavenProject project;

		public SiteTop(MavenProject project) {
			this.project = project;
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException {
			String caption = getPropertyValue(project, "siteTopCaption", false, false);

			if (caption == null || caption.trim().isEmpty()) return;

			String link = project.getUrl() + "/index.html";

			html
			.a(class_("brand").href(link))
				.write(caption, false)
			._a();
		}
	}

}
