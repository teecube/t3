/**
 * (C) Copyright 2016-2016 teecube
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.FileSet;
import org.apache.maven.model.Profile;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.tools.ant.taskdefs.optional.ReplaceRegExp;
import org.joox.Context;
import org.joox.Filter;
import org.joox.JOOX;
import org.joox.Match;
import org.rendersnake.HtmlCanvas;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import t3.CommonMojo;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public abstract class AbstractSiteMojo extends CommonMojo {

	@Parameter (defaultValue = "${localRepository}", readonly = true, required = true)
	protected ArtifactRepository localRepository;

	@Parameter (property="project.build.sourceEncoding", defaultValue = "UTF-8")
	protected String sourceEncoding;

	@Parameter (property = "siteOutputDirectory", defaultValue = "${project.reporting.outputDirectory}")
	protected File outputDirectory;

	@Parameter
	protected List<String> siteProperties;

	protected static List<String> staticSiteProperties;

	@Parameter
	protected List<String> fromRootParentProperties;

	@Parameter
	protected List<String> inOriginalModelProperties;

	@Parameter
	protected List<String> lookInSettingsProperties;

	protected static List<String> staticLookInSettingsProperties;

	@Parameter (property="t3.site.autoclosingElements", defaultValue="//a|//b|//i|//span|//script")
	private String autoclosingElements;

	private static String toCommaSeparatedString(List<String> strings) {
		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(string);
		}
		return sb.toString();
	}

	protected static List<File> toFileList(FileSet fileSet) throws IOException {
		File directory = new File(fileSet.getDirectory());
		String includes = toCommaSeparatedString(fileSet.getIncludes());
		String excludes = toCommaSeparatedString(fileSet.getExcludes());
		return org.codehaus.plexus.util.FileUtils.getFiles(directory, includes, excludes);
	}

	protected String formatHtml(String html) throws MojoExecutionException {
		try {
			InputSource src = new InputSource(new StringReader(html));
			Node document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(src).getDocumentElement();
			Boolean keepDeclaration = Boolean.valueOf(html.startsWith("<?xml"));

			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
			LSSerializer serializer = impl.createLSSerializer();

			LSOutput lsOutput =  impl.createLSOutput();
			lsOutput.setEncoding(sourceEncoding);
			Writer stringWriter = new StringWriter();
			lsOutput.setCharacterStream(stringWriter);

			serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
			serializer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

			serializer.write(document, lsOutput);
			return stringWriter.toString();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
    }

	protected void addHTMLEntities(File htmlFile) {
		replaceByLine(htmlFile, "<!DOCTYPE html.*>", "<!DOCTYPE html [<!ENTITY nbsp \"&#160;\"> <!ENTITY iexcl \"&#161;\"> <!ENTITY cent \"&#162;\"> <!ENTITY pound \"&#163;\"> <!ENTITY curren \"&#164;\"> <!ENTITY yen \"&#165;\"> <!ENTITY brvbar \"&#166;\"> <!ENTITY sect \"&#167;\"> <!ENTITY uml \"&#168;\"> <!ENTITY copy \"&#169;\"> <!ENTITY ordf \"&#170;\"> <!ENTITY laquo \"&#171;\"> <!ENTITY not \"&#172;\"> <!ENTITY shy \"&#173;\"> <!ENTITY reg \"&#174;\"> <!ENTITY macr \"&#175;\"> <!ENTITY deg \"&#176;\"> <!ENTITY plusmn \"&#177;\"> <!ENTITY sup2 \"&#178;\"> <!ENTITY sup3 \"&#179;\"> <!ENTITY acute \"&#180;\"> <!ENTITY micro \"&#181;\"> <!ENTITY para \"&#182;\"> <!ENTITY middot \"&#183;\"> <!ENTITY cedil \"&#184;\"> <!ENTITY sup1 \"&#185;\"> <!ENTITY ordm \"&#186;\"> <!ENTITY raquo \"&#187;\"> <!ENTITY frac14 \"&#188;\"> <!ENTITY frac12 \"&#189;\"> <!ENTITY frac34 \"&#190;\"> <!ENTITY iquest \"&#191;\"> <!ENTITY Agrave \"&#192;\"> <!ENTITY Aacute \"&#193;\"> <!ENTITY Acirc \"&#194;\"> <!ENTITY Atilde \"&#195;\"> <!ENTITY Auml \"&#196;\"> <!ENTITY Aring \"&#197;\"> <!ENTITY AElig \"&#198;\"> <!ENTITY Ccedil \"&#199;\"> <!ENTITY Egrave \"&#200;\"> <!ENTITY Eacute \"&#201;\"> <!ENTITY Ecirc \"&#202;\"> <!ENTITY Euml \"&#203;\"> <!ENTITY Igrave \"&#204;\"> <!ENTITY Iacute \"&#205;\"> <!ENTITY Icirc \"&#206;\"> <!ENTITY Iuml \"&#207;\"> <!ENTITY ETH \"&#208;\"> <!ENTITY Ntilde \"&#209;\"> <!ENTITY Ograve \"&#210;\"> <!ENTITY Oacute \"&#211;\"> <!ENTITY Ocirc \"&#212;\"> <!ENTITY Otilde \"&#213;\"> <!ENTITY Ouml \"&#214;\"> <!ENTITY times \"&#215;\"> <!ENTITY Oslash \"&#216;\"> <!ENTITY Ugrave \"&#217;\"> <!ENTITY Uacute \"&#218;\"> <!ENTITY Ucirc \"&#219;\"> <!ENTITY Uuml \"&#220;\"> <!ENTITY Yacute \"&#221;\"> <!ENTITY THORN \"&#222;\"> <!ENTITY szlig \"&#223;\"> <!ENTITY agrave \"&#224;\"> <!ENTITY aacute \"&#225;\"> <!ENTITY acirc \"&#226;\"> <!ENTITY atilde \"&#227;\"> <!ENTITY auml \"&#228;\"> <!ENTITY aring \"&#229;\"> <!ENTITY aelig \"&#230;\"> <!ENTITY ccedil \"&#231;\"> <!ENTITY egrave \"&#232;\"> <!ENTITY eacute \"&#233;\"> <!ENTITY ecirc \"&#234;\"> <!ENTITY euml \"&#235;\"> <!ENTITY igrave \"&#236;\"> <!ENTITY iacute \"&#237;\"> <!ENTITY icirc \"&#238;\"> <!ENTITY iuml \"&#239;\"> <!ENTITY eth \"&#240;\"> <!ENTITY ntilde \"&#241;\"> <!ENTITY ograve \"&#242;\"> <!ENTITY oacute \"&#243;\"> <!ENTITY ocirc \"&#244;\"> <!ENTITY otilde \"&#245;\"> <!ENTITY ouml \"&#246;\"> <!ENTITY divide \"&#247;\"> <!ENTITY oslash \"&#248;\"> <!ENTITY ugrave \"&#249;\"> <!ENTITY uacute \"&#250;\"> <!ENTITY ucirc \"&#251;\"> <!ENTITY uuml \"&#252;\"> <!ENTITY yacute \"&#253;\"> <!ENTITY thorn \"&#254;\"> <!ENTITY yuml \"&#255;\">]>");
	}

	protected void removeHTMLEntities(File htmlFile) {
		replaceByLine(htmlFile, "<!DOCTYPE html.*>", "<!DOCTYPE html>");
	}

	protected void replaceByLine(File file, String match, String replace, boolean byLine, String flags) {
		if (file == null || match == null || replace == null) return;

		ReplaceRegExp replaceRegExp = new ReplaceRegExp();
		replaceRegExp.setFile(file);
		replaceRegExp.setMatch(match);
		replaceRegExp.setReplace(replace);
		replaceRegExp.setByLine(byLine);
		replaceRegExp.setEncoding(sourceEncoding);
		if (flags != null) {
			replaceRegExp.setFlags(flags);
		}
		replaceRegExp.execute();

	}

	protected void replaceByLine(File file, String match, String replace) {
		replaceByLine(file, match, replace, true, null);
	}

	public void printDocument(Document doc, File file) throws TransformerException, IOException, XPathExpressionException {
		fixAutoclosingElements(doc);

		XPathFactory xpathFactory = XPathFactory.newInstance();
		// XPath to find empty text nodes.
		XPathExpression xpathExp = xpathFactory.newXPath().compile("//text()[normalize-space(.) = '']");
		NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);

		// Remove each empty text node from document.
		for (int i = 0; i < emptyTextNodes.getLength(); i++) {
		    Node emptyTextNode = emptyTextNodes.item(i);
		    emptyTextNode.getParentNode().removeChild(emptyTextNode);
		}

		TransformerFactory factory = TransformerFactory.newInstance();

		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "application/xml+xhtml");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD XHTML 1.0 Transitional//EN");
		transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd");
		transformer.setOutputProperty(OutputKeys.ENCODING, sourceEncoding);
		
		PrintWriter writer = new PrintWriter(file, sourceEncoding);
		try {
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
		} finally {
			writer.close();
		}

		removeHTMLEntities(file);
	}

	private void fixAutoclosingElements(Document domDocument) {
		try {
			Match document = JOOX.$(domDocument);
			Match lists = document.xpath(autoclosingElements).filter(new Filter() {
				@Override
				public boolean filter(Context context) {
					return (context.element().getFirstChild() == null); // with no child element (so autoclosing)
				}
			});
			for (org.w3c.dom.Element element : lists) {
				if ("script".equals(element.getNodeName())) {
					element.appendChild(document.document().createTextNode("// preserve auto-closing elements"));
				} else {
					element.appendChild(document.document().createComment("preserve auto-closing elements"));
				}
			}
		} catch (Exception e) {
			// nothing to do
		}
	}

	protected void replaceProperty(File htmlFile, String propertyName, String modelPropertyName, boolean propertyInRootProject, boolean onlyInOriginalModel, boolean lookInSettings) {
		replaceByLine(htmlFile,
					  "\\$\\{" + propertyName + "\\}",
					  getPropertyValue(modelPropertyName, propertyInRootProject, onlyInOriginalModel, lookInSettings),
					  true,
					  "gs");
	}

	protected HtmlCanvas createCommandLines(String commandLine, String templateStart, String templateEnd, List<String> arguments, List<String> results, boolean addBreak) throws IOException {
		HtmlCanvas result = new HtmlCanvas();

		result.write(templateStart, false);
		result.write(commandLine, false);

		String spacing = " ";
		String breakAdd;
		if (addBreak) {
			breakAdd = "<br />";
		} else {
			breakAdd = "";
		}
		boolean first = true;
		for (String argument : arguments) {
			if (first) {
				result.write("<span>" + spacing + argument + "</span>" + breakAdd + "\n", false);
				spacing = StringUtils.repeat(" ", commandLine.length() + 1);
				first = false;
			} else {
				result.write("<span class='command-space'>" + spacing + argument + "</span>" + breakAdd + "\n", false);
			}
		}

		for (String r : results) {
			result.write("<span class='result'>" + r + "</span>" + breakAdd + "\n", false);
		}

		result.write(templateEnd, false);

		return result;
	}

	protected String getFullCommandLine(String commandLine, List<String> arguments) {
		String result = commandLine;
		for (String argument : arguments) {
			result += " " + argument;
		}

		return result;
	}

	protected HtmlCanvas createArchetypesCommandLines(Dependency dependency, String archetypeAdditionalArguments, boolean addBreak) throws IOException {
		HtmlCanvas archetypesCommandLine = new HtmlCanvas();

		String commandLine = "mvn archetype:generate";

		List<String> arguments = new ArrayList<>();
		arguments.add("-DarchetypeGroupId=" + dependency.getGroupId());
		arguments.add("-DarchetypeArtifactId=" + dependency.getArtifactId());
		arguments.add("-DarchetypeVersion=" + dependency.getVersion());
		if (archetypeAdditionalArguments != null) {
			String[] lines = archetypeAdditionalArguments.split(" ");
			if (lines.length > 0) {
				for (int i = 0; i < lines.length; i++) {
					arguments.add(lines[i].trim());
				}
			} else {
				arguments.add(archetypeAdditionalArguments);
			}
		}

		this.project.getProperties().put("data-clipboard-text", getFullCommandLine(commandLine, arguments));
		this.project.getProperties().put("command-title", "&#160;");

		String templateStart = replaceProperties(replaceProperties("${commandLineStart}"));
		String templateEnd = replaceProperties(replaceProperties("${commandLineEnd}"));

		archetypesCommandLine = createCommandLines(commandLine, templateStart, templateEnd, arguments, new ArrayList<String>(), addBreak);

		addPropertyInSessionRequest(dependency.getArtifactId() + "_ArchetypeCommandLine", archetypesCommandLine.toHtml());

		HtmlCanvas result = new HtmlCanvas();

		result.
			p().write("Command line for this archetype:")._p().
			write(archetypesCommandLine.toHtml(), false);

		return result;
	}

	protected void addPropertyInSessionRequest(String propertyName, String content) {
		getLog().debug("Adding property to session request : " + propertyName);
		getLog().debug(content);

		List<Profile> profiles = this.session.getRequest().getProfiles();

		Profile profile = null;

		for (Profile p : profiles) {
			if ("_tmp-site".equals(p.getId())) {
				profile = p;
			}
		}

		if (profile == null) {
			Activation activation = new Activation();
			activation.setActiveByDefault(true);

			profile = new Profile();
			profile.setActivation(activation);
			profile.setId("_tmp-site");

			profiles.add(profile);
			this.session.getRequest().setProfiles(profiles);
			this.session.getRequest().addActiveProfile(profile.getId());
		}

		Properties properties = profile.getProperties();
		if (properties == null) {
			properties = new Properties();
		}
		properties.put(propertyName, content);

		profile.setProperties(properties);

		if (staticSiteProperties == null) {
			staticSiteProperties = new ArrayList<String>();
		}
		if (staticLookInSettingsProperties == null) {
			staticLookInSettingsProperties = new ArrayList<String>();
		}

		staticSiteProperties.add(propertyName);
		staticLookInSettingsProperties.add(propertyName);
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (outputDirectory == null || !outputDirectory.exists() || !outputDirectory.isDirectory()) {
			return;
		}
	}
}
