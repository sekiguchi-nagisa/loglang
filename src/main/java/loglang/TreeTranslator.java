package loglang;

import static loglang.Node.*;

import loglang.misc.Utils;
import nez.ast.CommonTree;

import java.util.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */

@FunctionalInterface
interface TagHandler {
    Node invoke(CommonTree tree) throws Exception;
}

public abstract class TreeTranslator {
    protected final Map<String, TagHandler> handlerMap = new HashMap<>();

    protected void add(String tagName, TagHandler handler) {
        if(Objects.nonNull(this.handlerMap.put(tagName, handler))) {
            Utils.fatal("duplicated tag: " + tagName);
        }
    }

    public Node translate(CommonTree tree) {
        String key = tree.getTag().getName();
        TagHandler handler = this.handlerMap.get(key);
        if(Objects.isNull(handler)) {
            Utils.fatal("undefined handler: " + key);
        }
        try {
            return handler.invoke(tree);
        } catch(Exception e) {
            Utils.propagate(e);
        }
        return null;
    }

    public static TreeTranslator create() {
        return new TreeTranslatorImpl();
    }
}

class TreeTranslatorImpl extends TreeTranslator {{
    this.add("Match", (t) -> {  // entry point
        RootNode node = new RootNode();
        for(CommonTree child : t) {
            node.addCaseNode((CaseNode) this.translate(child));
        }
        return node;
    });


    this.add("CaseStatement", (t) -> this.translate(t.get(1)));

    this.add("CaseBlock", (t) -> {
        assert t.size() == 2;

        CaseNode caseNode = new CaseNode(null); //FIXME: label
        // state decl
        for(CommonTree child : t.get(0)) {
            caseNode.addStateDeclNode((StateDeclNode) this.translate(child));
        }

        // block
        for(CommonTree child : t.get(1)) {
            caseNode.addStmtNode(this.translate(child));
        }

        return caseNode;
    });



    this.add("Integer", (t) -> {
        int value = Integer.parseInt(t.getText());
        return new IntLiteralNode(value);
    });

    this.add("Float", (t) -> {
        float value = Float.parseFloat(t.getText());
        return new FloatLiteralNode(value);
    });

    this.add("True", (t) -> new BoolLiteralNode(true));
    this.add("False", (t) -> new BoolLiteralNode(false));

    this.add("String", (t) -> {
        String src = t.getText();
        boolean dquote = src.charAt(0) == '"';
        StringBuilder sb = new StringBuilder();
        final int size = src.length() - 1;
        for(int i = 1; i < size; i++) {
            char ch = src.charAt(i);
            if(ch == '\\' && ++i < size) {
                char next = src.charAt(i);
                switch(next) {
                case 'n':
                    ch = '\n';
                    break;
                case 'r':
                    ch = '\r';
                    break;
                case 't':
                    ch = '\t';
                    break;
                case '\'':
                    if(!dquote) {
                        ch = '\'';
                        break;
                    }
                    i--;
                    break;
                case '"':
                    if(dquote) {
                        ch = '"';
                        break;
                    }
                    i--;
                    break;
                case '\\':
                    ch = '\\';
                    break;
                default:
                    i--;
                    break;
                }
            }
            sb.append(ch);
        }

        return new StringLiteralNode(sb.toString());
    });

    this.add("Variable", (t) -> new VarNode(t.getText()));

    this.add("State", (t) -> {
        assert t.size() == 2;
        String name = t.get(0).getText();
        Node initValueNode = this.translate(t.get(1));
        return new StateDeclNode(name, initValueNode);
    });

    this.add("VarDecl", (t) -> {
        assert t.size() == 2;
        String name = t.get(0).getText();
        Node initValueNode = this.translate(t.get(1));
        return new VarDeclNode(name, initValueNode);
    });
}}


