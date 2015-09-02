package loglang.type;

import java.util.*;
import static loglang.SemanticException.*;

/**
 * Created by skgchxngsxyz-opensuse on 15/09/02.
 */
public interface TypeToken {
    LType toType(TypeEnv env);

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
        public LType toType(TypeEnv env) {
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
                            Arrays.asList(elementTypeTokens.toArray(new TypeToken[0])));
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
        public LType toType(TypeEnv env) {
            Objects.requireNonNull(env);
            switch(this.name) {
            case "Array":
                if(this.elementTypeTokens.size() != 1) {  semanticError("Array type require 1 element"); }
                return env.getArrayType(this.elementTypeTokens.get(0).toType(env));
            case "Optional":
                if(this.elementTypeTokens.size() != 1) {  semanticError("Optional type require 1 element"); }
                return env.getOptionalType(this.elementTypeTokens.get(0).toType(env));
            case "Tuple":
                return env.getTupleType(
                        this.elementTypeTokens
                                .stream()
                                .map((t) -> t.toType(env))
                                .toArray(LType[]::new)
                );
            case "Union":
                return env.getUnionType(
                        this.elementTypeTokens
                                .stream()
                                .map((t) -> t.toType(env))
                                .toArray(LType[]::new)
                );
            }
            semanticError("illegal composed type");
            return null;
        }
    }
}
