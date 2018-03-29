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
package t3;

import t3.plugin.annotations.Categories;
import t3.plugin.annotations.Category;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Categories({
    @Category(title = CommonMojoInformation.mavenCategory, description = CommonMojoInformation.mavenCategory_description),
    @Category(title = CommonMojoInformation.systemCategory, description = CommonMojoInformation.systemCategory_description),
    @Category(title = CommonMojoInformation.tibcoCategory, description = CommonMojoInformation.tibcoCategory_description),
})
public class CommonMojoInformation {

    /* Categories */
    public static final String mavenCategory = "Standard Maven";
    public static final String mavenCategory_description = "Default built-in Maven properties belong to the Standard Maven category";

    public static final String systemCategory = "System";
    public static final String systemCategory_description = "System properties depends on environment (Linux, Windows...)";

    public static final String tibcoCategory = "TIBCO";
    public static final String tibcoCategory_description = "Properties are specific to TIBCO settings";

    /* TIBCO home */
    public static final String tibcoHome = "tibco.home";
    public static final String tibcoHome_description = "The path of a valid TIBCO installation to use with the plugin.";

    /* TIBCO JRE home */
    public static final String jreHome = "tibco.jre.home";
    public static final String jreHome_default = "${tibco.home}/tibcojre/${tibco.jre.version}";
    public static final String jreHome_description = "The path where TIBCO JRE 32bit is installed.";

    public static final String jreVersion = "tibco.jre.version";
    public static final String jreVersion_description = "The TIBCO JRE version (as defined by its directory).";

    public static final String jre64Home = "tibco.jre64.home";
    public static final String jre64Home_default = "${tibco.home}/tibcojre64/${tibco.jre64.version}";
    public static final String jre64Home_description = "The path where TIBCO JRE 64bit is installed.";

    public static final String jre64Version = "tibco.jre64.version";
    public static final String jre64Version_description = "The TIBCO JRE version (as defined by its directory).";

    /* platform dependent properties */
    public static final String executablesExtension = "executables.extension";
    public static final String executablesExtension_description = "The extension string for executables files on current system (might be empty for *nix or '.exe' for Windows).";

    public static final String platformArch = "platform.arch";
    public static final String platformArch_description = "The architecture of running platform (x86, x86_64, ia64, ppc, ...)";

    public static final String platformOs = "platform.os";
    public static final String platformOs_description = "The Operating System of running platform (linux, win, ...)";

    /* built-in Maven properties */
    public static final String directory_description = "The directory used by Maven to copy temporary files for builds (by default it is well-known 'target' directory).";
    public static final String outputDirectory_description = "The directory used by Maven to copy artifacts created by builds.";
    public static final String testOutputDirectory_description = "The directory used by Maven for all files related to tests.";
    public static final String sourceEncoding_description = "Charset encoding used to read source files of projects.";

    public static final String classifier_description = "Classifier of the generated artifact.";
    public static final String finalName_description = "Name of the generated artifact (without file extension).";

}
