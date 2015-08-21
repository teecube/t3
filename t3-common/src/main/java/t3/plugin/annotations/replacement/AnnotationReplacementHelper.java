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

import static lombok.javac.handlers.JavacHandlerUtil.chainDots;

import java.lang.annotation.Annotation;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;

import lombok.core.AnnotationValues;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

/**
*
* @author Mathieu Debove &lt;mad@teecube.org&gt;
*
*/
public class AnnotationReplacementHelper {

	public static <T extends Annotation> void handle(final AnnotationValues<T> annotation, final JCAnnotation ast, final JavacNode annotationNode, String annotationCanonicalName, java.util.List<String> replacementClassElements) {
		JavacTreeMaker treeMaker = annotationNode.getTreeMaker();
		JavacNode owner = annotationNode.up(); // the field where the @Annotation applies
		List<JCAnnotation> annotations = null;
		switch (owner.get().getClass().getSimpleName()) {
		case "JCClassDecl":
			JCClassDecl classDecl = (JCClassDecl) owner.get();
			annotations = classDecl.mods.annotations;
			break;
		case "JCVariableDecl":
			JCVariableDecl fieldDecl = (JCVariableDecl) owner.get();
			annotations = fieldDecl.mods.annotations;
			break;

		default:
			break;
		}
		JCAnnotation parameterRuntime = null;
		for (JCAnnotation a : annotations) {
			if (a.annotationType.type.tsym.getQualifiedName().toString().equals(annotationCanonicalName)) {
				parameterRuntime = a;
			}
		}

		if (parameterRuntime != null) {
			JCExpression mavenParameterAnnotationType = chainDots(owner, replacementClassElements.toArray(new String[0]));

			ListBuffer<JCExpression> mavenParameterFields = new ListBuffer<JCExpression>();
			for (JCExpression arg : parameterRuntime.args) {
				JCAssign argument = (JCAssign) arg;
				JCIdent ident = (JCIdent) argument.lhs;

				mavenParameterFields.add(treeMaker.Assign(treeMaker.Ident(annotationNode.toName(ident.name.toString())), argument.rhs));
			}

			JCAnnotation addedAnnotation = treeMaker.Annotation(mavenParameterAnnotationType, mavenParameterFields.toList());

			switch (owner.get().getClass().getSimpleName()) {
			case "JCClassDecl":
				JCClassDecl classDecl = (JCClassDecl) owner.get();
				classDecl.mods.annotations = classDecl.mods.annotations.append(addedAnnotation);;
				break;
			case "JCVariableDecl":
				JCVariableDecl fieldDecl = (JCVariableDecl) owner.get();
				fieldDecl.mods.annotations = fieldDecl.mods.annotations.append(addedAnnotation);;
				break;

			default:
				break;
			}

			owner.getAst().setChanged();
		}
	}

}