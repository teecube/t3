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

import com.sun.tools.javac.parser.JavadocTokenizer;
import com.sun.tools.javac.parser.LazyDocCommentTable;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class PoorDocCommentTable implements DocCommentTable {

    HashMap<JCTree, Tokens.Comment> table;

    public PoorDocCommentTable() {
        table = new HashMap<>();
    }

    public PoorDocCommentTable(LazyDocCommentTable lazyDocCommentTable) {
        table = new HashMap<>();

        Field tableField = null;
        try {
            tableField = lazyDocCommentTable.getClass().getDeclaredField("table");
            tableField.setAccessible(true);
            Map<JCTree, ?> originalTable = (Map<JCTree, ?>) tableField.get(lazyDocCommentTable);
            for (JCTree key : originalTable.keySet()) {
                Object lazyComment = originalTable.get(key);
                Field commentField = lazyComment.getClass().getDeclaredField("comment");
                commentField.setAccessible(true);
                Object comment = commentField.get(lazyComment);
                table.put(key, (Tokens.Comment) comment);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // TODO
        }
    }

    public boolean hasComment(JCTree tree) {
        return table.containsKey(tree);
    }

    public Tokens.Comment getComment(JCTree tree) {
        return table.get(tree);
    }

    public String getCommentText(JCTree tree) {
        Tokens.Comment c = getComment(tree);
        return (c == null) ? null : c.getText();
    }

    public DCTree.DCDocComment getCommentTree(JCTree tree) {
        return null; // no need for generator purposes, Pretty does not call it
    }

    public void putComment(JCTree tree, Tokens.Comment c) {
        table.put(tree, c);
    }
}