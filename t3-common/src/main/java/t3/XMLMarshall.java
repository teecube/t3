/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://t3soft.org) and others.
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

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
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

	public XMLMarshall(File xmlFile) throws JAXBException {
		this(xmlFile, Object.class);
	}

	@SuppressWarnings("unchecked")
	private Class<Factory> getActualFactoryClass() {
		Class<Factory> cls = (Class<Factory>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1]; // WARN
		return cls;
	}

	public XMLMarshall(File xmlFile, Class<?>... classesToBeBound) throws JAXBException {
		this.xmlFile = xmlFile;
		this.classesToBound = new ArrayList<Class<?>>();
		this.classesToBound.addAll(Arrays.asList(classesToBeBound));
		this.classesToBound.add(getActualFactoryClass());
		load();
	}

	/**
	 * <p>
	 * This will unmarshall the object from an XML file.
	 * </p>
	 *
	 * @throws JAXBException
	 */
	@SuppressWarnings("unchecked")
	private void load() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(this.classesToBound.toArray(new Class<?>[0]));

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		Object o =  jaxbUnmarshaller.unmarshal(xmlFile);

		JAXBElement<Type> j = (JAXBElement<Type>) o;
		this.object = j.getValue();
	}

	/**
	 * <p>
	 * This will marshall the object back to the XML file.
	 * </p>
	 *
	 * @throws JAXBException
	 */
	public void save() throws JAXBException {
		Marshaller m = jaxbContext.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.marshal(object, xmlFile);
	}

	public static <ParentType, ChildType extends ParentType> List<ChildType> convertList(List<JAXBElement<? extends ParentType>> listToConvert, ChildType childElement) {
		if (childElement == null || listToConvert == null) return null;

		Class<?> actualChildType = childElement.getClass();

		List<ChildType> result = new ArrayList<ChildType>();

		for (JAXBElement<? extends ParentType> j : listToConvert) {
			@SuppressWarnings("unchecked") // safe because "? extends ParentType" and "ChildType extends ParentType"
			Class<ChildType> childType = (Class<ChildType>) j.getDeclaredType();
			if (childType.isAssignableFrom(childElement.getClass())) {
				ChildType objectAsChildType = (ChildType) actualChildType.cast(j.getValue());
				result.add(objectAsChildType);
			}
		}
		return result;
	}
}
