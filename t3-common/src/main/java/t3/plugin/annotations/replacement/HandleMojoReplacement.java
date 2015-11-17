/**
 * (C) Copyright 2014-2015 teecube
 * (http://teecube.org) and others.
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
package t3.plugin.annotations.replacement;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.MetaInfServices;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;

import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import t3.plugin.annotations.Mojo;

/**
 * <p>
 * This class will add a {@link org.apache.maven.plugins.annotations.Mojo}
 * annotation wherever a {@link t3.plugin.annotations.Mojo} is found.<br />
 * <br />
 * The annotation created is a duplicate.<br />
 * <br />
 * It allows to have both annotations: one is the standard Maven one (used by
 * Maven core and to generate Maven site for instance), the other is a copy with
 * a <b>RUNTIME retention policy</b>.
 * </p>
 *
 * <p>
 * This class will also add a call to "super.initStandalonePOM()" method at the
 * beginning of "execute()" method of the Mojo.
 * </p>
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
@MetaInfServices(JavacAnnotationHandler.class)
@HandlerPriority(1024)
public class HandleMojoReplacement extends JavacAnnotationHandler<Mojo> {

	public String getAnnotationCanonicalName() {
		return Mojo.class.getCanonicalName();
	}

	public List<String> getReplacementClassElements() {
		List<String> result = new ArrayList<String>();
		result.add("org");
		result.add("apache");
		result.add("maven");
		result.add("plugins");
		result.add("annotations");
		result.add("Mojo");
		return result;
	}

	@Override
	public void handle(final AnnotationValues<Mojo> annotation, final JCAnnotation ast, final JavacNode annotationNode) {
		// no inheritance possible

		// add "t3.plugin.annotations.Mojo" where "org.apache.maven.plugins.annotations.Mojo" is found
		AnnotationReplacementHelper.duplicateAnnotationWithAnother(annotation, ast, annotationNode, getAnnotationCanonicalName(), getReplacementClassElements());

		// let's add a call to "super.initStandalonePOM" at the beginning of
		// "execute()" method of the Mojo
		List<String> methodToAddName = new ArrayList<String>();
		methodToAddName.add("super");
		methodToAddName.add("initStandalonePOM");
		AnnotationReplacementHelper.addMethodCallInMethodBody(annotation, ast, annotationNode, "execute", methodToAddName, true);
	}

}