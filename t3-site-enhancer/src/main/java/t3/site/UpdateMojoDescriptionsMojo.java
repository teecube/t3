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
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.joox.JOOX;
import org.joox.Match;
import org.reflections.util.ClasspathHelper;
import t3.plugin.annotations.GlobalParameter;
import t3.plugin.annotations.helpers.AnnotationsHelper;
import t3.plugin.annotations.helpers.ParametersHelper;
import t3.plugin.annotations.impl.ParameterImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Mojo(name = "update-mojo-descriptions", defaultPhase = LifecyclePhase.POST_SITE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class UpdateMojoDescriptionsMojo extends AbstractReplaceAllMojo {

    @Parameter(defaultValue = "${project.build.sourceDirectory}")
    File sourceDirectory;

    public static Map<String, String> classOfGoal = null;
    public static Map<String, List<GlobalParameter>> globalParametersOfClass = null;
    public static Map<String, List<t3.plugin.annotations.Parameter>> parametersOfClass = null;

    @Override
    public void processHTMLFile(File htmlFile) throws MojoExecutionException {
        if (htmlFile != null && htmlFile.getName().endsWith("-mojo.html")) {
            addHTMLEntities(htmlFile);
            try {
                Match document = JOOX.$(htmlFile);

                String goalName = htmlFile.getName().replace("-mojo.html", "");
                String canonicalClassName = classOfGoal.get(goalName);
                if (canonicalClassName != null) {
                    List<t3.plugin.annotations.Parameter> parameters = parametersOfClass.get(canonicalClassName);
                    if (parameters != null) {
                        for (t3.plugin.annotations.Parameter parameter : parameters) {
                            String description = parameter.description();
                            if (StringUtils.isNotEmpty(description)) {
                                if (parameter.description().equals(parameter.property())) continue;

                                Match td = document.xpath("//tr[./td/b/a/@href='#" + ((ParameterImpl)parameter).field() + "']/td[4]");
                                String content = td.content();
                                content = content.replace("(no description)", parameter.description());
                                td.content(content);

                                Match div = document.xpath("//p[./b/a/@name='" + ((ParameterImpl)parameter).field() + "']/following-sibling::div[1]");
                                div.content(parameter.description());
                            }
                        }
                        printDocument(document.document(), htmlFile);
                    }
                }

            } catch (Exception e) {
                removeHTMLEntities(htmlFile);
                return;
            } finally {
                removeHTMLEntities(htmlFile);
            }
        }
    }

    private interface ScanFileForPatternCallback {
        void apply(File file, Matcher m);
    }

    private void scanFileForPattern(File file, String pattern, ScanFileForPatternCallback callback) {
        Pattern p = Pattern.compile(pattern);
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String str = scan.findInLine(pattern);
                if (str != null) {
                    Matcher m = p.matcher(str);
                    if (m.matches()) {
                        callback.apply(file, m);
                    }
                }
                scan.nextLine();
            }
        } catch (FileNotFoundException e) {
            // ignore
        }
    }

    private String getCanonicalClassNameFromSourceFile(File sourceFile) {
        if (sourceFile == null) return null;

        final String[] canonicalClassName = new String[1];

        scanFileForPattern(sourceFile, "\\s*package\\s*([^\\s]*)\\s*;", new ScanFileForPatternCallback() {
            @Override
            public void apply(File file, Matcher m) {
                if (canonicalClassName[0] != null) return;

                String packageName = m.group(1);
                final String[] className = new String[1];

                scanFileForPattern(file, "public\\s*class\\s*([^\\s]*)", new ScanFileForPatternCallback() {
                    @Override
                    public void apply(File file, Matcher m) {
                        className[0] = m.group(1).toString();
                    }
                });

                canonicalClassName[0] = packageName + "." + className[0];
            }
        });

        return canonicalClassName[0];
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sourceDirectory == null || !sourceDirectory.exists()) return;

        if (classOfGoal == null) {
            classOfGoal = new HashMap<String, String>();
            globalParametersOfClass = new HashMap<String, List<GlobalParameter>>();
            parametersOfClass = new HashMap<String, List<t3.plugin.annotations.Parameter>>();

            Collection<File> mojoFiles = FileUtils.listFiles(sourceDirectory, new WildcardFileFilter("*Mojo.java"), TrueFileFilter.INSTANCE);

            String mojoPattern = "@Mojo\\s*\\(\\s*name\\s*=\\s*\"(.*)\"";
            Pattern pattern = Pattern.compile(mojoPattern);

            for (File mojoFile : mojoFiles) {
                scanFileForPattern(mojoFile, mojoPattern, new ScanFileForPatternCallback() {
                    @Override
                    public void apply(File file, Matcher m) {
                        String goalName = m.group(1);
                        classOfGoal.put(goalName, getCanonicalClassNameFromSourceFile(file));
                    }
                });
            }

            if (classOfGoal.isEmpty()) {
                return;
            }

            String className = classOfGoal.entrySet().iterator().next().getValue();
            try {
                ClassLoader projectClassLoader = getClassLoader(project);
                Class<?> loadedClass = projectClassLoader.loadClass(className);

                Set<Field> fieldsWithParameterAnnotation = AnnotationsHelper.getFieldsAnnotatedWith(loadedClass, Parameter.class, ClasspathHelper.contextClassLoader(), projectClassLoader);

                for (Field field : fieldsWithParameterAnnotation) {
                    Set<ParameterImpl> parameters = ParametersHelper.getFieldAnnotatedWith(field, t3.plugin.annotations.Parameter.class);
                    for (ParameterImpl parameter : parameters) {
                        String canonicalName = field.getDeclaringClass().getCanonicalName();
                        List<t3.plugin.annotations.Parameter> parametersForThisClass = parametersOfClass.get(canonicalName);
                        if (parametersForThisClass == null) {
                            parametersForThisClass = new ArrayList<t3.plugin.annotations.Parameter>();
                            parametersOfClass.put(canonicalName, parametersForThisClass);
                        }
                        parametersForThisClass.add(parameter);
                    }
                }

                Set<Field> fieldsWithGlobalParameterAnnotation = AnnotationsHelper.getFieldsAnnotatedWith(loadedClass, GlobalParameter.class, ClasspathHelper.contextClassLoader(), projectClassLoader);

                for (Field field : fieldsWithGlobalParameterAnnotation) {
                    GlobalParameter globalParameter = field.getAnnotation(GlobalParameter.class);

                    String canonicalName = field.getDeclaringClass().getCanonicalName();
                    List<GlobalParameter> globalParametersForThisClass = globalParametersOfClass.get(canonicalName);
                    if (globalParametersForThisClass == null) {
                        globalParametersForThisClass = new ArrayList<GlobalParameter>();
                        globalParametersOfClass.put(canonicalName, globalParametersForThisClass);
                    }
                    globalParametersForThisClass.add(globalParameter);
                }
            } catch (ClassNotFoundException | MalformedURLException | DependencyResolutionRequiredException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }

        }
        outputDirectory.mkdirs();
        super.execute();
    }
}