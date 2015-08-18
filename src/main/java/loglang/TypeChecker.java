package loglang;

import java.lang.reflect.Type;

import static loglang.Node.*;
import static loglang.SemanticException.*;
import static loglang.Types.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public class TypeChecker implements NodeVisitor<Node, Void> {
    public Node checkType(Type requiredType, Node targetNode, Type unacceptableType) {  //FIXME: coercion
        if(targetNode.getType() == null) {
            this.visit(targetNode);
        }

        Type type = targetNode.getType();
        if(type == null) {
            semanticError("broken node");
        }

        if(requiredType == null) {
            if(unacceptableType != null && isSameOrBaseOf(unacceptableType, type)) {
                semanticError("unacceptable type: " + type);
            }
            return targetNode;
        }

        if(isSameOrBaseOf(requiredType, type)) {
            return targetNode;
        }

        semanticError("require: " + requiredType + ", but is: " + type);
        return null;
    }

    public Node checkType(Node targetNode) {
        return this.checkType(null, targetNode, void.class);
    }

    public Node checkType(Type requiredType, Node targetNode) {
        return this.checkType(requiredType, targetNode, null);
    }

    public Node checkTypeAsStatement(Node targetNode) {
        Node node = this.checkType(null, targetNode, null);
        return node.hasReturnValue() ? new PopNode(node) : node;
    }


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

    @Override
    public Node visitCaseNode(CaseNode node, Void param) {  //FIXME:
        this.checkTypeAsStatement(node.getBlockNode());
        node.setType(void.class);
        return node;
    }

    @Override
    public Node visitBlockNode(BlockNode node, Void param) {    //FIXME:
        node.setType(void.class);
        return node;
    }

    @Override
    public Node visitStateDeclNode(StateDeclNode node, Void param) {    //FIXME: register state name to symbol table
        node.setInitValueNode(this.checkType(node.getInitValueNode()));
        node.setType(void.class);
        return node;
    }

    @Override
    public Node visitPopNode(PopNode node, Void paramNode) {
        throw new UnsupportedOperationException();    // not call it.
    }
}
