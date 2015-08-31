package loglang.type;

import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/27.
 */
public class LType implements Comparable<LType> {
    /**
     * mangled name( [_a-zA-Z][_a-zA-Z0-9]* )
     */
    private final String uniqueName;

    /**
     * must be fully qualify class name.
     */
    private final String internalName;

    /**
     * may be null, (if Any or Void type)
     */
    private final LType superType;

    /**
     * if type is void, 0.
     * if type is long or double type, 2.
     * otherwise 1.
     */
    private final int stackConsumption;

    /**
     *
     * @param uniqueName
     * not null
     * @param internalName
     * not null
     * @param superType
     * may be null
     */
    public LType(String uniqueName, String internalName, LType superType) {
        this.uniqueName = Objects.requireNonNull(uniqueName);
        this.internalName = Objects.requireNonNull(internalName);
        this.superType = superType;
        this.stackConsumption = stackConsumption(this.asType());
    }

    public LType(Class<?> clazz, LType superType) {
        Objects.requireNonNull(clazz);
        this.uniqueName = Mangler.mangleBasicType(clazz.getSimpleName());
        this.internalName = clazz.getCanonicalName();
        this.superType = superType;
        this.stackConsumption = stackConsumption(this.asType());
    }

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

    /**
     *
     * @return
     * mangled name
     */
    public final String getUniqueName() {
        return uniqueName;
    }

    public final String getInternalName() {
        return internalName;
    }

    public LType getSuperType() {
        return superType;
    }

    public final Type asType() {
        switch(this.internalName) {
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
            return Type.getType( "L" + this.internalName + ";");
        }
    }

    public final int stackConsumption() {
        return this.stackConsumption;
    }

    /**
     * check type inheritence
     * @param type
     * not null
     * @return
     */
    public boolean isSameOrBaseOf(LType type) {
        return this.equals(Objects.requireNonNull(type))
                || (this.superType != null && this.superType.isSameOrBaseOf(type));
    }

    /**
     *
     * @param fieldName
     * not null
     * @return
     * if not found, return null
     */
    public MemberRef.FieldRef lookupField(String fieldName) {
        return null;
    }

    /**
     *
     * @param methodName
     * not null
     * @return
     * if not found, return null
     */
    public MemberRef.MethodRef lookupMethod(String methodName) {
        return null;
    }

    /**
     *
     * @return
     * if not found, return null
     */
    public MemberRef.ConstructorRef lookupConstructor() {
        return (MemberRef.ConstructorRef) this.lookupMethod("<init>");
    }

    @Override
    public String toString() {
        return this.getUniqueName();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LType
                && this.uniqueName.equals(((LType) obj).uniqueName);
    }

    @Override
    public int hashCode() {
        return this.uniqueName.hashCode();
    }

    @Override
    public int compareTo(LType o) {
        return this.uniqueName.compareTo(o.uniqueName);
    }

    public static LType voidType = new LType(void.class, null);
    public static LType anyType =
            new LType(Mangler.mangleBasicType("Any"), Object.class.getCanonicalName(), null);
}
