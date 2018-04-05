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

import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.xml.sax.SAXException;
import t3.xml.RootElementNamespaceFilter.NamespaceDeclaration;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class XMLMarshall<Type, Factory> {

    private Type object;

    public Type getObject() {
        return object;
    }

    private JAXBContext jaxbContext;
    private File xmlFile;
    private List<Class<?>> classesToBound;

    protected String rootElementLocalName;
    protected List<NamespaceDeclaration> namespaceDeclarationsToRemove;

    private Pattern patternElement = Pattern.compile("(\\w+)(\\[([\\w- \\*\\.\\/?]*)\\])?");
    private HashMap<String, Object> map = new HashMap<String, Object>();
    private InputStream xsdStream;

    public XMLMarshall(File xmlFile) throws JAXBException, SAXException {
        this(xmlFile, Object.class);
    }

    public XMLMarshall(File xmlFile, InputStream xsdStream) throws JAXBException, SAXException {
        this(xmlFile, xsdStream, Object.class);
    }

    @SuppressWarnings("unchecked")
    private Class<Factory> getActualFactoryClass() {
        Class<Factory> cls = (Class<Factory>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1]; // WARN
        return cls;
    }

    public XMLMarshall(File xmlFile, InputStream xsdStream, Class<Object> class1) throws JAXBException, SAXException {
        this.xmlFile = xmlFile;
        this.xsdStream = xsdStream;

        this.classesToBound = new ArrayList<Class<?>>();
        this.classesToBound.add(getActualFactoryClass());
        this.classesToBound.addAll(Arrays.asList(class1));

        load();
    }

    public XMLMarshall(File xmlFile, Class<?>... classesToBeBound) throws JAXBException, SAXException {
        this.xmlFile = xmlFile;

        this.classesToBound = new ArrayList<Class<?>>();
        this.classesToBound.add(getActualFactoryClass());
        this.classesToBound.addAll(Arrays.asList(classesToBeBound));

        load();
    }

    public File getXMLFile() {
        return this.xmlFile;
    }

    /**
     * <p>
     * This will unmarshall the object from an XML file.
     * </p>
     *
     * @throws JAXBException
     * @throws SAXException 
     */
    @SuppressWarnings("unchecked")
    private void load() throws JAXBException, SAXException {
        this.rootElementLocalName = "";
        this.namespaceDeclarationsToRemove = new ArrayList<NamespaceDeclaration>();

        jaxbContext = JAXBContext.newInstance(this.classesToBound.toArray(new Class<?>[0]));

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        
        if (this.xsdStream != null) {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            jaxbUnmarshaller.setSchema(sf.newSchema(new StreamSource(xsdStream)));
        }

        Object o = null;
        if (xmlFile == null || !xmlFile.exists()) {
            StringBuffer xmlStr = new StringBuffer( "<?xml version=\"1.0\"?><root/>");
            o =  jaxbUnmarshaller.unmarshal(new StreamSource(new StringReader(xmlStr.toString())));
        } else {
            o =  jaxbUnmarshaller.unmarshal(xmlFile);
        }

        if (o.getClass().getName().contains("JAXBElement")) {
            this.object = ((JAXBElement<Type>) o).getValue();
        } else {
            this.object = (Type) o;
        }
    }

    /**
     * <p>
     * This will marshall the object back to the XML file.
     * </p>
     *
     * @throws JAXBException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void save() throws JAXBException, UnsupportedEncodingException, FileNotFoundException {
        Marshaller m = jaxbContext.createMarshaller();

        RootElementNamespaceFilter outFilter = new RootElementNamespaceFilter(rootElementLocalName, namespaceDeclarationsToRemove);

        OutputFormat format = new OutputFormat();
        format.setIndent(true);
        format.setIndent("    ");
        format.setNewlines(true);

        XMLWriter writer = new XMLWriter(new FileOutputStream(xmlFile), format);

        outFilter.setContentHandler(writer);

        m.marshal(object, outFilter);
    }

    public void saveWithoutFilter() throws JAXBException, UnsupportedEncodingException, FileNotFoundException {
        Marshaller m = jaxbContext.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(object, xmlFile);
    }


    protected Object createElement(String path, String elementName, String nameAttribute, String value, Object parent) {
        return null; // to be overridden
    }

    /**
     * <p>
     * This method will create JAXB objects from Properties through recursive
     * calls.
     * Each property has a path. The last part of a path (after last /) is the
     * element.
     * </p> 
     */
    public Object getElement(String path, String element, String value, Object parent) {
        if (map.containsKey(path)) {
            return map.get(path);
        } else {
            Matcher matcherElement = patternElement.matcher(element);
            if (matcherElement.matches()) {                    
                String elementName = matcherElement.group(1);
                String nameAttribute = matcherElement.group(3);

                Object result = createElement(path, elementName, nameAttribute, value, parent);
                if (result != null) {
                    map.put(path, result);
                }
                return result;
            }
            return parent;
        }
    }

    public static <ParentType, ChildType extends ParentType> List<ChildType> convertList(List<JAXBElement<? extends ParentType>> listToConvert, ChildType childElement) {
        if (childElement == null || listToConvert == null) return null;

        Class<?> actualChildType = childElement.getClass();

        List<ChildType> result = new ArrayList<ChildType>();

        for (JAXBElement<? extends ParentType> j : listToConvert) {
            @SuppressWarnings("unchecked") // safe because "? extends ParentType" and "ChildType extends ParentType"
            Class<ChildType> childType = (Class<ChildType>) j.getDeclaredType();
            if (childType.isAssignableFrom(childElement.getClass())) {
                @SuppressWarnings("unchecked") // safe because "? extends ParentType" and "ChildType extends ParentType"
                ChildType objectAsChildType = (ChildType) actualChildType.cast(j.getValue());
                result.add(objectAsChildType);
            }
        }
        return result;
    }

}
