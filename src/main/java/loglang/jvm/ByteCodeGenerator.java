package loglang.jvm;

import static loglang.Node.*;

import loglang.*;
import loglang.lang.Helper;
import loglang.misc.Pair;
import loglang.misc.Utils;
import loglang.TypeUtil;
import loglang.symbol.MemberRef;
import nez.ast.Tree;
import nez.peg.tpeg.type.LType;
import org.objectweb.asm.Label;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-opensuse on 15/08/19.
 */
public class ByteCodeGenerator implements NodeVisitor<Void, GeneratorAdapter>, Opcodes {
    private final String packageName;

    private int classNameSuffixCount = -1;

    /***
     * left is break label.
     * right is continue label.
     */
    private final Deque<Pair<Label, Label>> loopLabels = new ArrayDeque<>();

    public ByteCodeGenerator(String packageName) {
        this.packageName = packageName;
    }

    /**
     * code generation entry point
     * @param caseNode
     * @return
     * pair of generated class name (fully qualified name) and byte code.
     */
    public Pair<String, byte[]> generateCode(CaseNode caseNode) {
        String className = packageName + "/Case" + ++this.classNameSuffixCount;
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
        GeneratorAdapter adapter = new GeneratorAdapter(ACC_PUBLIC, methodDesc, null, null, cw);
        adapter.loadThis();
        adapter.invokeConstructor(Type.getType(Object.class), methodDesc);
        // field initialization
        for(StateDeclNode child : caseNode.getStateDeclNodes()) {
            this.visit(child.getInitValueNode(), adapter);
            adapter.putField(Type.getType("L" + className + ";"), child.getName(),
                    TypeUtil.asType(child.getInitValueNode().getType()));
        }
        adapter.returnValue();
        adapter.endMethod();

        // generate method
        methodDesc = TypeUtil.toMethodDescriptor(void.class, "invoke", Tree.class, Tree.class);
        adapter = new GeneratorAdapter(ACC_PUBLIC, methodDesc, null, null, cw);

        this.visit(caseNode.getBlockNode(), adapter);

        adapter.returnValue();
        adapter.endMethod();


        // finalize
        cw.visitEnd();
        return Pair.of(className, cw.toByteArray());
    }

    @Override
    public Void visit(Node node) {
        throw new UnsupportedOperationException("not call it");
    }

    @Override
    public Void visit(Node node, GeneratorAdapter param) {
        return node.accept(this, Objects.requireNonNull(param));
    }

    @Override
    public Void visitIntLiteralNode(IntLiteralNode node, GeneratorAdapter param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitFloatLiteralNode(FloatLiteralNode node, GeneratorAdapter param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitBoolLiteralNode(BoolLiteralNode node, GeneratorAdapter param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitStringLiteralNode(StringLiteralNode node, GeneratorAdapter param) {
        param.push(node.getValue());
        return null;
    }

    @Override
    public Void visitCaseNode(CaseNode node, GeneratorAdapter param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visitBlockNode(BlockNode node, GeneratorAdapter param) {
        for(Node child : node.getNodes()) {
            this.visit(child, param);
        }
        return null;
    }

    @Override
    public Void visitStateDeclNode(StateDeclNode node, GeneratorAdapter param) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visitVarDeclNode(VarDeclNode node, GeneratorAdapter param) {
        this.visit(node.getInitValueNode(), param);
        Type desc = TypeUtil.asType(node.getEntry().getFieldType());
        param.visitVarInsn(desc.getOpcode(ISTORE), node.getEntry().getIndex());
        return null;
    }

    @Override
    public Void visitVarNode(VarNode node, GeneratorAdapter param) {
        Type desc = TypeUtil.asType(node.getEntry().getFieldType());
        if(Utils.hasFlag(node.getEntry().getAttribute(), MemberRef.LOCAL_VAR)) {
            param.visitVarInsn(desc.getOpcode(ILOAD), node.getEntry().getIndex());
        } else {
//            param.get //FIXME:
        }
        return null;
    }

    @Override
    public Void visitPrintNode(PrintNode node, GeneratorAdapter param) {
        LType exprType = node.getExprNode().getType();

        this.visit(node.getExprNode(), param);

        // boxing
        param.box(TypeUtil.asType(exprType));

        param.invokeStatic(Type.getType(Helper.class), TypeUtil.toMethodDescriptor(void.class, "print", Object.class));

        return null;
    }

    @Override
    public Void visitPopNode(PopNode node, GeneratorAdapter param) {
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
            Utils.fatal("broken popNode type: " + node.getExprNode().getType());
        }
        return null;
    }

    @Override
    public Void visitRootNode(RootNode node, GeneratorAdapter param) {
        throw new UnsupportedOperationException();
    }
}
