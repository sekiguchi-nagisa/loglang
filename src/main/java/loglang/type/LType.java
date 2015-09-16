package loglang.type;

import loglang.misc.Utils;

import java.util.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/27.
 */
public class LType implements Comparable<LType> {
    /**
     * mangled name( [_a-zA-Z][_a-zA-Z0-9]* )
     */
    protected final String uniqueName;

    /**
     * must be fully qualify class name.
     */
    protected final String internalName;

    /**
     * may be null, (if Any or Void type)
     */
    protected final LType superType;

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
    }

    public LType(Class<?> clazz, LType superType) {
        Objects.requireNonNull(clazz);
        this.uniqueName = Mangler.mangleBasicType(clazz.getSimpleName());
        this.internalName = clazz.getCanonicalName();
        this.superType = superType;
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

    public final String getSimpleName() {
        return Mangler.demangle(this.uniqueName);
    }

    public LType getSuperType() {
        return superType;
    }

    /**
     * check type inheritence
     * @param type
     * not null
     * @return
     */
    public boolean isSameOrBaseOf(LType type) {
        return this.equals(Objects.requireNonNull(type))
                || (type.superType != null && this.isSameOrBaseOf(type.superType));
    }

    public final boolean isVoid() {
        return this.equals(voidType);
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

    @Override
    public String toString() {
        return this.getSimpleName();
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


    public static class ArrayType extends LType {
        private final LType elementType;

        ArrayType(String uniqueName, LType elementType) {
            super(uniqueName, List.class.getCanonicalName(), anyType);
            this.elementType = Objects.requireNonNull(elementType);
        }

        public LType getElementType() {
            return elementType;
        }

        public boolean isSameOrBaseOf(LType type) {
            if(type instanceof ArrayType) {
                return this.elementType.isSameOrBaseOf(((ArrayType) type).elementType);
            }
            return this.superType != null && this.superType.isSameOrBaseOf(type);
        }
    }

    public static class OptionalType extends LType {
        private final LType elementType;

        OptionalType(String uniqueName, LType type) {
            super(uniqueName, List.class.getCanonicalName(), anyType);
            this.elementType = Objects.requireNonNull(type);
        }

        public LType getElementType() {
            return elementType;
        }

        public boolean isSameOrBaseOf(LType type) {
            if(type instanceof OptionalType) {
                return this.elementType.isSameOrBaseOf(((OptionalType) type).elementType);
            }
            return type.superType != null && this.isSameOrBaseOf(type.superType);
        }
    }

    public static class TupleType extends LType {
        private final List<LType> elementTypes;

        TupleType(String uniqueName, LType[] types) {
            super(uniqueName, List.class.getCanonicalName(), anyType);
            this.elementTypes = Collections.unmodifiableList(Arrays.asList(types));
        }

        public List<LType> getElementTypes() {
            return elementTypes;
        }

        public boolean isSameOrBaseOf(LType type) {
            if(type instanceof TupleType) {
                return this.elementTypes.equals(((TupleType) type).elementTypes);
            }
            return type.superType != null && this.isSameOrBaseOf(type.superType);
        }
    }

    public static class UnionType extends LType {
        /**
         * sorted
         */
        private final List<LType> elementTypes;

        /**
         *
         * @param uniqueName
         * @param types
         * not contains duplicated type.
         * not contains union type.
         * must be sorted
         */
        UnionType(String uniqueName, LType[] types) {
            super(uniqueName, List.class.getCanonicalName(), anyType);  //FIXME: internal name
            this.elementTypes = Collections.unmodifiableList(Arrays.asList(types));
        }

        public List<LType> getElementTypes() {
            return elementTypes;
        }

        public boolean isSameOrBaseOf(LType type) {
            if(type instanceof UnionType) {
                boolean match = true;
                for(LType t : ((UnionType) type).elementTypes) {
                    if(!this.isSameOrBaseOf(t)) {
                        match = false;
                        break;
                    }
                }
                if(match) {
                    return true;
                }
            } else {
                for(LType e : this.elementTypes) {
                    if(e.isSameOrBaseOf(type)) {
                        return true;
                    }
                }
            }
            return type.superType != null && this.isSameOrBaseOf(type.superType);
        }
    }

    public static abstract class AbstractStructureType extends LType {
        protected final Map<String, MemberRef.FieldRef> fieldMap = new HashMap<>();

        AbstractStructureType(String uniqueName, String internalName, LType superType) {
            super(uniqueName, internalName, superType);
        }

        abstract MemberRef.FieldRef addField(String fieldName, LType fieldType);

        @Override
        public MemberRef.FieldRef lookupField(String fieldName) {
            return this.fieldMap.get(fieldName);
        }
    }

    public static class StructureType extends AbstractStructureType {
        StructureType(String uniqueName) {
            super(uniqueName, List.class.getCanonicalName(), anyType);  //FIXME: internal name
        }

        @Override
        MemberRef.FieldRef addField(String fieldName, LType fieldType) {
            Objects.requireNonNull(fieldName);
            Objects.requireNonNull(fieldType);

            if(this.fieldMap.containsKey(fieldName)) {
                return null;
            }

            final int fieldIndex = this.fieldMap.size();
            int attribute = MemberRef.READ_ONLY;
            String simpleName = this.getSimpleName();
            if(simpleName.startsWith(TypeEnv.getAnonymousCaseTypeNamePrefix())) {
                attribute = Utils.setFlag(attribute, MemberRef.CASE_TREE_FIELD);
            } else if(simpleName.startsWith(TypeEnv.getAnonymousPrefixTypeName())) {
                attribute = Utils.setFlag(attribute, MemberRef.PREFIX_TREE_FIELD);
            } else {
                attribute = Utils.setFlag(attribute, MemberRef.TREE_FIELD);
            }

            MemberRef.FieldRef ref = new MemberRef.FieldRef(fieldIndex, fieldType, fieldName, this, attribute);
            this.fieldMap.put(fieldName, ref);
            return ref;
        }
    }

    public static class CaseContextType extends AbstractStructureType {
        CaseContextType(String uniqueName, String internalName) {
            super(uniqueName, internalName, anyType);
        }

        @Override
        MemberRef.FieldRef addField(String fieldName, LType fieldType) {
            Objects.requireNonNull(fieldName);
            Objects.requireNonNull(fieldType);

            if(this.fieldMap.containsKey(fieldName)) {
                return null;
            }

            final int fieldIndex = -1;
            final int attribute = MemberRef.INSTANCE_FIELD;

            MemberRef.FieldRef ref = new MemberRef.FieldRef(fieldIndex, fieldType, fieldName, this, attribute);
            this.fieldMap.put(fieldName, ref);
            return ref;
        }
    }
}
