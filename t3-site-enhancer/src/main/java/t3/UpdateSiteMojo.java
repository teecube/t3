package t3;

import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

@Mojo(name = "update-menu", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateSiteMojo extends AbstractSiteMojo {

	@Parameter (defaultValue = "true")
	protected Boolean generateSubMenuFromModules;
	private SubMenuReplacement subMenuReplacement;
	private SubMenuReplacement subMenuReplacementParent;

	@Override
	public void processHTMLFile(File htmlFile) throws MojoExecutionException {
		try {
			updateMenu(htmlFile, subMenuReplacement);
			updateMenu(htmlFile, subMenuReplacementParent);
		} catch (MojoExecutionException | IOException e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		subMenuReplacement = new SubMenuReplacement();
		subMenuReplacementParent = new SubMenuReplacement();
		if (generateSubMenuFromModules) {
			try {
				subMenuReplacement = generateSubMenuFromModules();
				subMenuReplacementParent = generateSubMenuFromParent();
			} catch (IOException | XmlPullParserException e) {
				throw new MojoExecutionException(e.getLocalizedMessage(), e);
			}
		}

		super.execute();
	}

	private SubMenuReplacement generateSubMenu(MavenProject project, boolean addParentToLink) throws IOException, XmlPullParserException {
		SubMenuReplacement result = new SubMenuReplacement();

		if (project != null && !project.getModules().isEmpty()) {
			result.setOriginalMenuElement(project.getArtifactId().toUpperCase());
			if (addParentToLink) {
				result.setOriginalMenuElementLink("../index.html");
			} else {
				result.setOriginalMenuElementLink("index.html");
			}

			List<String> modules = project.getModel().getModules();
			for (String module : modules) {
				Model model = POMManager.getModelOfModule(project, module);
				if (addParentToLink) {
					result.getSubMenuElements().put(model.getArtifactId().toUpperCase(), model.getArtifactId() + "/../index.html");
				} else {
					result.getSubMenuElements().put(model.getArtifactId().toUpperCase(), model.getArtifactId() + "/index.html");
				}
			}
		}

		return result;
	}

	private SubMenuReplacement generateSubMenuFromParent() throws IOException, XmlPullParserException {
		return generateSubMenu(project.getParent(), true);
	}

	private SubMenuReplacement generateSubMenuFromModules() throws IOException, XmlPullParserException {
		return generateSubMenu(project, false);
	}

	private void updateMenu(File htmlFile, SubMenuReplacement subMenuReplacement) throws IOException, MojoExecutionException {
		if (subMenuReplacement == null || subMenuReplacement.getOriginalMenuElement() == null) return;

		HtmlCanvas html = new HtmlCanvas();
		html
			.li(class_("dropdown-submenu"))
			.a(href(subMenuReplacement.getOriginalMenuElementLink())).write(subMenuReplacement.getOriginalMenuElement())._a()
			.ul(class_("dropdown-menu"));

		for (String subMenuElement : subMenuReplacement.getSubMenuElements().keySet()) {
			html.render(new SubMenuElement(subMenuElement, subMenuReplacement.getSubMenuElements().get(subMenuElement)));
		}

		html
			._ul()
		._li();

		getLog().info(
			formatHtml(html.toHtml())
		);

		ReplaceRegExp replaceRegExp = new ReplaceRegExp();
		replaceRegExp.setFile(htmlFile);
		replaceRegExp.setMatch("<li.*><a href=.*>" + subMenuReplacement.getOriginalMenuElement() + "</a></li>");
		replaceRegExp.setReplace(formatHtml(html.toHtml()));
		replaceRegExp.setByLine(true);
		replaceRegExp.execute();
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
				subMenuElements = new HashMap<String, String>();
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
