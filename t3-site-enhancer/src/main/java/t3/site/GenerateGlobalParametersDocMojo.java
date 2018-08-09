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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.util.ClasspathHelper;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;
import t3.plugin.annotations.helpers.AnnotationsHelper;
import t3.plugin.annotations.helpers.CategoriesHelper;
import t3.plugin.annotations.helpers.ParametersHelper;
import t3.plugin.annotations.impl.CategoryImpl;
import t3.plugin.annotations.impl.ParameterImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.rendersnake.HtmlAttributesFactory.*;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Mojo(name = "generate-global-doc", defaultPhase = LifecyclePhase.POST_SITE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GenerateGlobalParametersDocMojo extends AbstractNewPageMojo {

    private List<GlobalParameter> globalParameters;
    private List<Category> parametersCategories;

    private static List<URL> additionalClasspathEntries = new ArrayList<URL>();

    @org.apache.maven.plugins.annotations.Parameter (property="t3.site.globalDocumentation.pageName", defaultValue="global-documentation")
    private String pageName;

    @org.apache.maven.plugins.annotations.Parameter (property="t3.site.globalDocumentation.bootstrapClass", required = false, defaultValue="java.util.Properties")
    private String bootstrapClass;

    @org.apache.maven.plugins.annotations.Parameter (property="t3.site.globalDocumentation.sampleProfileCommandLineGenerator", required = false, defaultValue="")
    private String sampleProfileCommandLineGenerator;

    private Class<?> bootstrapClazz = null;

    public void setBootstrapClass(Class<?> bootstrapClazz) {
        this.bootstrapClazz = bootstrapClazz;
    }

    private boolean hasAtLeastOneNotGuessedProperty(String category, List<GlobalParameter> globalParameters) {
        if (category == null || globalParameters == null) {
            for (GlobalParameter globalParameter : globalParameters) {
                if ((globalParameter.category == null || globalParameter.category.isEmpty()) && !globalParameter.valueGuessedByDefault) {
                    return true;
                }
            }

            return false;
        }

        for (GlobalParameter globalParameter : globalParameters) {
            if (category.equals(globalParameter.category) && !globalParameter.valueGuessedByDefault) {
                return true;
            }
        }
        return false;
    }

    private Class<?> getBootstrapClass() throws MalformedURLException, DependencyResolutionRequiredException, ClassNotFoundException {
        if (bootstrapClazz != null) {
            return bootstrapClazz;
        }

        ClassLoader classLoader = getClassLoader(project, additionalClasspathEntries);
        Class<?> clazz = classLoader.loadClass(bootstrapClass);

        return clazz;
    }

    private List<GlobalParameter> getGlobalParameters() throws MalformedURLException, DependencyResolutionRequiredException, ClassNotFoundException {
        List<GlobalParameter> globalParameters = new ArrayList<GlobalParameter>();

        Set<Field> globalParametersAnnotatedFields = AnnotationsHelper.getFieldsAnnotatedWith(getBootstrapClass(), t3.plugin.annotations.GlobalParameter.class, ClasspathHelper.contextClassLoader(), getClassLoader(project, additionalClasspathEntries));
        Set<ParameterImpl> globalParametersAnnotatations = ParametersHelper.getFieldsAnnotatedWith(globalParametersAnnotatedFields, t3.plugin.annotations.GlobalParameter.class);

        boolean firstClass = true;
        for (ParameterImpl parameter : globalParametersAnnotatations) {
            firstClass = !firstClass;
            globalParameters.add(new GlobalParameter(parameter.field(), parameter.type(), parameter.property(), parameter.defaultValue(), "-", parameter.description(), parameter.category(), parameter.valueGuessedByDefault(), firstClass));
        }
        return globalParameters;
    }

    private class Category {
        private String title;
        private String description;

        public Category(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    private List<Category> getParametersCategories() throws MalformedURLException, ClassNotFoundException, DependencyResolutionRequiredException {
        List<Category> parametersCategories = new ArrayList<Category>();

        Set<Class<?>> parametersCategoriesAnnotatedTypes = AnnotationsHelper.getTypesAnnotatedWith(getBootstrapClass(), t3.plugin.annotations.Categories.class, ClasspathHelper.contextClassLoader(), getClassLoader(project, additionalClasspathEntries));
        Set<CategoryImpl> parametersCategoriesAnnotatations = CategoriesHelper.getCategories(parametersCategoriesAnnotatedTypes);

        for (CategoryImpl parameter : parametersCategoriesAnnotatations) {
            parametersCategories.add(new Category(parameter.getTitle(), parameter.getDescription()));
        }

        return parametersCategories;
    }

    protected class GlobalParameterComparator implements Comparator<GlobalParameter> {
        @Override
        public int compare(GlobalParameter o1, GlobalParameter o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }

            if (o1.category != null && o2.category != null) {
                int i = o1.category.compareTo(o2.category);
                if (i != 0) {
                    return i;
                }
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
        private String category;
        private boolean valueGuessedByDefault;

        private boolean firstClass;

        public GlobalParameter(String name, String type, String property, String defaultValue, String since, String description, String category, boolean valueGuessedByDefault, boolean firstClass) {
            this.name = name;
            this.type = type;
            this.property = property;
            this.defaultValue = defaultValue;
            this.since = since;
            this.description = description;
            this.category = category;
            this.valueGuessedByDefault = valueGuessedByDefault;

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
            if (this.valueGuessedByDefault) {
                clazz = clazz + " guessed";
            } else {
                clazz = clazz + " notguessed";
            }

            html.
            tr(class_(clazz)).
                td().
                    b().
                        a(id(name).href("#"+name)).write(property)._a()
                    ._b()
                ._td().
//                td().
//                    tt().write(defaultValue)._tt()
//                ._td().
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
                    html.p();
                    
                    if (description != null && !description.isEmpty()) {
                        html.write(replaceProperties(description), false).br();
                    }

                    if (name != null && !name.isEmpty()) {
                        if (description != null && !description.isEmpty()) {
                            html.br();
                        }
                        html.b().write("Parameter name is")._b().write(": " + name + ".");
                    }
                    if (property != null && !property.isEmpty()) {
                        if (name != null && !name.isEmpty() || description != null && !description.isEmpty()) {
                            html.br();
                        }
                        html.b().write("User property is")._b().write(": " + property + ".");
                    }
                    if (defaultValue != null && !defaultValue.isEmpty()) {
                        if (property != null && !property.isEmpty() || name != null && !name.isEmpty() || description != null && !description.isEmpty()) {
                            html.br();
                        }
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
            p().em().write("By default, only parameters which cannot be guessed are displayed. ")._em().
            a(href("#").id("toggleGuessed")).write("Show other parameters")._a().write(" to customize default values.")._p();

            String category = null;
            for (GlobalParameter globalParameter : globalParameters) {
                boolean changingCategory = (globalParameter.category != null && !globalParameter.category.equals(category) && !globalParameter.category.isEmpty()) || (category == null);
                if (changingCategory) {
                    if (category != null) {
                        html._tbody()._table()._div();
                    }

                    if (globalParameter.category != null && !globalParameter.category.isEmpty()) {
                        boolean hasAtLeastOneNotGuessedProperty = hasAtLeastOneNotGuessedProperty(globalParameter.category, globalParameters);
                        String clazz;
                        if (hasAtLeastOneNotGuessedProperty) {
                            clazz = "notguessed";
                        } else {
                            clazz = "guessed";
                        }

                        html.div(class_(clazz));//.p().write("TEST")._p()._div();
                        html.h4().write(globalParameter.category)._h4();

                        for (Category c : parametersCategories) {
                            if (c.title.equals(globalParameter.category)) {
                                html.p().write(c.description)._p();
                            }
                        }
                    } else {
                        boolean hasAtLeastOneNotGuessedProperty = hasAtLeastOneNotGuessedProperty(globalParameter.category, globalParameters);
                        String clazz;
                        if (hasAtLeastOneNotGuessedProperty) {
                            clazz = "notguessed";
                        } else {
                            clazz = "guessed";
                        }

                        html.div(class_(clazz)); // todo : manage uncategorized with hasAtLeastOneNotGuessedProperty
                        html.h4().write("Uncategorized")._h4();
                    }
                    html.table(border("0").class_("bodyTable table table-striped table-hover")).
                        thead().
                            tr(class_("a")).
                                th().write("Property")._th().
//                                th().write("Default value")._th().
                                th().write("Type")._th().
                                th().write("Since")._th().
                                th().write("Description")._th().
                            _tr().
                        _thead().
                        tbody();
                }

                html.render(globalParameter);
                category = globalParameter.category;
            }

        html._tbody()._table()._div()._div();

        return html;
    }

    private String getFullSampleProfile() {
        String result = "";

        result += "&lt;profile>&#xa;";
        result += "  &lt;id>" + project.getArtifactId() + "&lt;/id>&#xa;";
        result += "  &lt;properties>&#xa;";

        for (GlobalParameter globalParameter : globalParameters) {
            if (!globalParameter.valueGuessedByDefault) {
                result += "    &lt;" + globalParameter.property + ">[...]&lt;/" + globalParameter.property + ">&#xa;";
            }
        }

        result += "  &lt;/properties>&#xa;";
        result += "&lt;/profile>";

        return result;
    }

    public org.apache.maven.settings.Profile getFullSampleProfile(String id, Properties profileProperties) {
        org.apache.maven.settings.Profile profile = new org.apache.maven.settings.Profile();
        profile.setId(id);

        for (GlobalParameter globalParameter : globalParameters) {
            if (!globalParameter.valueGuessedByDefault) {
                String value;
                if (profileProperties.containsKey(globalParameter.property)) {
                    value = profileProperties.getProperty(globalParameter.property);
                } else {
                    value = "[...]";
                }

                profile.getProperties().put(globalParameter.property, value);
            }
        }

        return profile;
    }

    public String getFullSampleProfileForCommandLine(String id, Properties profileProperties) {
        return getFullSampleProfileForCommandLine(id, "", profileProperties);
    }

    public String getFullSampleProfileForCommandLine(String id) {
        return getFullSampleProfileForCommandLine(id, "", new Properties());
    }

    public String getFullSampleProfileForCommandLine(String id, String linePrefix) {
        return getFullSampleProfileForCommandLine(id, linePrefix, new Properties());
    }

    public String getFullSampleProfileForCommandLine(String id, String linePrefix, Properties profileProperties) {
        String result = "";

        result += linePrefix + "<profile>\n";
        result += linePrefix + "  <id>" + id + "</id>\n";
        result += linePrefix + "  <properties>\n";

        for (GlobalParameter globalParameter : globalParameters) {
            if (!globalParameter.valueGuessedByDefault) {
                String value;
                if (profileProperties.containsKey(globalParameter.property)) {
                    value = profileProperties.getProperty(globalParameter.property);
                } else {
                    value = "[...]";
                }

                result += linePrefix + "    <" + globalParameter.property + ">" + value + "</" + globalParameter.property + ">\n";
            }
        }

        result += linePrefix + "  </properties>\n";
        result += linePrefix + "</profile>";

        return result;
    }

    private HtmlCanvas generateSampleProfile(HtmlCanvas html) throws IOException {
        this.project.getProperties().put("data-clipboard-text", getFullSampleProfile());
        this.project.getProperties().put("config-title", "Sample profile for Maven settings.xml");

        String templateStart = replaceProperties(replaceProperties("${configTextStart}"));
        String templateEnd = replaceProperties(replaceProperties("${configTextEnd}"));

        html.write(templateStart, false);

        html.
            pre(class_("xml")).
                write("<profile>\n").
                write("  <id>" + project.getArtifactId() + "</id>\n").
                write("  <properties>\n");

        for (GlobalParameter globalParameter : globalParameters) {
            if (!globalParameter.valueGuessedByDefault) {
                html.write("    <" + globalParameter.property + ">[...]</" + globalParameter.property + ">\n");
            }
        }

        html.
                write("  </properties>\n").
                write("</profile>")
            ._pre();

        html.write(templateEnd, false);

        return html;
    }

    private HtmlCanvas generateSampleProfileDocumentation(HtmlCanvas html) throws IOException {
        HtmlCanvas sampleProfile = new HtmlCanvas();
        sampleProfile = generateSampleProfile(sampleProfile);

        html.
        div(class_("section")).
            h3(id("Sample_Profile")).write("Sample Profile")._h3().
            p().em().write("Based on above properties, here is a sample profile to include in ").a(href("https://maven.apache.org/settings.html")).write("Maven settings.xml file")._a().write(":")._em()._p().
            write(sampleProfile.toHtml(), false).
            write(sampleProfileCommandLineGenerator, false)
        ._div();

        addPropertyInSessionRequest("sampleProfile", sampleProfile.toHtml());

        return html;
    }

    private HtmlCanvas generateGlobalDocumentation(HtmlCanvas html) throws IOException {
        html.

        div(class_("row")).
            div(class_("span12")).
                div(class_("body-content")).
                    div(class_("section")).
                        div(class_("page-header")).
                            h2(id("Global_Documentation")).write("Global Documentation")._h2().
                            p().write("The global documentation describes parameters which are common to a group of projects.")._p();

        html.render(new Renderable() {
            @Override
            public void renderOn(HtmlCanvas html) throws IOException {
                generateGlobalParametersSection(html);
            }
        });

        html.render(new Renderable() {
            @Override
            public void renderOn(HtmlCanvas html) throws IOException {
                generateSampleProfileDocumentation(html);
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
        return generateGlobalDocumentation(html);
    }

    @Override
    public String getPageName() {
        return pageName;
    }

    public void init() throws MojoExecutionException {
        try {
            globalParameters = getGlobalParameters();
            parametersCategories = getParametersCategories();
            Collections.sort(globalParameters, new GlobalParameterComparator());
        } catch (Throwable e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (bootstrapClass == null || bootstrapClass.isEmpty()) return; // skip

        init();

        super.execute();
    }

    public static GenerateGlobalParametersDocMojo standaloneGenerator(MavenProject project, String bootstrapClass, List<File> additionalClasspathEntry) {
        GenerateGlobalParametersDocMojo generateGlobalParametersDocMojo = new GenerateGlobalParametersDocMojo();
        generateGlobalParametersDocMojo.setProject(project);
        generateGlobalParametersDocMojo.bootstrapClass = bootstrapClass;

        if (additionalClasspathEntry != null) {
            for (File file : additionalClasspathEntry) {
                try {
                    additionalClasspathEntries.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    // nothing
                }
            }
        }

        try {
            generateGlobalParametersDocMojo.init();
        } catch (MojoExecutionException e) {
            return null; // and deal with it ;p
        }
        return generateGlobalParametersDocMojo;
    }

    public static GenerateGlobalParametersDocMojo standaloneGenerator(MavenProject project, Class<?> bootstrapClass) {
        GenerateGlobalParametersDocMojo generateGlobalParametersDocMojo = new GenerateGlobalParametersDocMojo();
        generateGlobalParametersDocMojo.setProject(project);
        generateGlobalParametersDocMojo.setBootstrapClass(bootstrapClass);
        try {
            generateGlobalParametersDocMojo.init();
        } catch (MojoExecutionException e) {
            return null; // and deal with it ;p
        }
        return generateGlobalParametersDocMojo;
    }
}
