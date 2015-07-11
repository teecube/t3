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
import static org.rendersnake.HtmlAttributesFactory.href;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

@Mojo(name = "update-site", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateSiteMojo extends AbstractReplaceAllMojo {

	@Parameter (defaultValue = "true")
	protected Boolean generateSubMenuFromModules;

	@Parameter
	private List<TopMenu> topmenus;

	private List<SubMenuReplacement> subMenuReplacements;
	private SimpleReplacement span4to5;
	private SimpleReplacement span8to7;

	@Override
	public void processHTMLFile(File htmlFile) throws MojoExecutionException {
		try {
			updateMenu(htmlFile, subMenuReplacements);

			updateSimple(htmlFile, span4to5);
			updateSimple(htmlFile, span8to7);
		} catch (MojoExecutionException | IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		subMenuReplacements = new ArrayList<SubMenuReplacement>();

		span4to5 = new SimpleReplacement();
		span4to5.setFrom("<div class=\"span4\">");
		span4to5.setTo("<div class=\"span5\"><style>ul { margin: 0 0 0 25px; }</style>");

		span8to7 = new SimpleReplacement();
		span8to7.setFrom("<div class=\"span8\">");
		span8to7.setTo("<div class=\"span7\">");

		if (generateSubMenuFromModules) {
			subMenuReplacements = generateSubMenu();
		}

		super.execute();
	}

	private List<SubMenuReplacement> generateSubMenu() {
		List<SubMenuReplacement> result = new ArrayList<SubMenuReplacement>();

		if (this.topmenus == null) {
			this.topmenus = new ArrayList<TopMenu>();
		}

		for (TopMenu topMenu : topmenus) {
			SubMenuReplacement subMenuReplacement = new SubMenuReplacement();
			subMenuReplacement.setOriginalMenuElement(topMenu.name);

			String url = topMenu.url;
			if (url != null && !url.toLowerCase().endsWith("index.html") && !url.toLowerCase().endsWith("index.htm")) {
				url = url + "/index.html";
			}
			subMenuReplacement.setOriginalMenuElementLink(url);

			for (SubMenu module : topMenu.submenus) {
				url = module.url;
				if (url != null && !url.toLowerCase().endsWith("index.html") && !url.toLowerCase().endsWith("index.htm")) {
					url = url + "/index.html";
				}
				subMenuReplacement.getSubMenuElements().put(module.name, url);
			}

			result.add(subMenuReplacement);
		}

		return result;
	}

	private void updateMenu(File htmlFile, List<SubMenuReplacement> subMenuReplacements) throws IOException, MojoExecutionException {
		if (subMenuReplacements == null || subMenuReplacements.isEmpty()) return;

		for (SubMenuReplacement subMenuReplacement : subMenuReplacements) {
			HtmlCanvas html = new HtmlCanvas();
			html
				.li(class_("dropdown-submenu"))
				.a(href(subMenuReplacement.getOriginalMenuElementLink())).write(subMenuReplacement.getOriginalMenuElement())._a()
				.ul(class_("dropdown-menu"));

			if (subMenuReplacement.getSubMenuElements() != null) {
				for (String subMenuElement : subMenuReplacement.getSubMenuElements().keySet()) {
					html.render(new SubMenuElement(subMenuElement, subMenuReplacement.getSubMenuElements().get(subMenuElement)));
				}
			}

			html
				._ul()
			._li();

			replaceByLine(htmlFile, "<li.*><a href=.*>" + subMenuReplacement.getOriginalMenuElement() + "</a></li>", formatHtml(html.toHtml()));
		}
	}

	private void updateSimple(File htmlFile, SimpleReplacement simpleReplacement) {
		if (htmlFile == null || simpleReplacement == null) return;

		replaceByLine(htmlFile, simpleReplacement.getFrom(), simpleReplacement.getTo());
	}

	protected class SubMenuElement implements Renderable {

		private String subMenuElement;
		private String subMenuLink;

		public SubMenuElement(String subMenuElement, String subMenuLink) {
			this.subMenuElement = subMenuElement;
			this.subMenuLink = subMenuLink;
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException {
			html.li().a(href(subMenuLink)).write(subMenuElement)._a()._li();
		}

	}

	protected class SimpleReplacement {
		private String from;
		private String to;

		public String getFrom() {
			return from;
		}
		public void setFrom(String from) {
			this.from = from;
		}
		public String getTo() {
			return to;
		}
		public void setTo(String to) {
			this.to = to;
		}
	}

	protected class SubMenuReplacement {
		private String originalMenuElement; // for instance: TIC (in <li ><a href="tic/index.html" title="TIC">TIC</a></li>)
		private String originalMenuElementLink; // for instance: tic/index.html (in <li ><a href="tic/index.html" title="TIC">TIC</a></li>)
		private Map<String, String> subMenuElements; // caption/link

		public String getOriginalMenuElement() {
			return originalMenuElement;
		}

		public void setOriginalMenuElement(String originalMenuElement) {
			this.originalMenuElement = originalMenuElement;
		}

		public Map<String, String> getSubMenuElements() {
			if (subMenuElements == null) {
				subMenuElements = new TreeMap<String, String>();
			}
			return subMenuElements;
		}

		public void setSubMenuElements(Map<String, String> subMenuElements) {
			this.subMenuElements = subMenuElements;
		}

		public String getOriginalMenuElementLink() {
			return originalMenuElementLink;
		}

		public void setOriginalMenuElementLink(String originalMenuElementLink) {
			this.originalMenuElementLink = originalMenuElementLink;
		}
	}

}
