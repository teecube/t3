/**
 * (C) Copyright 2016-2018 teecube
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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.rendersnake.HtmlCanvas;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public abstract class AbstractNewPageMojo extends AbstractSiteMojo {

	private File copyFromIndex() throws IOException {
		File indexFile = new File(outputDirectory.getAbsolutePath(), "index.html");
		File htmlFile = new File(outputDirectory.getAbsolutePath(), getPageName() + ".html");
		FileUtils.copyFile(indexFile, htmlFile);

		replaceByLine(htmlFile, "<a href=\"\" id=\"bannerLeft\">", "<a id=\"bannerLeft\" href=\"./index.html\">", false, "ms");

		return htmlFile;
	}

	private void saveDocumentationFile(File htmlFile, HtmlCanvas html) throws FileNotFoundException, MojoExecutionException {
		replaceByLine(htmlFile,
					  "<div class=\"main-body\">.*</div>" + System.lineSeparator() + System.lineSeparator() + "\t</div><!-- /container -->",
					  "<div class=\"main-body\">." + System.lineSeparator() + formatHtml(html.toHtml()) + System.lineSeparator() + "\t</div>" + System.lineSeparator() + System.lineSeparator() + "\t</div><!-- /container -->",
					  false,
					  "ms");
	}

	public abstract HtmlCanvas getContent(HtmlCanvas html) throws IOException, SAXException, MojoExecutionException, MojoFailureException;

	public abstract String getPageName();

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();

		File htmlFile;
		try {
			HtmlCanvas html = getContent(new HtmlCanvas());
			if (html != null) {
				htmlFile = copyFromIndex();
				saveDocumentationFile(htmlFile, html);
			}
		} catch (IOException | SAXException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

}
