package loglang.type;

import loglang.misc.Utils;

import java.util.Collections;
import java.util.List;

public class Mangler {
    private Mangler(){}

    public static String mangleBasicType(String name) {
        return new StringBuilder()
                .append("B")
                .append(name.length())
                .append(name)
                .toString();
    }

    public static String mangleTupleType(List<LType> types) {
        return mangleComposedType("Tuple", types);
    }

    public static String mangleUnionType(List<LType> types) {
        return mangleComposedType("Union", types);
    }

    public static String mangleArrayType(LType type) {
        return mangleComposedType("Array", Collections.singletonList(type));
    }

    public static String mangleOptionalType(LType type) {
        return mangleComposedType("Optional", Collections.singletonList(type));
    }

    private static String mangleComposedType(String baseName, List<LType> types) {
        StringBuilder sBuilder = new StringBuilder()
                .append("C")
                .append(baseName.length())
                .append(baseName);

        sBuilder.append("E").append(types.size());
        for(LType type : types) {
            sBuilder.append(type.getUniqueName());
        }
        return sBuilder.toString();
    }

    public static String demangle(String mangledName) {
        StringBuilder sBuilder = new StringBuilder();
        demangle(mangledName, 0, sBuilder);
        return sBuilder.toString();
    }

    private static int demangle(final String mangledName, int index, final StringBuilder sb) {
        assert index < mangledName.length();
        char ch = mangledName.charAt(index);
        switch(ch) {
        case 'B': { // basic type
            int len = mangledName.charAt(++index) - '0';
            index++;
            sb.append(mangledName, index, index += len);
            return index;
        }
        case 'C': { // composed type
            int len = mangledName.charAt(++index) - '0';
            index++;
            sb.append(mangledName, index, index += len);
            sb.append("<");

            len = mangledName.charAt(++index) - '0';    //skip 'E'
            for(int i = 0; i < len; i++) {
                if(i > 0) {
                    sb.append(",");
                }
                index = demangle(mangledName, index, sb);
            }
            sb.append(">");
            return index;
        }
        default:
            Utils.fatal("illegal char: " + ch + " at " + mangledName);
        }
        return 0;
    }
}
