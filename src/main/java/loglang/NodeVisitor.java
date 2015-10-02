package loglang;

import static loglang.Node.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/18.
 */
public interface NodeVisitor<T, P> {
    default T visit(Node node, P param) {
        return node.accept(this, param);
    }

    default T visit(Node node) {
        return this.visit(node, null);
    }

    T visitIntLiteralNode(IntLiteralNode node, P param);
    T visitFloatLiteralNode(FloatLiteralNode node, P param);
    T visitBoolLiteralNode(BoolLiteralNode node, P param);
    T visitStringLiteralNode(StringLiteralNode node, P param);
    T visitCaseNode(CaseNode node, P param);
    T visitBlockNode(BlockNode node, P param);
    T visitStateDeclNode(StateDeclNode node, P param);
    T visitVarDeclNode(VarDeclNode node, P param);
    T visitVarNode(VarNode node, P param);
    T visitPrintNode(PrintNode node, P param);
    T visitAssertNode(AssertNode node, P param);
    T visitPopNode(PopNode node, P param);
    T visitRootNode(RootNode node, P param);
}
