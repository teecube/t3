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
package t3.plugin.annotations.replacement;

import java.util.ArrayList;
import java.util.Set;
import javax.lang.model.element.Modifier;

import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.source.tree.Tree.Kind;

class Documentifier {

    static Documentifier instance;

//    final DocCommentGenerator docGen;
    final ScannerFactory scanners;

    public Documentifier(Context context) {
        // docGen = new DocCommentGenerator();
        scanners = ScannerFactory.instance(context);
    }

    private DocCommentTable curDocComments;

    public void documentify(JCCompilationUnit topLevel, boolean isFxStyle) {
        if (!topLevel.docComments.getClass().equals(PoorDocCommentTable.class)) {
            LazyDocCommentTable lazyDocComments = (LazyDocCommentTable) topLevel.docComments;
            curDocComments = new PoorDocCommentTable(lazyDocComments);
        } else {
            curDocComments = (PoorDocCommentTable) topLevel.docComments;
            return;
        }
        JCClassDecl base = (JCClassDecl)topLevel.getTypeDecls().get(0);
        documentifyBase(base, true, isFxStyle);
        topLevel.docComments = curDocComments;
    }

    private void documentifyBase(JCClassDecl base, boolean isTopLevel, boolean isFxStyle) {
        // add doc comment to class itself
//        Comment comm = comment(docGen.getBaseComment(base, isTopLevel));
//        curDocComments.putComment(base, comm);

        // add doc comments to members
        for (JCTree member : base.getMembers()) {
            switch (member.getTag()) {
                case VARDEF:
                    documentifyField(base, (JCVariableDecl)member, isFxStyle);
                    break;
                case METHODDEF:
                    documentifyMethod(base, (JCMethodDecl)member, isFxStyle);
                    break;
                case CLASSDEF:
                    documentifyBase((JCClassDecl)member, false, isFxStyle);
                    break;
            }
        }
    }

    private void documentifyField(JCClassDecl base, JCVariableDecl field, boolean isFxStyle) {
        Kind baseKind = base.getKind();
        Set<Modifier> fieldMods = field.getModifiers().getFlags();
        String doc = (baseKind == Kind.ENUM
                && fieldMods.contains(Modifier.PUBLIC)
                && fieldMods.contains(Modifier.STATIC)
                && fieldMods.contains(Modifier.FINAL)) ?
//                docGen.getConstComment() :
//                docGen.getFieldComment(base, field, isFxStyle);
                "SOME COMMENT" : "SOME COMMENT";
        Comment comm = comment(doc);
        curDocComments.putComment(field, comm);
    }

    private void documentifyMethod(JCClassDecl base, JCMethodDecl method, boolean isFxStyle) {
        //Comment comm = comment(docGen.getMethodComment(base, method, isFxStyle));
        Comment comm = comment("SOME OTHER COMMENT");
        curDocComments.putComment(method, comm);
    }

    private Comment comment(String docString) {
        StringBuilder docComment = new StringBuilder()
                .append("/** ")
                .append(docString)
                .append(" */");
        Scanner scanner = scanners.newScanner(docComment, true);
        scanner.nextToken();
        Token token = scanner.token();
        return token.comment(CommentStyle.JAVADOC);
    }

    // provide package comment data ONLY
    /*
    public DocCommentGenerator getDocGenerator() {
        return docGen;
    }
    */
}