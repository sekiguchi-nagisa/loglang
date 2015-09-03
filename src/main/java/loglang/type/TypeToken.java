package loglang.type;

import loglang.TypeException;

import java.util.*;
import static loglang.TypeException.*;

/**
 * Created by skgchxngsxyz-opensuse on 15/09/02.
 */
public interface TypeToken {
    LType toType(TypeEnv env) throws TypeException;

    class BasicTypeToken implements TypeToken {
        private final String name;

        public BasicTypeToken(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BasicTypeToken && this.name.equals(((BasicTypeToken) obj).name);
        }

        @Override
        public LType toType(TypeEnv env) throws TypeException {
            return Objects.requireNonNull(env).getBasicType(this.name);
        }
    }

    class ComposedTypeToken implements TypeToken {
        private final String name;
        private final List<TypeToken> elementTypeTokens;

        public ComposedTypeToken(String name, List<TypeToken> elementTypeTokens) {
            this.name = Objects.requireNonNull(name);
            this.elementTypeTokens =
                    Collections.unmodifiableList(
                            Arrays.asList(elementTypeTokens.toArray(new TypeToken[elementTypeTokens.size()])));
        }

        public String getName() {
            return name;
        }

        /**
         *
         * @return
         * read only list
         */
        public List<TypeToken> getElementTypeTokens() {
            return elementTypeTokens;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ComposedTypeToken
                    && this.elementTypeTokens.equals(((ComposedTypeToken) obj).elementTypeTokens);
        }

        @Override
        public LType toType(TypeEnv env) throws TypeException {
            Objects.requireNonNull(env);
            switch(this.name) {
            case "Array":
                if(this.elementTypeTokens.size() != 1) {  typeError("Array type require 1 element"); }
                return env.getArrayType(this.elementTypeTokens.get(0).toType(env));
            case "Optional":
                if(this.elementTypeTokens.size() != 1) {  typeError("Optional type require 1 element"); }
                return env.getOptionalType(this.elementTypeTokens.get(0).toType(env));
            case "Tuple": {
                final int size = this.elementTypeTokens.size();
                LType[] types = new LType[size];
                for(int i = 0; i < size; i++) {
                    types[i] = this.elementTypeTokens.get(i).toType(env);
                }
                return env.getTupleType(types);
            }
            case "Union":{
                final int size = this.elementTypeTokens.size();
                LType[] types = new LType[size];
                for(int i = 0; i < size; i++) {
                    types[i] = this.elementTypeTokens.get(i).toType(env);
                }
                return env.getUnionType(types);
            }
            }
            typeError("illegal composed type");
            return null;
        }
    }
}
