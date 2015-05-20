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

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.joox.JOOX;
import org.joox.Match;

@Mojo(name = "update-doc-menu", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateDocumentationMenuMojo extends AbstractReplaceAllMojo {

	@Override
	public void processHTMLFile(File htmlFile) throws Exception {
		addHTMLEntities(htmlFile);

		Match document;
		Match reportsMenu;

		try {
			document = JOOX.$(htmlFile);
			reportsMenu = document.xpath("//div[@id='top-nav-collapse']/ul/li/ul/li[a/@title='Project Reports']");
		} catch (Exception e) {
			removeHTMLEntities(htmlFile);
			return;
		}

		replaceByLine(htmlFile, "<li class=\"disabled\"><a title=\"#reports\">#reports</a></li>", reportsMenu.content());

		document = JOOX.$(htmlFile);
		document.xpath("//div[@id='top-nav-collapse']/ul/li[ul/li[a/@title='Project Reports']][2]").remove();
		document.xpath("//footer/div/div/div/ul/li[a/@title='#reports']").remove();
		printDocument(document.document(), htmlFile);

		removeHTMLEntities(htmlFile);
	}

}
