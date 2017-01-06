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

import static org.rendersnake.HtmlAttributesFactory.border;
import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;
import static org.rendersnake.HtmlAttributesFactory.id;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.joox.JOOX;
import org.joox.Match;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import t3.LifecyclesUtils;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
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

	private HtmlCanvas getLifecyclesSection(HtmlCanvas html) throws IOException {
		for (Lifecycle lifecycle : lifecycles) {
			html.render(lifecycle);
		}

		return html;		
	}

	private HtmlCanvas getLifecyclesDocumentation(HtmlCanvas html) throws IOException {
		html.

		div(class_("row")).
			div(class_("span12")).
				div(class_("body-content")).
					div(class_("section")).
						div(class_("page-header")).
							h2(id("Lifecycles")).write("Lifecycles")._h2().
							p().write("The different lifecycles of the plugin are associated with custom packagings.")._p();

		html.render(new Renderable() {
			@Override
			public void renderOn(HtmlCanvas html) throws IOException {
				getLifecyclesSection(html);
			}
		});

		html
						._div()
					._div()
				._div()
			._div()
		._div();

		return html;
	}

	@Component
	private DefaultPlexusContainer beanModule;

	private List<Lifecycle> lifecycles;

	private List<Lifecycle> parseLifecycles(File componentsFile) throws SAXException, IOException {
		List<Lifecycle> lifecycles = new ArrayList<GenerateLifecyclesDocMojo.Lifecycle>();
		Match lifecyclesElements;
		lifecyclesElements = JOOX.$(componentsFile).xpath("//component[implementation='org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping']");
		for (Element element : lifecyclesElements) {
			List<Phase> phases = new ArrayList<GenerateLifecyclesDocMojo.Phase>();
			Match phasesElements = JOOX.$(element).xpath("configuration/phases/*");
			for (Element phase : phasesElements) {
				phases.add(new Phase(phase.getNodeName(), phase.getTextContent(), project, session));
			}
			lifecycles.add(new Lifecycle(JOOX.$(element).xpath("role-hint").text(), phases));
		}
		return lifecycles;
	}

	@Override
	public HtmlCanvas getContent(HtmlCanvas html) throws IOException, SAXException, MojoExecutionException, MojoFailureException {
		componentsFile = getComponentsFile();
		
		if (componentsFile != null && componentsFile.exists()) {
			this.lifecycles = parseLifecycles(componentsFile);
			for (Lifecycle lifecycle : lifecycles) {
				createPackagingPage(lifecycle.getPackagingName());
			}
			return getLifecyclesDocumentation(html);
		} else {
			return null;
		}
	}

	private void createPackagingPage(final String packagingName) throws MojoExecutionException, MojoFailureException {
		AbstractNewPageMojo packagingPage = new AbstractNewPageMojo() {
			@Override
			public String getPageName() {
				return "packaging-" + packagingName;
			}
			
			@Override
			public HtmlCanvas getContent(HtmlCanvas html) throws IOException, SAXException {
				html.

				div(class_("row")).
					div(class_("span12")).
						div(class_("body-content")).
							div(class_("section")).
								div(class_("page-header")).
									h2(id(packagingName + "-packaging")).write(packagingName + " packaging")._h2().
									p().write("This is the packaging for " + packagingName + ".")._p()
								._div()
							._div()
						._div()
					._div()
				._div();

				return html;
			}
		};

		packagingPage.outputDirectory = this.outputDirectory;
		packagingPage.execute();
	}

	class Lifecycle extends LifecyclesUtils.Lifecycle<Phase> implements Renderable {

		public Lifecycle(String packagingName, List<Phase> phases) {
			super(packagingName, phases);
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException {
			html.
			div(class_("section")).
				h3(id("Lifecycle_"+this.getPackagingName())).a(href("packaging-"+this.getPackagingName()+".html")).write(this.getPackagingName())._a()._h3().
				table(border("0").class_("bodyTable table table-striped table-hover")).
					thead().
						tr(class_("a")).
							th().write("Phase")._th().
							th().write("Goal")._th().
						_tr()
					._thead().
					tbody();

			for (t3.LifecyclesUtils.Phase phase : this.getPhases()) {
				((Phase) phase).renderOn(html);
			}

			html
					._tbody()
				._table()
			._div();
		}

	}

	class Phase extends LifecyclesUtils.Phase implements Renderable {

		public Phase(String phaseName, String goals, MavenProject mavenProject, MavenSession session) {
			super(phaseName, goals, mavenProject, session);
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException {
			html.
				tr().
					td().
						tt().write(this.getPhaseName())._tt()
					._td().
					td().
						tt();

			for (final String goal : this.getGoals()) {
				html.render(new Renderable() {
					@Override
					public void renderOn(HtmlCanvas html) throws IOException {
						String goalName = null;
						if (goal != null && getMavenProject() != null && goal.startsWith(getMavenProject().getGroupId() + ":" + getMavenProject().getArtifactId())) {
							goalName = goal.substring(goal.lastIndexOf(":") + 1, goal.length());
						}
						if (goalName != null) {
							 html.a(href("./" + goalName + "-mojo.html"));
						}
						html.write(goal);
						if (goalName != null) {
							html._a();
						}
						html.br();
					}
				});
			}

			html
						._tt()
					._td()
				._tr();
		}

	}

}
