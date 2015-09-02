package loglang.type;

import loglang.misc.Utils;

import java.util.*;

public class Mangler {
    private Mangler(){}

    public static String mangleBasicType(String name) {
        return new StringBuilder()
                .append("B")
                .append(name.length())
                .append(name)
                .toString();
    }

    /**
     *
     * @param types
     * contains at least 2 elements
     * @return
     * @throws IllegalArgumentException
     */
    public static String mangleTupleType(LType[] types) throws IllegalArgumentException {
        if(types.length < 2) {
            throw new IllegalArgumentException("need at least 2 elements");
        }

        for(LType t : types) {
            if(t.equals(LType.voidType)) {
                throw new IllegalArgumentException("element type must not be void type");
            }
        }

        return mangleComposedType("Tuple", types);
    }

    /**
     *
     * @param types
     * not contains duplicated element
     * contains at least 2 elements.
     * @return
     * @throws IllegalArgumentException
     */
    public static String mangleUnionType(LType[] types) throws IllegalArgumentException {
        return mangleUnionTypeUnsafe(flattenUnionElements(types));
    }

    static String mangleUnionTypeUnsafe(LType[] types) {
        return mangleComposedType("Union", types);
    }

    /**
     *
     * @param types
     * @return
     * @throws IllegalArgumentException
     */
    static LType[] flattenUnionElements(LType[] types) throws IllegalArgumentException{
        Set<LType> typeSet = new TreeSet<>();

        LinkedList<LType> queue = new LinkedList<>();
        queue.addAll(Arrays.asList(types));
        while(!queue.isEmpty()) {
            LType type = queue.removeFirst();
            if(type instanceof LType.UnionType) {
                queue.addAll(((LType.UnionType) type).getElementTypes());
            } else if(type.equals(LType.voidType)) {
                throw new IllegalArgumentException("element type must not be void type");
            } else {
                typeSet.add(type);
            }
        }

        final int size = typeSet.size();
        if(size < 2) {
            throw new IllegalArgumentException("need at least 2 element");
        }

        types = new LType[size];
        int index = 0;
        for(LType t : typeSet) {
            types[index++] = t;
        }
        return types;
    }

    /**
     *
     * @param type
     * @return
     * @throws IllegalArgumentException
     */
    public static String mangleArrayType(LType type) throws IllegalArgumentException {
        if(type.equals(LType.voidType)) {
            throw new IllegalArgumentException("element type must not be void type");
        }
        return mangleComposedType("Array", new LType[]{type});
    }

    /**
     *
     * @param type
     * @return
     * @throws IllegalArgumentException
     */
    public static String mangleOptionalType(LType type) throws IllegalArgumentException{
        if(type.equals(LType.voidType)) {
            throw new IllegalArgumentException("element type must not be void type");
        }
        return mangleComposedType("Optional", new LType[]{type});
    }

    private static String mangleComposedType(String baseName, LType[] types) {
        StringBuilder sBuilder = new StringBuilder()
                .append("C")
                .append(baseName.length())
                .append(baseName);

        sBuilder.append("E").append(types.length);
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
            index++;
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
