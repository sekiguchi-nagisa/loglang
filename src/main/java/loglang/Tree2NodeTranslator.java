package loglang;

import static loglang.Node.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class Tree2NodeTranslator extends TreeTranslator<Node> {{
    this.add("Match", t -> {  // entry point
        RootNode node = new RootNode(range(t));
        t.stream().map(this::translate).map(CaseNode.class::cast).forEach(node::addCaseNode);
        return node;
    });


    this.add("CaseStatement", t -> this.translate(t.get(1))); // ignore case pattern

    this.add("CaseBlock", t -> {
        assert t.size() == 2;

        CaseNode caseNode = new CaseNode(range(t), null); //FIXME: label
        // state decl
        t.get(0).stream().map(this::translate).map(StateDeclNode.class::cast).forEach(caseNode::addStateDeclNode);

        // block
        t.get(1).stream().map(this::translate).forEach(caseNode::addStmtNode);

        return caseNode;
    });

    this.add("CondOr", t -> CondOpNode.newOrNode(range(t), this.translate(t.get(0)), this.translate(t.get(1))));

    this.add("CondAnd", t -> CondOpNode.newAndNode(range(t), this.translate(t.get(0)), this.translate(t.get(1))));

    this.add("Integer", t -> new IntLiteralNode(range(t), Integer.parseInt(t.toText())));

    this.add("Float", t -> new FloatLiteralNode(range(t), Float.parseFloat(t.toText())));

    this.add("True", t -> new BoolLiteralNode(range(t), true));
    this.add("False", t -> new BoolLiteralNode(range(t), false));

    this.add("String", t -> {
        String src = t.toText();
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

    this.add("Variable", t -> new VarNode(range(t), t.toText()));

    this.add("Print", t -> new PrintNode(range(t), this.translate(t.get(0))));

    this.add("Assert", t -> new AssertNode(range(t), this.translate(t.get(0)),
            t.size() == 2 ? this.translate(t.get(1)) : null));

    this.add("Block", t -> {
        BlockNode blockNode = new BlockNode(range(t));
        t.stream().map(this::translate).forEach(blockNode::addNode);
        return blockNode;
    });

    this.add("While", t -> new WhileNode(range(t), this.translate(t.get(0)), (BlockNode) this.translate(t.get(1))));

    this.add("If", t ->
            new IfNode(range(t), this.translate(t.get(0)), this.translate(t.get(1)),
                    t.size() == 2 ? null : this.translate(t.get(2)))
    );

    this.add("State", t -> new StateDeclNode(range(t), t.get(0).toText(), this.translate(t.get(1))));

    this.add("VarDecl", t -> new VarDeclNode(range(t), t.get(0).toText(), this.translate(t.get(1))));
}}