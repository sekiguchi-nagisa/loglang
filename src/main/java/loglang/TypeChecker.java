package loglang;

import static loglang.Node.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class TypeChecker implements NodeVisitor<Node, Void> {
    @Override
    public Node visitIntLiteralNode(IntLiteralNode node, Void param) {
        node.setType(int.class);
        return node;
    }

    @Override
    public Node visitFloatLiteralNode(FloatLiteralNode node, Void param) {
        node.setType(float.class);
        return node;
    }

    @Override
    public Node visitBoolLiteralNode(BoolLiteralNode node, Void param) {
        node.setType(boolean.class);
        return node;
    }

    @Override
    public Node visitStringLiteralNode(StringLiteralNode node, Void param) {
        node.setType(String.class);
        return node;
    }
}
