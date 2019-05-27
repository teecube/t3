/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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
package t3.utils;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This class handles POM file manipulation using the Maven API.
 * </p>
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class POMManager {

    /**
     * Load a Maven {@link Model} object from a POM file.
     *
     * @param pom
     * @return the model parsed from the POM file
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static Model getModelFromPOM(File pom) throws IOException, XmlPullParserException {
        Model model = null;
        FileInputStream fis = null;
        InputStreamReader isr = null;
        try {
            fis = new FileInputStream(pom);
            isr = new InputStreamReader(fis, "utf-8"); // FIXME
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(isr);
        } finally {
            try {
                isr.close();
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return model;
    }

    public static String getRepositoryPathFromGroupId(String groupId) {
        if (groupId == null) return null;

        return groupId.replace(".", File.separator);
    }

    public static Model getModelOfModule(MavenProject mavenProject, String module, ArtifactRepository localRepository) throws IOException, XmlPullParserException {
        if (mavenProject == null || module == null || module.trim().isEmpty()) {
            return null;
        }

        File pom = new File(mavenProject.getBasedir(), module);
        if (pom == null || !pom.exists()) {
            // maybe the POM is in the local repository, assuming that module == artifactId
            if (localRepository != null) {
                String repositoryPOMFileName = localRepository.getBasedir() + File.separator
                                             + getRepositoryPathFromGroupId(mavenProject.getGroupId())  + File.separator
                                             + module + File.separator
                                             + mavenProject.getVersion() + File.separator
                                             + module + "-" + mavenProject.getVersion() + ".pom";
                pom = new File(repositoryPOMFileName);
                if (pom == null || !pom.exists()) {
                    return null;
                }
            } else {
                return null;
            }
        }

        if (pom.isDirectory()) {
            pom = new File(pom, "pom.xml");
        }

        if (pom == null || !pom.exists()) {
            return null;
        }

        return getModelFromPOM(pom);
    }

    public static Model getModelOfModule(MavenProject mavenProject, String module) throws IOException, XmlPullParserException {
        return getModelOfModule(mavenProject, module, null);
    }

    /**
     * Write a Maven {@link Model} object to a POM file.
     *
     * @param model
     * @param pom
     * @throws IOException
     */
    public static void writeModelToPOM(Model model, File pom) throws IOException {
        FileOutputStream fos = new FileOutputStream(pom);

        new MavenXpp3Writer().write(fos, model);

        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add the Maven dependency to a POM file.
     *
     * @param pom
     * @param dependency
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void addDependency(File pom, Dependency dependency) throws IOException, XmlPullParserException {
        Model model = getModelFromPOM(pom);

        model.addDependency(dependency);

        writeModelToPOM(model, pom);
    }

    /**
     * Add the Maven dependency to a POM file (in management section).
     *
     * @param pom
     * @param dependency
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void addDependencyManagement(File pom, Dependency dependency) throws IOException, XmlPullParserException {
        Model model = getModelFromPOM(pom);

        DependencyManagement dMgmt = model.getDependencyManagement();
        if (dMgmt == null) {
            model.setDependencyManagement(new DependencyManagement());
            dMgmt = model.getDependencyManagement();
        }
        dMgmt.addDependency(dependency);

        writeModelToPOM(model, pom);
    }

    /**
     * Remove the Maven dependency from a POM file.
     *
     * @param pom
     * @param dependency
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void removeDependency(File pom, Dependency dependency) throws IOException, XmlPullParserException {
        Model model = getModelFromPOM(pom);

        for (Iterator<Dependency> it = model.getDependencies().iterator(); it.hasNext();){
            if (dependenciesEqual(it.next(), dependency)) {
                it.remove();
            }
        }

        writeModelToPOM(model, pom);
    }

    /**
     * Remove the Maven dependency from a POM file (in management section).
     *
     * @param pom
     * @param dependency
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void removeDependencyManagement(File pom, Dependency dependency) throws IOException, XmlPullParserException {
        Model model = getModelFromPOM(pom);

        DependencyManagement dMgmt = model.getDependencyManagement();
        if (dMgmt == null) {
            model.setDependencyManagement(new DependencyManagement());
            dMgmt = model.getDependencyManagement();
        }

        for (Iterator<Dependency> it = dMgmt.getDependencies().iterator(); it.hasNext();){
            if (dependenciesEqual(it.next(), dependency)) {
                it.remove();
            }
        }

        writeModelToPOM(model, pom);
    }

    public static void removeParent(File pom) throws IOException, XmlPullParserException {
        Model model = getModelFromPOM(pom);

        model.setParent(null);

        writeModelToPOM(model, pom);
    }

    private static boolean dependenciesEqual(Dependency d1, Dependency d2) {
        boolean result = true;

        if (d1 == null || d2 == null) {
            return d1 == d2;
        }

        result = result && d1.getGroupId().equals(d2.getGroupId());
        result = result && d1.getArtifactId().equals(d2.getArtifactId());
        if (d1.getVersion() != null) {
            result = result && d1.getVersion().equals(d2.getVersion());
        } else {
            result = result && (d2.getVersion() == null);
        }
        if (d1.getType() != null) {
            result = result && d1.getType().equals(d2.getType());
        } else {
            result = result && (d2.getType() == null);
        }
        if (d1.getClassifier() != null) {
            result = result && d1.getClassifier().equals(d2.getClassifier());
        } else {
            result = result && (d2.getClassifier() == null);
        }

        return result;
    }

    /**
     * Check whether a dependency exists in a list of dependencies.
     *
     * @param dependency
     * @param dependencies
     * @return true if the dependency exists in dependencies list
     */
    private static boolean dependencyExists(Dependency dependency, List<Dependency> dependencies) {
        for (Dependency d : dependencies) {
            if (dependenciesEqual(dependency, d)) {
                return true;
            }
        }
        return false;

    }

    /**
     * Check whether a dependency exists in a POM.
     *
     * @param pom
     * @param dependency
     * @return true if the dependency exists in the POM
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static boolean dependencyExists(File pom, Dependency dependency) throws IOException, XmlPullParserException {
        Model model = getModelFromPOM(pom);

        return dependencyExists(dependency, model.getDependencies());
    }

    /**
     * Check whether a dependency exists in a POM (in management section).
     *
     * @param pom
     * @param dependency
     * @return true if the dependency exists in the management section of the
     * POM
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static boolean dependencyExistsManagement(File pom, Dependency dependency) throws IOException, XmlPullParserException {
        Model model = getModelFromPOM(pom);

        DependencyManagement dMgmt = model.getDependencyManagement();
        if (dMgmt == null) {
            model.setDependencyManagement(new DependencyManagement());
            dMgmt = model.getDependencyManagement();
        }

        return dependencyExists(dependency, dMgmt.getDependencies());
    }

    public static Profile getProfile(Model model, String profileId) {
        if (model == null || profileId == null || profileId.isEmpty()) return null;
        for (Profile profile : model.getProfiles()) {
            if (profileId.equals(profile.getId())) {
                return profile;
            }
        }

        Profile result = new Profile();
        result.setId(profileId);
        model.addProfile(result);
        return result;
    }

    public static File getPomOfModule(File pom, String relativePath) throws FileNotFoundException {
        if (pom == null || relativePath == null) return null; // throw ?

        File result = new File(pom.getParentFile(), relativePath);
        if (result.exists() && result.isDirectory()) {
            result = new File(result, "pom.xml");
        }

        if (!result.exists() || !result.isFile() || !"pom.xml".equals(result.getName())) {
            throw new FileNotFoundException("Unable to find POM of module to add in '" + relativePath + "'");
        }

        return result;
    }

    public static boolean moduleExists(File pom, String relativePath) throws IOException, XmlPullParserException {
        if (pom == null || relativePath == null) return false;

        Model model = getModelFromPOM(pom);
        relativePath = relativePath.replace("\\", "/");
        File modulePomToAdd = getPomOfModule(pom, relativePath);

        for (String module : model.getModules()) {
            File modulePom = getPomOfModule(pom, module);
            if (java.nio.file.Files.isSameFile(modulePom.toPath(), modulePomToAdd.toPath())) {
                return true; // exists
            }
        }

        return false;
    }

    public static Model removeModule(File pom, Model model, String relativePath, String profileId) throws IOException {


        return model;
    }

    public static void removeProjectAsModule(File pom, String relativePath,    String profileId) throws IOException, XmlPullParserException {
        if (relativePath == null) return;

        Model model = getModelFromPOM(pom);
        relativePath = relativePath.replace("\\", "/");
        File modulePomToRemove = getPomOfModule(pom, relativePath);

        if (profileId != null && !profileId.isEmpty()) {
            Profile p = getProfile(model, profileId);
            if (p != null) {
                for (String module : p.getModules()) {
                    File modulePom = getPomOfModule(pom, module);
                    if (java.nio.file.Files.isSameFile(modulePom.toPath(), modulePomToRemove.toPath())) {
                        p.removeModule(module);

                        break;
                    }
                }

            }
        } else {
            for (String module : model.getModules()) {
                File modulePom = getPomOfModule(pom, module);
                if (java.nio.file.Files.isSameFile(modulePom.toPath(), modulePomToRemove.toPath())) {
                    model.removeModule(module);

                    break;
                }
            }
        }

        writeModelToPOM(model, pom);
    }

    /**
     * Add a project as a module.
     *
     * @param pom
     * @param relativePath
     * @throws IOException
     * @throws XmlPullParserException
     */
    public static void addProjectAsModule(File pom, String relativePath, String profileId, boolean ignoreIfExists) throws IOException, XmlPullParserException {
        if (relativePath == null) return;

        Model model = getModelFromPOM(pom);
        relativePath = relativePath.replace("\\", "/");

        if (profileId != null && !profileId.isEmpty()) {
            Profile p = getProfile(model, profileId);
            if (p != null) {
                if (ignoreIfExists && moduleExists(pom, relativePath)) {
                    return; // ignore
                }
                p.addModule(relativePath);
            }
        } else {
            if (ignoreIfExists && moduleExists(pom, relativePath)) {
                return; // ignore
            }
            model.addModule(relativePath);
        }

        writeModelToPOM(model, pom);
    }

    /**
     * Check whether a module exists in a POM.
     *
     * @param pom
     * @param relativePath
     * @param profileId
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public static boolean moduleExists(File pom, String relativePath, String profileId) throws IOException, XmlPullParserException {
        if (relativePath == null) return false;

        Model model = getModelFromPOM(pom);
        relativePath = relativePath.replace("\\", "/");

        if (profileId != null && !profileId.isEmpty()) {
            Profile p = getProfile(model, profileId);
            if (p != null) {
                return p.getModules().indexOf(relativePath) >= 0;
            }
        } else {
            return model.getModules().indexOf(relativePath) >= 0;
        }

        return false;
    }

}
