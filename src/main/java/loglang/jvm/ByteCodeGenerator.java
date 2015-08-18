package loglang.jvm;

import static loglang.Node.*;

import loglang.CaseContext;
import loglang.Node;
import loglang.NodeVisitor;
import loglang.misc.Pair;
import loglang.misc.Utils;
import org.objectweb.asm.Label;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by skgchxngsxyz-opensuse on 15/08/19.
 */
public class ByteCodeGenerator implements NodeVisitor<Void, GeneratorAdapter>, Opcodes {
    private final String packageName = "loglang.generated" + Utils.getRandomNum();

    private int classNameSuffixCount = -1;

    /***
     * left is break label.
     * right is continue label.
     */
    private final Deque<Pair<Label, Label>> loopLabels = new ArrayDeque<>();

    /**
     * code generation entry point
     * @param caseNode
     * @return
     * pair of generated class name (fully qualified name) and byte code.
     */
    public Pair<String, byte[]> generateCode(CaseNode caseNode) {
        String className = packageName + "Case" + ++this.classNameSuffixCount;
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(V1_8, ACC_PUBLIC, className, null,
                Type.getInternalName(Object.class), new String[]{Type.getInternalName(CaseContext.class)});
        cw.visitSource(null, null);

        return Pair.of(className, null);
    }

    @Override
    public Void visitIntLiteralNode(IntLiteralNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitFloatLiteralNode(FloatLiteralNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitBoolLiteralNode(BoolLiteralNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitStringLiteralNode(StringLiteralNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitCaseNode(CaseNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitBlockNode(BlockNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitStateDeclNode(StateDeclNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitPopNode(PopNode node, GeneratorAdapter param) {
        return null;
    }

    @Override
    public Void visitRootNode(RootNode node, GeneratorAdapter param) {
        return null;
    }
}
