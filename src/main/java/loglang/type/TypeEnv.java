package loglang.type;

import java.util.*;

import static loglang.SemanticException.*;

import loglang.SemanticException;
import loglang.misc.Pair;
import loglang.type.LType.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/27.
 */
public class TypeEnv {
    /**
     * key is mangled name
     */
    private final Map<String, LType> typeMap = new HashMap<>();

    private LType intType;
    private LType floatType;
    private LType boolType;
    private LType stringType;



    public TypeEnv() {
        this.typeMap.put(LType.voidType.getUniqueName(), LType.voidType);
        this.typeMap.put(LType.anyType.getUniqueName(), LType.anyType);

        // add basic type
        this.intType = this.newBasicType("int", int.class);
        this.floatType = this.newBasicType("float", float.class);
        this.boolType = this.newBasicType("bool", boolean.class);
        this.stringType = this.newBasicType("string", String.class);
    }


    /**
     *
     * @param simpleName
     * @param clazz
     * @return
     * @throws SemanticException
     */
    private LType newBasicType(String simpleName, Class<?> clazz) throws SemanticException{
        String mangledName = Mangler.mangleBasicType(simpleName);
        LType type = new LType(mangledName, clazz.getCanonicalName(), LType.anyType);
        return this.registerType(mangledName, type);
    }

    /**
     *
     * @param mangledName
     * @param type
     * @return
     * @throws SemanticException
     */
    private LType registerType(String mangledName, LType type) throws SemanticException{
        if(Objects.nonNull(this.typeMap.put(mangledName, type))) {
            semanticError("already defined type: " + type.getSimpleName());
        }
        return type;
    }

    /**
     *
     * @param mangledName
     * must be mangled name
     * @return
     * if not found, return null
     */
    public LType getTypeByMangledName(String mangledName) {
        return this.typeMap.get(mangledName);
    }

    /**
     *
     * @param simpleName
     * @return
     * @throws SemanticException
     */
    public LType getBasicType(String simpleName) throws SemanticException{
        LType type = this.getTypeByMangledName(Mangler.mangleBasicType(simpleName));
        if(type == null) {
            semanticError("undefined type: " + simpleName);
        }
        return type;
    }

    public LType getAnyType() {
        return LType.anyType;
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


    public boolean isPrimaryType(String simpleName) {
        String mangledName = Mangler.mangleBasicType(simpleName);
        LType type = this.getTypeByMangledName(mangledName);
        return type != null && this.isPrimaryType(type);
    }

    /**
     *
     * @param type
     * @return
     * if type is int, float or string, return true.
     */
    public boolean isPrimaryType(LType type) {
        return this.intType.equals(type) || this.floatType.equals(type) || this.stringType.equals(type);
    }


    /**
     *
     * @param elementType
     * must not be void
     * @return
     * @throws SemanticException
     * if broken.
     */
    public ArrayType getArrayType(LType elementType) throws SemanticException {
        String mangledName;
        try {
            mangledName = Mangler.mangleArrayType(elementType);
        } catch(IllegalArgumentException e) {
            throw new SemanticException(e.getMessage());
        }

        LType type = this.getTypeByMangledName(mangledName);
        if(type == null) {  // create array type
            type = this.registerType(mangledName, new ArrayType(mangledName, elementType));
        }
        return (ArrayType) type;
    }

    /**
     *
     * @param elementType
     * must not be void
     * @return
     * @throws SemanticException
     */
    public OptionalType getOptionalType(LType elementType) throws SemanticException {
        String mangledName;
        try {
            mangledName = Mangler.mangleOptionalType(elementType);
        } catch(IllegalArgumentException e) {
            throw new SemanticException(e.getMessage());
        }
        LType type = this.getTypeByMangledName(mangledName);
        if(type == null) {
            type = this.registerType(mangledName, new OptionalType(mangledName, elementType));
        }
        return (OptionalType)type;
    }

    public TupleType getTupleType(LType[] elementTypes) throws SemanticException {
        String mangledName;
        try {
            mangledName = Mangler.mangleTupleType(elementTypes);
        } catch(IllegalArgumentException e) {
            throw new SemanticException(e.getMessage());
        }
        LType type = this.getTypeByMangledName(mangledName);
        if(type == null) {
            type = this.registerType(mangledName, new TupleType(mangledName, elementTypes));
        }
        return (TupleType) type;
    }

    public UnionType getUnionType(LType[] elementTypes) throws SemanticException {
        try {
            elementTypes = Mangler.flattenUnionElements(elementTypes);
        } catch(IllegalArgumentException e) {
            throw new SemanticException(e.getMessage());
        }
        String mangledName = Mangler.mangleUnionTypeUnsafe(elementTypes);
        LType type = this.getTypeByMangledName(mangledName);
        if(type == null) {
            type = this.registerType(mangledName, new UnionType(mangledName, elementTypes));
        }
        return (UnionType) type;
    }

    /**
     *
     * @param name
     * must be simple name
     * @return
     * @throws SemanticException
     * if already defined.
     */
    public StructureType newStructureType(String name) throws SemanticException {
        String mangledName = Mangler.mangleBasicType(Objects.requireNonNull(name));
        return (StructureType) this.registerType(mangledName, new StructureType(mangledName));
    }


    /**
     *
     * @param type
     * @param fieldName
     * @param fieldType
     * @throws SemanticException
     * if already defined.
     */
    public void defineField(StructureType type, String fieldName, LType fieldType) throws SemanticException{
        if(!type.addField(fieldName, fieldType)) {
            semanticError("already undefined field: " + fieldName + ", in " + type.getSimpleName());
        }
    }
}
