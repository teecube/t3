/**
 * (C) Copyright 2016-2018 teecube
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
package t3.plugin.annotations.replacement;

import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import lombok.core.AnnotationValues;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;
import t3.plugin.annotations.Categories;

import java.lang.annotation.Annotation;

import static com.sun.tools.javac.parser.Tokens.Comment.CommentStyle.JAVADOC;
import static lombok.javac.handlers.JavacHandlerUtil.chainDots;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class AnnotationReplacementHelper {

    private static final List<JCExpression> NIL_EXPRESSION = List.<JCExpression>nil();

    public static <T extends Annotation> void addMethodCallInMethodBody(AnnotationValues<T> annotation, JCTree.JCAnnotation ast, JavacNode annotationNode, String methodWhereToAddName, java.util.List<String> methodToAddName, boolean addInFirstPosition) {
        JavacTreeMaker treeMaker = annotationNode.getTreeMaker();
        JavacNode owner = annotationNode.up(); // the field where the @Annotation applies
        switch (owner.get().getClass().getSimpleName()) {
        case "JCClassDecl":
            JCClassDecl classDecl = (JCClassDecl) owner.get();
            for (JCTree e : classDecl.defs) {
                if ("METHOD".equals(e.getKind().toString())) {
                    JCMethodDecl md = (JCMethodDecl) e;
                    if (methodWhereToAddName.equals(md.name.toString())) {
                        JCExpression callIt=JavacHandlerUtil.chainDots(owner, methodToAddName.toArray(new String[0]));
                        JCMethodInvocation factoryMethodCall=treeMaker.Apply(NIL_EXPRESSION, callIt, NIL_EXPRESSION);

                        JCExpressionStatement exec = treeMaker.Exec(factoryMethodCall);
                        if (addInFirstPosition) {
                            md.body.stats = md.body.stats.prepend(exec);
                        } else {
                            md.body.stats = md.body.stats.append(exec);
                        }
                    }
                }
            }
            break;

        default:
            break;
        }

        owner.getAst().setChanged();
    }

    public static <T extends Annotation> void duplicateAnnotationWithAnother(final AnnotationValues<T> annotation, final JCAnnotation ast, final JavacNode annotationNode, String annotationToDuplicateCanonicalName, java.util.List<String> annotationToAdd, java.util.List<String> fieldsToIgnore) {
        JavacNode top = annotationNode.top();

        JCCompilationUnit compilationUnit = (JCCompilationUnit) top.get();
        JCClassDecl classDecl = (JCClassDecl) compilationUnit.getTypeDecls().get(0);
        JavacTreeMaker treeMaker = annotationNode.getTreeMaker();
        JavacNode owner = annotationNode.up(); // the field where the @Annotation applies (field or method)

        List<JCAnnotation> annotations = null;
        switch (owner.get().getClass().getSimpleName()) {
        case "JCClassDecl":
            annotations = classDecl.mods.annotations;
            break;
        case "JCVariableDecl":
            JCVariableDecl fieldDecl = (JCVariableDecl) owner.get();
            annotations = fieldDecl.mods.annotations;
            break;

        default:
            break;
        }

        JCAnnotation annotationToDuplicate = null;
        for (JCAnnotation a : annotations) {
            if (a.annotationType.type.tsym.getQualifiedName().toString().equals(annotationToDuplicateCanonicalName)) {
                annotationToDuplicate = a;
            }
        }

        if (annotationToDuplicate != null) {
            JCExpression mavenParameterAnnotationType = chainDots(owner, annotationToAdd.toArray(new String[0]));

            ListBuffer<JCExpression> mavenParameterFields = new ListBuffer<JCExpression>();
            ListBuffer<JCExpression> javadocs = new ListBuffer<JCExpression>();

            for (JCExpression arg : annotationToDuplicate.args) {
                JCAssign argument = (JCAssign) arg;
                JCIdent ident = (JCIdent) argument.lhs;

                JCFieldAccess value = null;
                if (argument.rhs instanceof JCFieldAccess) {
                    value = (JCFieldAccess) argument.rhs;
                }

                if (fieldsToIgnore.contains(ident.name.toString())) {
                    continue;
                }

                mavenParameterFields.add(treeMaker.Assign(treeMaker.Ident(annotationNode.toName(ident.name.toString())), argument.rhs));
            }

            JCTree.JCAnnotation addedAnnotation = treeMaker.Annotation(mavenParameterAnnotationType, mavenParameterFields.toList());

            switch (owner.get().getClass().getSimpleName()) {
            case "JCClassDecl":
                classDecl.mods.annotations = classDecl.mods.annotations.append(addedAnnotation);;
                break;
            case "JCVariableDecl":
                JCVariableDecl fieldDecl = (JCVariableDecl) owner.get();
                fieldDecl.mods.annotations = fieldDecl.mods.annotations.append(addedAnnotation);
                break;
            default:
                break;
            }

            owner.getAst().setChanged();
            top.getAst().setChanged();
        }
    }

    public static void moveJavadocDescriptionToAnnotation(AnnotationValues<Categories> annotation, JCAnnotation ast, JavacNode annotationNode) {
        JCCompilationUnit compilationUnit = (JCCompilationUnit) annotationNode.top().get();
        JCClassDecl classDecl = (JCClassDecl) compilationUnit.getTypeDecls().get(0);

        replaceDescrition(classDecl, compilationUnit.docComments);
    }

    private static void replaceDescrition(JCClassDecl classDecl, DocCommentTable docComments) {
        for (JCTree def : classDecl.defs) {
            if (def.getClass().getSimpleName().equals("JCClassDecl")) {
                replaceDescrition((JCClassDecl) def, docComments);
            } else if (def.getClass().getSimpleName().equals("JCVariableDecl")) {
                JCVariableDecl variable = (JCVariableDecl) def;
                Tokens.Comment comment = docComments.getComment(def);
                if (comment != null && comment.getStyle().equals(JAVADOC)) {
                    ((JCLiteral) variable.init).value = comment.getText();
                }
            }
        }
    }
}