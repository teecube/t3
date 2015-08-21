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

import org.kohsuke.MetaInfServices;

import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.ListBuffer;

import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import t3.plugin.annotations.Parameter;

@MetaInfServices(JavacAnnotationHandler.class)
@HandlerPriority(1024)
public class HandleParameterRuntime extends JavacAnnotationHandler<Parameter> {

	@Override
	public void handle(final AnnotationValues<Parameter> annotation, final JCAnnotation ast, final JavacNode annotationNode) {
		JavacTreeMaker treeMaker = annotationNode.getTreeMaker();
		JavacNode owner = annotationNode.up(); // the field where the @Annotation applies
		JCVariableDecl fieldDecl = (JCVariableDecl) owner.get();
		JCAnnotation parameterRuntime = null;
		for (JCAnnotation a : fieldDecl.mods.annotations) {
			if (a.annotationType.type.tsym.getQualifiedName().toString().equals(Parameter.class.getCanonicalName())) {
				parameterRuntime = a;
			}
		}

		if (parameterRuntime != null) {
			JCExpression mavenParameterAnnotationType = chainDots(owner, "org", "apache", "maven", "plugins", "annotations", "Parameter");

			ListBuffer<JCExpression> mavenParameterFields = new ListBuffer<JCExpression>();
			for (JCExpression arg : parameterRuntime.args) {
				JCAssign argument = (JCAssign) arg;
				JCIdent ident = (JCIdent) argument.lhs;

				mavenParameterFields.add(treeMaker.Assign(treeMaker.Ident(annotationNode.toName(ident.name.toString())), argument.rhs));
			}

			// @Parameter
			JCAnnotation mavenParameterAnnotation = treeMaker.Annotation(mavenParameterAnnotationType, mavenParameterFields.toList());

			fieldDecl.mods.annotations = fieldDecl.mods.annotations.append(mavenParameterAnnotation);
			
			owner.getAst().setChanged();
		}
	}

}