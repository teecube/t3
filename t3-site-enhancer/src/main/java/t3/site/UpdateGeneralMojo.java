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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.joox.JOOX;
import org.joox.Match;
import org.rendersnake.HtmlCanvas;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import t3.site.parameters.DependencyWithAdditionalArguments;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Mojo(name = "update-general", defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateGeneralMojo extends AbstractReplaceAllMojo {

    @Parameter
    List<DependencyWithAdditionalArguments> archetypes;

    @Override
    public void processHTMLFile(File htmlFile) throws Exception {
        replaceProperty(htmlFile, "siteURL2", "siteURL", true, true, true); // TODO: externalize in configuration ?

        for (String propertyToUpdate : siteProperties) {
            boolean propertyInRootProject = fromRootParentProperties.contains(propertyToUpdate);
            boolean onlyInOriginalModel = inOriginalModelProperties.contains(propertyToUpdate);
            boolean lookInSettings = lookInSettingsProperties.contains(propertyToUpdate);
            replaceProperty(htmlFile, propertyToUpdate, propertyToUpdate, propertyInRootProject, onlyInOriginalModel, lookInSettings);
        }

        addHTMLEntities(htmlFile);
        try {
            addSocial(htmlFile);
            fixLinks(htmlFile);
            processCommandLines(htmlFile);
            fixFooter(htmlFile);
            removeIgnoredParameters(htmlFile);
        } catch (Exception e) {
            removeHTMLEntities(htmlFile);
            return;
        } finally {
            removeHTMLEntities(htmlFile);
        }
    }

    private void removeIgnoredParameters(File htmlFile) throws Exception {
        Match document = JOOX.$(htmlFile);

        // remove ignored parameters
        Match tr = document.xpath("//tr[./td/tt/a/@href='#ignoredParameters']");
        tr.remove();

        Match div = document.xpath("//div[@class='section'][./h4/@id='a.3CignoredParameters.3E']");
        div.remove();

        printDocument(document.document(), htmlFile);
    }

    private void addSocial(File htmlFile) throws Exception {
        String socialLinks = this.getPropertyValue("socialLinks");
        if (socialLinks == null || socialLinks.isEmpty()) {
            return;
        }

        Match document = JOOX.$(htmlFile);

        // add non endorsement warning
        document.xpath("//ul[@class='nav pull-right']").prepend(socialLinks);

        printDocument(document.document(), htmlFile);
    }

    private void fixFooter(File htmlFile) throws Exception  {
        Match document = JOOX.$(htmlFile);

        // add non endorsement warning
        document.xpath("//p[@class='copyright']").prepend(this.getPropertyValue("nonEndorsement") + "<br />");

        Match version = JOOX.$(JOOX.$(document.xpath("//p[@class='version-date']").get(0)));
        // add time to the date if we are in SNAPSHOT version
        Element projectVersion = version.xpath("//span[@class='projectVersion']").get(0);
        if (projectVersion != null && projectVersion.getTextContent().contains("SNAPSHOT")) {
            Element publishDate = version.xpath("//span[@class='publishDate']").get(0);
            if (publishDate != null) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                publishDate.setTextContent(publishDate.getTextContent().substring(0, publishDate.getTextContent().length()-2) + " " + sdf.format(cal.getTime()));
            }
        }

        printDocument(document.document(), htmlFile);
    }

    private void processCommandLines(File htmlFile) throws Exception {
        Match document = JOOX.$(htmlFile);
        Match lists = document.xpath("//div[@class='command']");
        for (Iterator<Element> iterator = lists.iterator(); iterator.hasNext();) {
            Element element = (Element) iterator.next();

            Match fullCommand = JOOX.$(JOOX.$(element).toString());
            Match command = fullCommand.xpath("//span[@id='command']");
            Match arguments = fullCommand.xpath("//span[@class='argument']");
            Match results = fullCommand.xpath("//span[@class='result']");
            Match t = fullCommand.xpath("//div[@class='command']");
            String title = null;
            if (t != null && t.get(0) != null) {
                title = t.get(0).getAttribute("title");
            }

            if (command != null && !command.isEmpty()) {
                String commandLine = command.get(0).getTextContent();
                List<String> args = new ArrayList<String>();
                if (arguments != null) {
                    for (Element arg : arguments) {
                        args.add(arg.getTextContent());
                    }
                }
                List<String> results_ = new ArrayList<String>();
                if (results != null) {
                    for (Element result : results) {
                        results_.add(result.getTextContent());
                    }
                }

                this.project.getProperties().put("data-clipboard-text", getFullCommandLine(commandLine , args));
                if (title == null || title.isEmpty()) {
                    title = "&#160;";
                }
                this.project.getProperties().put("command-title", title);

                String templateStart = replaceProperties(replaceProperties("${commandLineStart}"));
                String templateEnd = replaceProperties(replaceProperties("${commandLineEnd}"));

                HtmlCanvas commandLineHTML = createCommandLines(commandLine, templateStart, templateEnd, args, results_, true);

                Element newElement = JOOX.$(commandLineHTML.toHtml()).get(0);
                Node newElementImported = element.getOwnerDocument().importNode(newElement, true);
                element.getParentNode().appendChild(newElementImported);
                element.getParentNode().removeChild(element);
            }
        }
        printDocument(document.document(), htmlFile);
    }

    private void fixLinks(File htmlFile) throws Exception {
        Match document = JOOX.$(htmlFile);
        Match lists = document.xpath("//a");
        for (org.w3c.dom.Element element : lists) {
            Attr href = element.getAttributeNode("href");
            if (href != null) {
                String value = href.getValue();
                if (value != null) {
                    href.setValue(value.replaceAll("(?<!(http:|https:))[//]+", "/"));
                }
            }
        }
        printDocument(document.document(), htmlFile);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (archetypes == null) {
            archetypes = new ArrayList<DependencyWithAdditionalArguments>();
        }
        for (DependencyWithAdditionalArguments dependency : archetypes) {
            try {
                createArchetypesCommandLines(dependency, dependency.getArchetypeAdditionalArguments(), true);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
        }

        super.execute();

        removeParent(project); // avoid Doxia SiteTool to mess up with parent loading

        this.session.getRequest().getActiveProfiles().remove("_tmp-site");
    }

    private void removeParent(MavenProject project) {
        if (project == null) return;

        MavenProject parent = project.getParent();
        removeParent(parent);

        project.setFile(null);
    }

}
