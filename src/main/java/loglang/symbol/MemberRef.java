package loglang.symbol;

import loglang.TypeUtil;
import nez.peg.tpeg.type.LType;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/27.
 */
public abstract class MemberRef {
    /**
     * if this is constructor, name is <init>
     */
    protected final String internalName;

    /**
     * the type which the method belong to.
     */
    protected final LType ownerType;

    public MemberRef(String internalName, LType ownerType) {
        this.internalName = Objects.requireNonNull(internalName);
        this.ownerType = Objects.requireNonNull(ownerType);
    }

    public final String getInternalName() {
        return internalName;
    }

    public final LType getOwnerType() {
        return ownerType;
    }


    /**
     * for symbol entry attribute
     */
    public final static int READ_ONLY         = 1;
    public final static int LOCAL_VAR         = 1 << 1;
    public final static int INSTANCE_FIELD    = 1 << 2;
    public final static int PREFIX_TREE_FIELD = 1 << 3;
    public final static int CASE_TREE_FIELD   = 1 << 4;
    public final static int TREE_FIELD        = 1 << 5;

    /**
     * for instance field and local variable
     */
    public static class FieldRef extends MemberRef {
        private final LType fieldType;

        /**
         * if represents instance field(state), index is -1
         */
        private final int index;

        private final int attribute;

        /**
         *
         * @param index
         * @param fieldType
         * @param fieldName
         * @param ownerType
         * @param attribute
         */
        public FieldRef(int index, LType fieldType, String fieldName, LType ownerType, int attribute) {
            super(fieldName, ownerType);
            this.fieldType = Objects.requireNonNull(fieldType);
            this.index = index;
            this.attribute = attribute;
        }

        public LType getFieldType() {
            return fieldType;
        }

        public int getIndex() {
            return index;
        }

        public int getAttribute() {
            return attribute;
        }

        @Override
        public String toString() {
            return "(" + this.fieldType + " " + this.getInternalName() + " in " + this.getOwnerType() + ")";
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FieldRef
                    && this.internalName.equals(((FieldRef) obj).internalName)
                    && this.ownerType.equals(((FieldRef) obj).ownerType)
                    && this.fieldType.equals(((FieldRef) obj).fieldType)
                    && this.index == ((FieldRef) obj).index
                    && this.attribute == ((FieldRef) obj).attribute;
        }
    }

    public enum InvocationKind {
        INVOKE_STATIC,
        INVOKE_SPECIAL,
        INVOKE_VIRTUAL,
        INVOKE_INTERFACE,
        INVOKE_DYNAMIC,
    }

    public static class MethodRef extends MemberRef {
        protected final LType returnType;

        /**
         * not contains receiver type.
         */
        protected List<LType> paramTypes = new ArrayList<>();

        protected InvocationKind kind;

        /**
         * if exist same name method, not null
         */
        protected MethodRef next = null;

        public MethodRef(LType returnType, String methodName, List<LType> paramTypes, LType ownerType) {
            super(methodName, ownerType);
            this.returnType = Objects.requireNonNull(ownerType);
            this.kind = InvocationKind.INVOKE_VIRTUAL;
            for(LType paramType : paramTypes) {
                this.paramTypes.add(Objects.requireNonNull(paramType));
            }
            // read only
            this.paramTypes = Collections.unmodifiableList(this.paramTypes);
        }

        public LType getReturnType() {
            return returnType;
        }

        /**
         *
         * @return
         * read only list.
         */
        public List<LType> getParamTypes() {
            return paramTypes;
        }

        public InvocationKind getKind() {
            return kind;
        }

        public Method asMethod() {
            final int paramSize = this.paramTypes.size();
            Type[] paramTypeDescs = new Type[paramSize];
            for(int i = 0; i < paramSize; i++) {
                paramTypeDescs[i] = TypeUtil.asType(this.paramTypes.get(i));
            }
            return new Method(this.internalName, TypeUtil.asType(this.returnType), paramTypeDescs);
        }

        /**
         *
         * @return
         * may be null
         */
        public MethodRef getNext() {
            return next;
        }

        void setNext(MethodRef ref) {
            this.next = Objects.requireNonNull(ref);
        }

        @Override
        public String toString() {
            StringBuilder sBuilder = new StringBuilder()
                    .append("(")
                    .append(this.returnType)
                    .append(" ")
                    .append(this.internalName)
                    .append("(");

            final int size = this.paramTypes.size();
            for(int i = 0; i < size; i++) {
                if(i > 0) {
                    sBuilder.append(", ");
                }
                sBuilder.append(this.paramTypes.get(i));
            }
            sBuilder.append(") in ").append(this.ownerType).append(")");
            return sBuilder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof MethodRef
                    && this.internalName.equals(((MethodRef) obj).internalName)
                    && this.ownerType.equals(((MethodRef) obj).ownerType)
                    && this.returnType.equals(((MethodRef) obj).returnType)
                    && this.paramTypes.equals(((MethodRef) obj).paramTypes);
        }
    }

    public static class StaticMethodRef extends MethodRef {
        public StaticMethodRef(LType returnType, String methodName, List<LType> paramTypes, LType ownerType) {
            super(returnType, methodName, paramTypes, ownerType);
            this.kind = InvocationKind.INVOKE_STATIC;
        }
    }

    public static class ConstructorRef extends MethodRef {
        public ConstructorRef(LType ownerType, List<LType> paramTypes) {
            super(LType.voidType, "<init>", paramTypes, ownerType);
            this.kind = InvocationKind.INVOKE_SPECIAL;
        }
    }
}
