package loglang.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by skgchxngsxyz-osx on 15/08/27.
 */
public class TypeEnv {
    private final Map<String, LType> typeMap = new HashMap<>();

    private LType intType;
    private LType floatType;
    private LType boolType;
    private LType stringType;



    public TypeEnv() {
    }

    /**
     *
     * @param typeName
     * @return
     * if not found, return null
     */
    public LType getType(String typeName) {
        return this.typeMap.get(typeName);
    }

    public LType getVoidType() {
        return LType.voidType;
    }

    public LType getIntType() {
        return intType;
    }

    public LType getFloatType() {
        return floatType;
    }

    public LType getBoolType() {
        return boolType;
    }

    public LType getStringType() {
        return stringType;
    }


}
