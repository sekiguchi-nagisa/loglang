package loglang.jvm;

import loglang.misc.Pair;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by skgchxngsxyz-osx on 15/10/02.
 */
public class MethodBuilder extends GeneratorAdapter {
    /**
     * fully qualified class name(contains '/')
     */
    private final String ownerClassName;

    /***
     * left is break label.
     * right is continue label.
     */
    private final Deque<Pair<Label, Label>> loopLabels = new ArrayDeque<>();

    public MethodBuilder(String ownerClassName,
                         int access, Method method, ClassVisitor cv) {
        super(Opcodes.ASM5,
                cv.visitMethod(access, method.getName(), method.getDescriptor(), null, null),
                access, method.getName(), method.getDescriptor());
        this.ownerClassName = ownerClassName;
    }

    public String getOwnerClassName() {
        return ownerClassName;
    }

    public Deque<Pair<Label, Label>> getLoopLabels() {
        return loopLabels;
    }

    @Override
    public void box(Type type) {
        final String methodName = "valueOf";
        Class<?> boxedClass = null;

        switch(type.getSort()) {
        case Type.BOOLEAN:
            boxedClass = Boolean.class;
            break;
        case Type.BYTE:
            boxedClass = Byte.class;
            break;
        case Type.CHAR:
            boxedClass = Character.class;
            break;
        case Type.SHORT:
            boxedClass = Short.class;
            break;
        case Type.INT:
            boxedClass = Integer.class;
            break;
        case Type.LONG:
            boxedClass = Long.class;
            break;
        case Type.FLOAT:
            boxedClass = Float.class;
            break;
        case Type.DOUBLE:
            boxedClass = Double.class;
            break;
        default:
            break;  // do nothing
        }

        if(boxedClass != null) {
            this.invokeStatic(
                    Type.getType(boxedClass),
                    new Method(methodName, Type.getType(boxedClass), new Type[]{type}));
        }
    }

    public void pushNull() {
        this.visitInsn(Opcodes.ACONST_NULL);
    }
}
