package loglang.jvm;

import static loglang.Node.*;

import loglang.*;
import loglang.misc.FatalError;
import loglang.misc.Pair;
import loglang.misc.Utils;
import loglang.symbol.MemberRef;
import nez.ast.Tree;
import nez.peg.tpeg.type.LType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Objects;

/**
 * Created by skgchxngsxyz-opensuse on 15/08/19.
 */
public class ByteCodeGenerator implements NodeVisitor<Void, MethodBuilder>, Opcodes {
    /**
     * code generation entry point
     * @param caseNode
     * @return
     * pair of generated class name (fully qualified name) and byte code.
     */
    public Pair<String, byte[]> generateCode(CaseNode caseNode) {
        String className = caseNode.getThisType().getInternalName();
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC, className, null,
                Type.getInternalName(Object.class), new String[]{Type.getInternalName(CaseContext.class)});
        cw.visitSource(null, null);

        // generate state (field)
        for(StateDeclNode child : caseNode.getStateDeclNodes()) {
            cw.visitField(ACC_PUBLIC, child.getName(),
                    TypeUtil.asType(child.getInitValueNode().getType()).getDescriptor(),
                    null, null);
        }

        // generate constructor
        Method methodDesc = TypeUtil.toConstructorDescriptor();
        MethodBuilder mBuilder = new MethodBuilder(className, ACC_PUBLIC, methodDesc, cw);
        mBuilder.loadThis();
        mBuilder.invokeConstructor(Type.getType(Object.class), methodDesc);
        // field initialization
        for(StateDeclNode child : caseNode.getStateDeclNodes()) {
            mBuilder.loadThis();
            this.visit(child.getInitValueNode(), mBuilder);
            mBuilder.putField(Type.getType("L" + className + ";"), child.getName(),
                    TypeUtil.asType(child.getInitValueNode().getType()));
        }
        mBuilder.returnValue();
        mBuilder.endMethod();

        // generate method
        methodDesc = TypeUtil.toMethodDescriptor(void.class, "invoke", Tree.class, Tree.class);
        mBuilder = new MethodBuilder(className, ACC_PUBLIC, methodDesc, cw);

        this.visit(caseNode.getBlockNode(), mBuilder);

        mBuilder.returnValue();
        mBuilder.endMethod();


        // finalize
        cw.visitEnd();
        return Pair.of(className, cw.toByteArray());
    }

    @Override
    public Void visit(Node node) {
        throw new UnsupportedOperationException("not call it");
    }

    @Override
    public Void visit(Node node, MethodBuilder param) {
        return node.accept(this, Objects.requireNonNull(param));
    }

    @Override
    public Void visitIntLiteralNode(IntLiteralNode node, MethodBuilder param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitFloatLiteralNode(FloatLiteralNode node, MethodBuilder param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitBoolLiteralNode(BoolLiteralNode node, MethodBuilder param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitStringLiteralNode(StringLiteralNode node, MethodBuilder param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitCaseNode(CaseNode node, MethodBuilder param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visitBlockNode(BlockNode node, MethodBuilder param) {
        for(Node child : node.getNodes()) {
            this.visit(child, param);
        }
        return null;
    }

    @Override
    public Void visitStateDeclNode(StateDeclNode node, MethodBuilder param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visitVarDeclNode(VarDeclNode node, MethodBuilder param) {
        this.visit(node.getInitValueNode(), param);
        Type desc = TypeUtil.asType(node.getEntry().getFieldType());
        param.visitVarInsn(desc.getOpcode(ISTORE), node.getEntry().getIndex());
        return null;
    }

    @Override
    public Void visitVarNode(VarNode node, MethodBuilder param) {
        MemberRef.FieldRef entry = node.getEntry();
        Type desc = TypeUtil.asType(entry.getFieldType());
        if(Utils.hasFlag(entry.getAttribute(), MemberRef.LOCAL_VAR)) {
            param.visitVarInsn(desc.getOpcode(ILOAD), entry.getIndex());
        } else { // access own field
            param.loadThis();
            param.getField(TypeUtil.asType(entry.getOwnerType()), node.getVarName(), desc);
        }
        return null;
    }

    @Override
    public Void visitPrintNode(PrintNode node, MethodBuilder param) {
        LType exprType = node.getExprNode().getType();

        this.visit(node.getExprNode(), param);

        // boxing
        param.box(TypeUtil.asType(exprType));

        param.invokeStatic(Type.getType(loglang.lang.Helper.class), TypeUtil.toMethodDescriptor(void.class, "print", Object.class));

        return null;
    }

    @Override
    public Void visitAssertNode(AssertNode node, MethodBuilder param) {
        if(Config.noAssert) {
            return null;    // not generate assert statement
        }

        this.visit(node.getCondNode(), param);

        node.getMsgNode().ifPresent(t -> this.visit(t, param));
        if(!node.getMsgNode().isPresent()) {
            param.pushNull();
        }

        param.invokeStatic(Type.getType(loglang.lang.Helper.class),
                TypeUtil.toMethodDescriptor(void.class, "checkAssertion", boolean.class, String.class));

        return null;
    }

    @Override
    public Void visitWhileNode(WhileNode node, MethodBuilder param) {
        // create label
        Label continueLabel = param.newLabel();
        Label breakLabel = param.newLabel();
        param.getLoopLabels().push(Pair.of(breakLabel, continueLabel));

        param.mark(continueLabel);
        this.visit(node.getCondNode(), param);
        param.ifZCmp(GeneratorAdapter.EQ, breakLabel);
        this.visit(node.getBlockNode(), param);
        param.goTo(continueLabel);
        param.mark(breakLabel);

        // remove label
        param.getLoopLabels().pop();

        return null;
    }

    @Override
    public Void visitIfNode(IfNode node, MethodBuilder param) {
        Label elseLabel = param.newLabel();
        Label mergeLabel = param.newLabel();

        // cond
        this.visit(node.getCondNode(), param);
        param.ifZCmp(GeneratorAdapter.EQ, elseLabel);

        // then
        this.visit(node.getThenNode(), param);
        param.goTo(mergeLabel);

        // else
        param.mark(elseLabel);
        node.getElseNode().ifPresent(e -> this.visit(e, param));

        // merge
        param.mark(mergeLabel);

        return null;
    }

    @Override
    public Void visitPopNode(PopNode node, MethodBuilder param) {
        this.visit(node.getExprNode(), param);

        // pop stack top
        switch(TypeUtil.stackConsumption(node.getExprNode().getType())) {
        case 1:
            param.pop();
            break;
        case 2:
            param.pop2();
            break;
        default:
            throw new FatalError("broken popNode type: " + node.getExprNode().getType());
        }
        return null;
    }

    @Override
    public Void visitRootNode(RootNode node, MethodBuilder param) {
        throw new UnsupportedOperationException();
    }
}
