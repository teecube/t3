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
package t3.site;

import static org.rendersnake.HtmlAttributesFactory.class_;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Mojo(name = "update-brand", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateBrandMojo extends AbstractReplaceAllMojo {

	@Override
	public void processHTMLFile(File htmlFile) throws Exception {
		HtmlCanvas html = new HtmlCanvas();
		html
			.div(class_("brand"));

		updateTop(project, html, true, false);

		html
			._div();

		replaceByLine(htmlFile, "<div class=\"brand\".*</div>", formatHtml(html.toHtml()));
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
	}

	private void updateTop(MavenProject project, HtmlCanvas html, boolean last, boolean nextIsEmpty) throws IOException {
		if (project == null) return;

		SiteTop siteTop = new SiteTop(project);

		updateTop(project.getParent(), html, false, siteTop.caption.isEmpty());

		html.render(siteTop);

		if (!last && !nextIsEmpty) {
			html
				.span(class_("brand")
						)
					.write("/")
				._span()
			;
		}
	}

	protected class SiteTop implements Renderable {

		private String caption;
		private String link;

		public SiteTop(MavenProject project) {
			this.caption = getPropertyValue(project, "siteTopCaption", false, false, false);
			this.link = getPropertyValue(project, "siteTopLink", false, false, false);
			if (this.link == null || this.link.isEmpty()) {
				this.link = project.getUrl() + "/index.html";
			} else {
				this.link += "/";
			}
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException {
			if (caption == null || caption.trim().isEmpty()) return;

			html
			.a(class_("brand").href(link))
				.write(caption, false)
			._a();
		}
	}

}
