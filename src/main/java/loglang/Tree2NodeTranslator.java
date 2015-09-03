package loglang;

import static loglang.Node.*;
import static nez.ast.ASTHelper.*;

import nez.ast.CommonTree;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class Tree2NodeTranslator extends TreeTranslator<Node> {{
    this.add("Match", (t) -> {  // entry point
        RootNode node = new RootNode(range(t));
        for(CommonTree child : t) {
            node.addCaseNode((CaseNode) this.translate(child));
        }
        return node;
    });


    this.add("CaseStatement", (t) -> this.translate(t.get(1)));

    this.add("CaseBlock", (t) -> {
        assert t.size() == 2;

        CaseNode caseNode = new CaseNode(range(t), null); //FIXME: label
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
        return new IntLiteralNode(range(t), value);
    });

    this.add("Float", (t) -> {
        float value = Float.parseFloat(t.getText());
        return new FloatLiteralNode(range(t), value);
    });

    this.add("True", (t) -> new BoolLiteralNode(range(t), true));
    this.add("False", (t) -> new BoolLiteralNode(range(t), false));

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

        return new StringLiteralNode(range(t), sb.toString());
    });

    this.add("Variable", (t) -> new VarNode(range(t), t.getText()));

    this.add("State", (t) -> {
        assert t.size() == 2;
        String name = t.get(0).getText();
        Node initValueNode = this.translate(t.get(1));
        return new StateDeclNode(range(t), name, initValueNode);
    });

    this.add("VarDecl", (t) -> {
        assert t.size() == 2;
        String name = t.get(0).getText();
        Node initValueNode = this.translate(t.get(1));
        return new VarDeclNode(range(t), name, initValueNode);
    });
}}