/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://www.t3soft.org) and others.
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
package t3.tic.maven.bw6;

import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class BW6Utils {

	public static Boolean isBW6(Dependency dependency) {
		if (dependency == null) return false;
		return isBW6(dependency.getType());
	}

	public static  Boolean isBW6(Artifact artifact) {
		if (artifact == null) return false;
		return isBW6(artifact.getType());
	}

	private static Boolean isBW6(String type) {
		if (type == null) {
			return false;
		}

		switch (type) {
		case "bw6-app-module":
		case "bw6-shared-module":
			// TODO: osgi
			return true;
		default:
			return false;
		}
	}

	public static File updateTIBCOXMLVersion(File tibcoXML, Map<String, String> modulesVersions) throws Exception {
		Document tibcoXMLDocument = loadTIBCOXML(tibcoXML);
		tibcoXMLDocument = updateTIBCOXMLVersion(tibcoXMLDocument, modulesVersions);
		File file = saveTIBCOXML(tibcoXML, tibcoXMLDocument);

		return file;
	}

	private static Document updateTIBCOXMLVersion(Document tibcoXMLDocument, Map<String, String> modulesVersions) throws Exception {
		if (tibcoXMLDocument == null || modulesVersions == null) {
			return null;
		}

		NodeList modules = tibcoXMLDocument.getElementsByTagNameNS(BW6Constants.ns_PackagingModel , "module");

		for (int i=0; i < modules.getLength(); i++) {
			Element module = (Element) modules.item(i);

			NodeList childList = module.getElementsByTagNameNS(BW6Constants.ns_PackagingModel, "symbolicName");
			String moduleName = childList.item(0).getTextContent();

			NodeList technologyVersionList = module.getElementsByTagNameNS(BW6Constants.ns_PackagingModel, "technologyVersion");
			Node technologyVersion = technologyVersionList.item(0);

			technologyVersion.setTextContent(modulesVersions.get( moduleName));
		}

		return tibcoXMLDocument;
	}

	public static Document loadTIBCOXML(File file) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		docFactory.setNamespaceAware(true);

		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(file );

		return doc;
	}

	public static File saveTIBCOXML(File tibcoXML, Document tibcoXMLDocument) throws Exception {
		tibcoXMLDocument.getDocumentElement().normalize();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        DOMSource source = new DOMSource(tibcoXMLDocument);

        StreamResult result = new StreamResult(tibcoXML);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);

        return tibcoXML;
	}

}
