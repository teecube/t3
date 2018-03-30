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
package t3.xml;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class RootElementNamespaceFilter extends XMLFilterImpl {

	private String rootElementLocalName;
	private List<NamespaceDeclaration> namespaceDeclarationsToRemove;

    public static class NamespaceDeclaration {
        private String prefix;
        private String uri;

        /**
         *
         * @param prefix, prefix in the form xmlns:prefix
         * @param uri, the namespace URI
         */
        public NamespaceDeclaration(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof NamespaceDeclaration))
				return false;
			if (obj == this)
				return true;

			NamespaceDeclaration rhs = (NamespaceDeclaration) obj;
			return new EqualsBuilder().
				append(prefix, rhs.prefix).
				append(uri, rhs.uri).
				isEquals();
		}

    }

	/**
	 *
	 * @param rootElementLocalName, the document root element local name
	 * @param prefixes, list of prefix in the form "xmlns:prefix"
	 */
	public RootElementNamespaceFilter(String rootElementLocalName, List<NamespaceDeclaration> namespaceDeclarationsToRemove) {
		super();

		if (rootElementLocalName == null) {
			this.rootElementLocalName = "";
		}
		this.rootElementLocalName = rootElementLocalName;

		if (namespaceDeclarationsToRemove == null) {
			namespaceDeclarationsToRemove = new ArrayList<NamespaceDeclaration>();
		}
		this.namespaceDeclarationsToRemove = namespaceDeclarationsToRemove;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if (localName.equals(rootElementLocalName)) {
			AttributesImpl result = new AttributesImpl();

			if (atts.getLength() > 0) {
				for (int i = 0; i < atts.getLength(); i++) {
					String _uri = atts.getURI(i);
					String _localName = atts.getLocalName(i);
					String _qName = atts.getQName(i);
					String _type = atts.getType(i);
					String _value = atts.getValue(i);

					if (!namespaceDeclarationsToRemove.contains(new NamespaceDeclaration(_qName, _value))) {
						result.addAttribute(_uri, _localName, _qName, _type, _value);
					}
				}
			}

			super.startElement(uri, localName, qName, result);
		} else {
			super.startElement(uri, localName, qName, atts);
		}
	}

}