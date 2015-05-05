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

import static org.rendersnake.HtmlAttributesFactory.border;
import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;
import static org.rendersnake.HtmlAttributesFactory.id;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.reflections.util.ClasspathHelper;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import t3.plugin.annotations.FieldsHelper;
import t3.plugin.annotations.ParametersHelper;
import t3.plugin.parameters.Parameter;

@Mojo(name = "generate-global-doc", defaultPhase = LifecyclePhase.POST_SITE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateGlobalParametersDocMojo extends AbstractNewPageMojo {

	private List<GlobalParameter> globalParameters;

	@org.apache.maven.plugins.annotations.Parameter (property="t3.site.globalDocumentation.pageName", defaultValue="global-documentation")
	private String pageName;

	@org.apache.maven.plugins.annotations.Parameter (property="t3.site.globalDocumentation.bootstrapClass", defaultValue="")
	private String bootstrapClass;

	protected class GlobalParameterComparator implements Comparator<GlobalParameter> {
	    @Override
	    public int compare(GlobalParameter o1, GlobalParameter o2) {
			if (o1 == null || o2 == null) {
				return 0;
			}
	        return o1.property.compareTo(o2.property);
	    }
	}

	protected class GlobalParameter implements Renderable {

		private String name;
		private String type;
		private String property;
		private String defaultValue;
		private String since;
		private String description;
		private boolean firstClass;

		public GlobalParameter(String name, String type, String property, String defaultValue, String since, String description, boolean firstClass) {
			this.name = name;
			this.type = type;
			this.property = property;
			this.defaultValue = defaultValue;
			this.since = since;
			this.description = description;
			this.firstClass = firstClass;
		}

		@Override
		public void renderOn(HtmlCanvas html) throws IOException {
			String clazz;
			if (this.firstClass) {
				clazz = "a";
			} else {
				clazz = "b";
			}

			html.
			tr(class_(clazz)).
				td().
					b().
						a(href("#"+name)).write(property)._a()
					._b()
				._td().
//				td().
//					tt().write(defaultValue)._tt()
//				._td().
				td().
					tt().write(type)._tt()
				._td().
				td().
					tt().write(since)._tt()
				._td().
				td();

			html.render(new Renderable() {

				@Override
				public void renderOn(HtmlCanvas html) throws IOException {
					html.
						p().write(description);

					if (name != null && !name.isEmpty()) {
						html.br();
						html.b().write("Parameter name is")._b().write(": " + name + ".");
					}
					if (property != null && !property.isEmpty()) {
						html.br();
						html.b().write("User property is")._b().write(": " + property + ".");
					}
					if (defaultValue != null && !defaultValue.isEmpty()) {
						html.br();
						html.b().write("Default value is")._b().write(": " + defaultValue + ".");
					}

					html._p();
				}
			});

			html._td()._tr();
		}

	}

	private HtmlCanvas generateGlobalParametersSection(HtmlCanvas html) throws IOException {
		html.
		div(class_("section")).
			h3(id("Global_Parameters")).write("Global Parameters")._h3().
			table(border("0").class_("bodyTable table table-striped table-hover")).
				thead().
					tr(class_("a")).
						th().write("Property")._th().
//						th().write("Default value")._th().
						th().write("Type")._th().
						th().write("Since")._th().
						th().write("Description")._th().
					_tr()
				._thead().
				tbody();

		for (GlobalParameter globalParameter : globalParameters) {
			html.render(globalParameter);
		}

		html
				._tbody()
			._table()
		._div();
		return html;
	}

	private HtmlCanvas generateGlobalParametersDocumentation(HtmlCanvas html) throws IOException {
		html.

		div(class_("row")).
			div(class_("span12")).
				div(class_("body-content")).
					div(class_("section")).
						div(class_("page-header")).
							h2(id("Global_Documentation")).write("Global Documentation")._h2().
							p().write("The global documentation describes the parameters shared by all goals called 'Global Parameters'.")._p();

		html.render(new Renderable() {
			@Override
			public void renderOn(HtmlCanvas html) throws IOException {
				generateGlobalParametersSection(html);
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

	@Override
	public HtmlCanvas getContent(HtmlCanvas html) throws IOException {
		return generateGlobalParametersDocumentation(html);
	}

	private ClassLoader getClassLoader() throws MalformedURLException, DependencyResolutionRequiredException {
		List<String> classpathElements = project.getRuntimeClasspathElements();

		List<URL> projectClasspathList = new ArrayList<URL>();
		for (String element : classpathElements) {
			projectClasspathList.add(new File(element).toURI().toURL());
		}

		URLClassLoader loader = new URLClassLoader(projectClasspathList.toArray(new URL[0]));
		return loader;
	}

	@Override
	public String getPageName() {
		return pageName;
	}

	private List<GlobalParameter> getGlobalParameters() throws MalformedURLException, DependencyResolutionRequiredException, ClassNotFoundException {
		List<GlobalParameter> result = new ArrayList<GlobalParameter>();

		ClassLoader classLoader = getClassLoader();
		Class<?> clazz = classLoader.loadClass(bootstrapClass);

		Set<Field> globalParametersAnnotatedFields = FieldsHelper.getFieldsAnnotatedWith(clazz, t3.plugin.parameters.GlobalParameter.class, ClasspathHelper.contextClassLoader(), classLoader);
		Set<Parameter> globalParametersAnnotatations = ParametersHelper.getFieldsAnnotatedWith(globalParametersAnnotatedFields, t3.plugin.parameters.GlobalParameter.class);

		boolean firstClass = true;
		for (Parameter parameter : globalParametersAnnotatations) {
			firstClass = !firstClass;
			result.add(new GlobalParameter(parameter.getField(), parameter.getType(), parameter.getProperty(), parameter.getDefaultValue(), "-", "description", firstClass));
		}
		return result;
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (bootstrapClass == null || bootstrapClass.isEmpty()) return; // skip

		try {
			globalParameters = getGlobalParameters();
			Collections.sort(globalParameters, new GlobalParameterComparator());
		} catch (Throwable e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}

		super.execute();
	}

}
