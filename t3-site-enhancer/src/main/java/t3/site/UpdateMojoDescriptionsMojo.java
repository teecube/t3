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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;
import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;
import t3.site.parameters.SubMenu;
import t3.site.parameters.TopMenu;

import javax.lang.model.element.Element;
import javax.tools.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.rendersnake.HtmlAttributesFactory.class_;
import static org.rendersnake.HtmlAttributesFactory.href;
import static org.rendersnake.HtmlAttributesFactory.summary;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Mojo(name = "update-mojo-descriptions", defaultPhase = LifecyclePhase.POST_SITE, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class UpdateMojoDescriptionsMojo extends AbstractReplaceAllMojo {

    public static Map<String, File> sourceFiles = null;

    @Override
    public void processHTMLFile(File htmlFile) throws MojoExecutionException {
        if (htmlFile != null && htmlFile.getName().endsWith("-mojo.html")) {
            String name = htmlFile.getName().replace("-mojo.html", "");
            File sourceFile = sourceFiles.get(name);
            if (sourceFile != null && sourceFile.exists()) {
                JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
                StandardJavaFileManager sfm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
                try {
                    sfm.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(sourceDirectory, new File(sourceDirectory.getParentFile(), "jaxb")));
                    List<File> classPathElements = new ArrayList<File>();
                    for (String runtimeClasspathElement : project.getRuntimeClasspathElements()) {
                        classPathElements.add(new File(runtimeClasspathElement));
                    }
                    sfm.setLocation(StandardLocation.CLASS_PATH, classPathElements);
                } catch (IOException | DependencyResolutionRequiredException e) {
                    e.printStackTrace();
                }

                List<String> options = new ArrayList<String>();
                try {
                    for (String runtimeClasspathElement : project.getRuntimeClasspathElements()) {
                        options.add("-classpath");
                        options.add(new File(runtimeClasspathElement).toURI().toURL().toExternalForm());
                    }
                } catch (DependencyResolutionRequiredException | MalformedURLException e) {
                    e.printStackTrace();
                }

                DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
                Iterable<? extends JavaFileObject> compilationUnits = sfm.getJavaFileObjects(sourceFile);

                JavaCompiler.CompilationTask task = compiler.getTask(null, sfm, diagnostics, options, null, compilationUnits);
                //task.call();
                try {
//                    Method analyzeMethod = task.getClass().getDeclaredMethod("analyze");
//                    Iterable<? extends Element> result = (Iterable<? extends Element>) analyzeMethod.invoke(task);
                    Method parseMethode = task.getClass().getDeclaredMethod("parse");
                    Iterable<? extends JCTree.JCCompilationUnit> result = (Iterable<? extends JCTree.JCCompilationUnit>) parseMethode.invoke(task);
                    for (JCTree.JCCompilationUnit cu : result) {
                        System.out.println(cu.getKind());
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                JavacElements elements = ((JavacTaskImpl) task).getElements();

                JavacTrees trees = JavacTrees.instance(task);

                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    System.out.println(diagnostic.getCode());
                }
                for (JavaFileObject compilationUnit : compilationUnits) {
                    System.out.println(compilationUnit.getKind());
                }

                System.out.println(sourceFile.getAbsolutePath());
            }
        }
    }

    @Parameter(defaultValue = "${project.build.sourceDirectory}")
    File sourceDirectory;

    public ClassLoader getClassLoader() throws MalformedURLException, DependencyResolutionRequiredException {
        List<String> classpathElements = project.getRuntimeClasspathElements();

        List<URL> projectClasspathList = new ArrayList<URL>();
        for (String element : classpathElements) {
            projectClasspathList.add(new File(element).toURI().toURL());
        }

        URLClassLoader loader = new URLClassLoader(projectClasspathList.toArray(new URL[0]));
        return loader;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (sourceFiles == null) {
            sourceFiles = new HashMap<String, File>();
            Collection<File> mojoFiles = FileUtils.listFiles(sourceDirectory, new WildcardFileFilter("*Mojo.java"), TrueFileFilter.INSTANCE);

            String mojoPattern = "@Mojo\\s*\\(\\s*name\\s*=\\s*\"(.*)\"";
            Pattern pattern = Pattern.compile(mojoPattern);

            for (File mojoFile : mojoFiles) {
                try (Scanner scan = new Scanner(mojoFile)) {
                    while (scan.hasNextLine()) {
                        String str = scan.findInLine(mojoPattern);
                        if (str != null) {
                            Matcher m = pattern.matcher(str);
                            if (m.matches()) {
                                String name = m.group(1);
                                sourceFiles.put(name, mojoFile);
                            }
                        }
                        scan.nextLine();
                    }
                } catch (FileNotFoundException e) {
                    // ignore
                }
            }
        }
        outputDirectory.mkdirs();
        super.execute();
    }
}
