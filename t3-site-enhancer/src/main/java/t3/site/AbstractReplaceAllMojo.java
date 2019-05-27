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
package t3.site;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.rendersnake.HtmlCanvas;
import t3.site.parameters.CommandLine;
import t3.site.parameters.Sample;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.rendersnake.HtmlAttributesFactory.class_;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public abstract class AbstractReplaceAllMojo extends AbstractSiteMojo {

    public abstract void processHTMLFile(File htmlFile) throws Exception;

    @Parameter
    List<Sample> samples;

    @Parameter
    List<CommandLine> commandLines;

    private HtmlCanvas generateSample(String title, String sample) throws IOException {
        HtmlCanvas html = new HtmlCanvas();

        this.project.getProperties().put("data-clipboard-text", sample.replace("\n", "&#xa;"));
        this.project.getProperties().put("config-title", title);

        String templateStart = replaceProperties(replaceProperties("${configTextStart}"));
        String templateEnd = replaceProperties(replaceProperties("${configTextEnd}"));

        html.write(templateStart, false);

        html.
        pre(class_("xml")).
            write(sample, false)
        ._pre();

        html.write(templateEnd, false);

        return html;
    }

    private HtmlCanvas generateCommandLine(String title, String commandLine, List<String> arguments, List<String> results) throws IOException {
        this.project.getProperties().put("data-clipboard-text", getFullCommandLine(commandLine, arguments));
        this.project.getProperties().put("command-title", "&#160;");

        String templateStart = replaceProperties(replaceProperties("${commandLineStart}"));
        String templateEnd = replaceProperties(replaceProperties("${commandLineEnd}"));

        return createCommandLines(commandLine, templateStart, templateEnd, arguments, results, true);
    }

    protected List<File> getHTMLFiles() throws IOException {
        FileSet htmlFiles = new FileSet();
        htmlFiles.setDirectory(outputDirectory.getAbsolutePath());

        htmlFiles.addInclude("**/*.html");
        htmlFiles.addExclude("apidocs/**/*");
        htmlFiles.addExclude("xref/**/*");

        return toFileList(htmlFiles);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        super.execute();

        if (siteProperties == null) {
            siteProperties = new ArrayList<String>();
        }
        if (fromRootParentProperties == null) {
            fromRootParentProperties = new ArrayList<String>();
        }
        if (inOriginalModelProperties == null) {
            inOriginalModelProperties = new ArrayList<String>();
        }
        if (lookInSettingsProperties == null) {
            lookInSettingsProperties = new ArrayList<String>();
        }

        if (staticSiteProperties == null) {
            staticSiteProperties = new ArrayList<String>();
        }
        if (staticLookInSettingsProperties == null) {
            staticLookInSettingsProperties = new ArrayList<String>();
        }

        lookInSettingsProperties.addAll(staticLookInSettingsProperties);
        siteProperties.addAll(staticSiteProperties);

        try {
            if (samples == null) {
                samples = new ArrayList<Sample>();
            }
            if (commandLines == null) {
                commandLines = new ArrayList<CommandLine>();
            }

            for (Sample sample : samples) {
                String sampleContent = generateSample(sample.getTitle(), sample.getContent()).toHtml();
                addPropertyInSessionRequest(sample.getProperty(), sampleContent);
            }
            for (CommandLine commandLine : commandLines) {
                if (commandLine.getArguments() == null) {
                    commandLine.setArguments(new ArrayList<String>());
                }
                if (commandLine.getResults() == null) {
                    commandLine.setResults(new ArrayList<String>());
                }
                String commandLineContent = generateCommandLine(commandLine.getTitle(), commandLine.getCommandLine(), commandLine.getArguments(), commandLine.getResults()).toHtml();
                addPropertyInSessionRequest(commandLine.getProperty(), commandLineContent);
            }
            
            List<File> htmlFiles = getHTMLFiles();

            for (File htmlFile : htmlFiles) {
                getLog().debug(htmlFile.getAbsolutePath());
                processHTMLFile(htmlFile);
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
