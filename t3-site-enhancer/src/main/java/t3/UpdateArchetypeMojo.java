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
import static org.rendersnake.HtmlAttributesFactory.id;
import static org.rendersnake.HtmlAttributesFactory.name;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.joox.JOOX;
import org.joox.Match;
import org.rendersnake.HtmlCanvas;

@Mojo(name = "update-archetype", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateArchetypeMojo extends AbstractReplaceMojo {

	@Override
	protected String getFileNameToReplace() {
		return "index.html";
	}

	private HtmlCanvas getArchetypeSection() throws IOException {
		HtmlCanvas html = new HtmlCanvas();

		html.div(class_("section")).
				div(class_("page-header")).
					h2(id("Archetype")).em().write(project.getArtifactId())._em().write(" archetype")._h2()
				._div().
				a(name("Archetype"))._a();

		String additionalHTML = replaceProperties(getPropertyValue(project, "archetypeAdditionalHTML", false, false, false));

		if (additionalHTML != null && ! additionalHTML.isEmpty()) {
			html.write(additionalHTML, false);
		}

		html.
				p().write("Command line for this archetype:")._p().
				pre().
					write("mvn archetype:generate -DarchetypeGroupId=" + project.getGroupId() + " -DarchetypeArtifactId=" + project.getArtifactId() + " -DarchetypeVersion=" + project.getVersion())
				._pre()
		
			._div();

		return html;
	}

	@Override
	public void processHTMLFile(File htmlFile) throws Exception {
		if ("maven-archetype".equals(project.getPackaging())) {
			addHTMLEntities(htmlFile);

			Match document = JOOX.$(htmlFile);
			try {
				document.xpath("//div[@class='main-body']/div[@class='row']/div[@class='span7']").attr("class", "span12");
			} catch (Exception e) {
				removeHTMLEntities(htmlFile);
				return;
			}
			getLog().info(project.getPackaging());
			getLog().info(htmlFile.getAbsolutePath());

			printDocument(document.document(), htmlFile);

			HtmlCanvas html = getArchetypeSection();
			replaceByLine(htmlFile,
				"<div class=\"body-content\">.*</p></div>",
				"<div class=\"body-content\">" + html.toHtml() + "</div>");
			
			removeHTMLEntities(htmlFile);
		}
	}

}
