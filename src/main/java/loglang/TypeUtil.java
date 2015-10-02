package loglang;

import nez.peg.tpeg.type.LType;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.List;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/09/17.
 */
public class TypeUtil {
    private TypeUtil() {}

    public static int stackConsumption(Type type) {
        switch(type.getSort()) {
        case Type.VOID:
            return 0;
        case Type.LONG:
        case Type.DOUBLE:
            return 2;
        default:
            return 1;
        }
    }

    public static int stackConsumption(LType type) {
        return stackConsumption(asType(type));
    }

    public static Type asType(LType type) {
        switch(type.getInternalName()) {
        case "void":
            return Type.VOID_TYPE;
        case "boolean":
            return Type.BOOLEAN_TYPE;
        case "byte":
            return Type.BYTE_TYPE;
        case "char":
            return Type.CHAR_TYPE;
        case "short":
            return Type.SHORT_TYPE;
        case "int":
            return Type.INT_TYPE;
        case "long":
            return Type.LONG_TYPE;
        case "float":
            return Type.FLOAT_TYPE;
        case "double":
            return Type.DOUBLE_TYPE;
        default:
            return Type.getType( "L" + type.getInternalName() + ";");
        }
    }

    /**
     *
     * @param type
     * @return
     * if represents primitive type, return false
     */
    public static boolean isObjectType(LType type) {
        return isObjectType(asType(type));
    }

    public static boolean isObjectType(Type type) {
        return type.getSort() == Type.OBJECT;
    }

    public static Method toMethodDescriptor(Class<?> returnClass, String methodName, Class<?> ... paramClasses) {
        int size = paramClasses.length;
        Type[] paramTypeDecs = new Type[size];
        for(int i = 0; i < size; i++) {
            paramTypeDecs[i] = Type.getType(paramClasses[i]);
        }
        Type returnTypeDesc = Type.getType(returnClass);
        return new Method(methodName, returnTypeDesc, paramTypeDecs);
    }

    public static Method toConstructorDescriptor(Class<?> ... paramClasses) {
        return toMethodDescriptor(void.class, "<init>", paramClasses);
    }

    public static Method toMethodDescriptor(LType returnType, String methodName, LType ... paramTypes) {
        int size = paramTypes.length;
        Type[] paramTypeDecs = new Type[size];
        for(int i = 0; i < size; i++) {
            paramTypeDecs[i] = asType(paramTypes[i]);
        }
        Type returnTypeDesc = asType(returnType);
        return new Method(methodName, returnTypeDesc, paramTypeDecs);
    }
}
