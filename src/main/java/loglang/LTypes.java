package loglang;

import loglang.type.LType;
import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/09/17.
 */
public class LTypes {
    private LTypes() {}

    public static int stackConsumption(Type type) {
        Objects.requireNonNull(type);
        if(type.equals(Type.VOID_TYPE)) {
            return 0;
        }
        if(type.equals(Type.LONG_TYPE) || type.equals(Type.DOUBLE_TYPE)) {
            return 2;
        }
        return 1;
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
}
