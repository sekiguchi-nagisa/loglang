package loglang.type;

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

    public static class FieldRef extends MemberRef {
        private final LType fieldType;

        public FieldRef(LType fieldType, String fieldName, LType ownerType) {
            super(fieldName, ownerType);
            this.fieldType = Objects.requireNonNull(fieldType);
        }

        public LType getFieldType() {
            return fieldType;
        }

        @Override
        public String toString() {
            return new StringBuilder()
                    .append("(")
                    .append(this.fieldType)
                    .append(" ")
                    .append(this.getInternalName())
                    .append(" in ")
                    .append(this.getOwnerType())
                    .append(")")
                    .toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FieldRef
                    && this.internalName.equals(((FieldRef) obj).internalName)
                    && this.ownerType.equals(((FieldRef) obj).ownerType)
                    && this.fieldType.equals(((FieldRef) obj).fieldType);
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
                paramTypeDescs[i] = this.paramTypes.get(i).asType();
            }
            return new Method(this.internalName, this.returnType.asType(), paramTypeDescs);
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
