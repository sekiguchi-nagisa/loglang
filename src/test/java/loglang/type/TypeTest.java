package loglang.type;

import loglang.TypeException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by skgchxngsxyz-opensuse on 15/09/02.
 */
public class TypeTest {
    private TypeEnv env = null;

    @Before
    public void setup() {
        this.env = new TypeEnv();
    }

    @Test
    public void testBase() {
        assertTrue(LType.anyType == this.env.getAnyType());
        assertEquals(LType.anyType, this.env.getAnyType());

        assertTrue(LType.voidType == this.env.getVoidType());
        assertEquals(LType.voidType, this.env.getVoidType());
    }

    @Test
    public void testMangling() {   // mangling
        // basic type
        assertEquals("B3int", Mangler.mangleBasicType("int"));
        assertEquals("B3int", this.env.getIntType().getUniqueName());

        // array type
        assertEquals("C5ArrayE1B3int", Mangler.mangleArrayType(this.env.getIntType()));

        try {
            Mangler.mangleArrayType(LType.voidType);
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        // optional type
        assertEquals("C8OptionalE1B4bool", Mangler.mangleOptionalType(this.env.getBoolType()));

        try {
            Mangler.mangleOptionalType(LType.voidType);
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        // tuple type
        assertEquals("C5TupleE2B3intB5float",
                Mangler.mangleTupleType(new LType[]{this.env.getIntType(), this.env.getFloatType()}));

        try {
            Mangler.mangleTupleType(new LType[]{this.env.getIntType()});
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            Mangler.mangleTupleType(new LType[]{this.env.getVoidType()});
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }


        // union type
        assertEquals("C5UnionE3B3intB4boolB5float",
                Mangler.mangleUnionType(new LType[]{this.env.getIntType(), this.env.getBoolType(), this.env.getFloatType()}));

        assertEquals("C5UnionE3B3intB4boolB5float",
                Mangler.mangleUnionType(new LType[]{this.env.getIntType(), this.env.getFloatType(),
                        this.env.getBoolType(), this.env.getIntType()}));   // has duplicated element

        try {
            Mangler.mangleUnionType(new LType[] {this.env.getIntType(), this.env.getIntType()});
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            Mangler.mangleUnionType(new LType[]{this.env.getVoidType(), this.env.getIntType()});
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testTypeEnvAPI() throws TypeException {
        // basic type
        LType type = this.env.getBasicType("int");
        assertTrue(type != null);

        try {
            type = this.env.getBasicType("Int");    // not found
            assertTrue(type != null);  // unreachable
        } catch(Exception e) {
            assertTrue(e instanceof TypeException);
            assertEquals("undefined type: Int", e.getMessage());
        }

        // array type
        type = this.env.getArrayType(this.env.getBoolType());
        assertTrue(type instanceof LType.ArrayType);

        try {
            type = this.env.getArrayType(this.env.getVoidType());
            assertTrue(type instanceof LType.ArrayType);
        } catch(Exception e) {
            assertTrue(e instanceof TypeException);
        }

        // optional type
        type = this.env.getOptionalType(this.env.getFloatType());
        assertTrue(type instanceof LType.OptionalType);

        try {
            type = this.env.getOptionalType(this.env.getVoidType());
            assertTrue(type instanceof LType.OptionalType);
        } catch(Exception e) {
            assertTrue(e instanceof TypeException);
        }

        // tuple type
        type = this.env.getTupleType(new LType[] {this.env.getIntType(), this.env.getIntType()});
        assertTrue(type instanceof LType.TupleType);

        try {
            this.env.getTupleType(new LType[]{this.env.getVoidType(), this.env.getIntType()});
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof TypeException);
        }

        // union type
        type = this.env.getUnionType(new LType[] {this.env.getIntType(), this.env.getStringType()});
        assertTrue(type instanceof LType.UnionType);

        try {
            this.env.getUnionType(new LType[]{this.env.getVoidType()});
            assertTrue(false);
        } catch(Exception e) {
            assertTrue(e instanceof TypeException);
        }
    }

    @Test
    public void testDemangling() throws TypeException {
        // basic type
        String simpleName = "int";
        String mangledName = Mangler.mangleBasicType("int");
        LType type = this.env.getIntType();

        assertEquals(simpleName, Mangler.demangle(mangledName));
        assertEquals(mangledName, type.getUniqueName());
        assertEquals(simpleName, type.getSimpleName());

        // array type
        simpleName = "Array<bool>";
        mangledName = Mangler.mangleArrayType(this.env.getBoolType());
        type = this.env.getArrayType(this.env.getBoolType());

        assertEquals(simpleName, Mangler.demangle(mangledName));
        assertEquals(mangledName, type.getUniqueName());
        assertEquals(simpleName, type.getSimpleName());

        // optional type
        simpleName = "Optional<int>";
        mangledName = Mangler.mangleOptionalType(this.env.getIntType());
        type = this.env.getOptionalType(this.env.getIntType());

        assertEquals(simpleName, Mangler.demangle(mangledName));
        assertEquals(mangledName, type.getUniqueName());
        assertEquals(simpleName, type.getSimpleName());

        // tuple test
        simpleName = "Tuple<float,int>";
        mangledName = Mangler.mangleTupleType(new LType[]{this.env.getFloatType(), this.env.getIntType()});
        type = this.env.getTupleType(new LType[]{this.env.getFloatType(), this.env.getIntType()});

        assertEquals(simpleName, Mangler.demangle(mangledName));
        assertEquals(mangledName, type.getUniqueName());
        assertEquals(simpleName, type.getSimpleName());

        // union test
        simpleName = "Union<int,float>";
        mangledName = Mangler.mangleUnionType(new LType[] {this.env.getFloatType(), this.env.getIntType()});
        type = this.env.getUnionType(new LType[] {this.env.getFloatType(), this.env.getIntType()});

        assertEquals(simpleName, Mangler.demangle(mangledName));
        assertEquals(mangledName, type.getUniqueName());
        assertEquals(simpleName, type.getSimpleName());
    }

    @Test
    public void testTypeAPI() throws TypeException {
        // basic type
        assertTrue(this.env.getAnyType().isSameOrBaseOf(this.env.getIntType()));
        assertFalse(this.env.getAnyType().isSameOrBaseOf(this.env.getVoidType()));
        assertTrue(this.env.getAnyType().isSameOrBaseOf(this.env.getStringType()));

        // array type
        assertTrue(this.env.getAnyType().isSameOrBaseOf(this.env.getArrayType(this.env.getIntType())));
        assertTrue(this.env.getArrayType(this.env.getAnyType())
                .isSameOrBaseOf(this.env.getArrayType(this.env.getStringType())));
        assertEquals(this.env.getBoolType(), this.env.getArrayType(this.env.getBoolType()).getElementType());

        // optional type
        assertTrue(this.env.getAnyType().isSameOrBaseOf(this.env.getOptionalType(this.env.getBoolType())));
        assertTrue(this.env.getOptionalType(this.env.getAnyType())
                .isSameOrBaseOf(this.env.getOptionalType(this.env.getStringType())));
        assertEquals(this.env.getBoolType(), this.env.getOptionalType(this.env.getBoolType()).getElementType());

        // tuple type
        LType.TupleType tupleType = this.env.getTupleType(new LType[]{this.env.getIntType(), this.env.getFloatType()});
        assertTrue(this.env.getAnyType().isSameOrBaseOf(tupleType));
        assertEquals(2, tupleType.getElementTypes().size());
        assertEquals(this.env.getIntType(), tupleType.getElementTypes().get(0));
        assertEquals(this.env.getFloatType(), tupleType.getElementTypes().get(1));

        // union type
        assertTrue(this.env.getAnyType()
                .isSameOrBaseOf(this.env.getUnionType(new LType[]{this.env.getFloatType(), this.env.getIntType()})));

        assertTrue(this.env.getUnionType(new LType[]{this.env.getIntType(), this.env.getStringType()})
                .isSameOrBaseOf(this.env.getIntType()));

        assertTrue(this.env.getUnionType(new LType[] {this.env.getStringType(), this.env.getIntType(), this.env.getBoolType()})
                .isSameOrBaseOf(this.env.getUnionType(new LType[]{this.env.getStringType(), this.env.getBoolType()})));

        // merge union
        LType.UnionType unionType1 = this.env.getUnionType(new LType[] {this.env.getBoolType(), this.env.getIntType()});
        LType.UnionType unionType2 = this.env.getUnionType(new LType[] {this.env.getStringType(), this.env.getIntType()});
        LType.UnionType unionType3 = this.env.getUnionType(new LType[] {unionType1, unionType2});

        assertTrue(unionType3.isSameOrBaseOf(unionType1));
        assertTrue(unionType3.isSameOrBaseOf(unionType2));

        assertEquals(3, unionType3.getElementTypes().size());
        assertEquals(this.env.getIntType(), unionType3.getElementTypes().get(0));
        assertEquals(this.env.getBoolType(), unionType3.getElementTypes().get(1));
        assertEquals(this.env.getStringType(), unionType3.getElementTypes().get(2));
    }
}
