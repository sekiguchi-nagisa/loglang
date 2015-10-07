package loglang.symbol;

import loglang.misc.Utils;
import nez.peg.tpeg.type.LType;
import nez.peg.tpeg.type.TypeEnv;
import nez.peg.tpeg.type.TypeEnvHelper;
import nez.peg.tpeg.type.TypeException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/25.
 */
public class SymbolTableTest {
    private TypeEnv env;
    private SymbolTable symbolTable;

    private LType intType;
    private LType floatType;
    private LType boolType;
    private LType stringType;
    private LType anyType;


    public static LType of(Class<?> clazz) {
        return new LType(clazz, null);
    }

    @Before
    public void setup() {
        this.env = TypeEnvHelper.newTypeEnv();
        this.symbolTable = new SymbolTable(this.env);

        this.intType = this.env.getIntType();
        this.floatType = this.env.getFloatType();
        this.boolType = this.env.getBoolType();
        this.stringType = this.env.getStringType();
        this.anyType = this.env.getAnyType();
    }


    @Test
    public void test() throws TypeException {
        final String label = "hoge";
        ClassScope scope = symbolTable.newCaseScope(env, label);

        assertEquals(1, symbolTable.getCasesSize());

        scope.enterMethod();

        // state entry
        MemberRef.FieldRef e = scope.newStateEntry("a", of(int.class), false);
        assertEquals(-1, e.getIndex());
        assertEquals(of(int.class), e.getFieldType());
        assertFalse(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.INSTANCE_FIELD));

        assertEquals(null, scope.newStateEntry("a", of(String.class), true));   // duplicated
        assertEquals(null, scope.newLocalEntry("a", of(String.class), true));   // duplicated

        // local entry
        e = scope.newLocalEntry("b", of(float.class), true);
        assertEquals(0, e.getIndex());
        assertEquals(of(float.class), e.getFieldType());
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        assertEquals(null, scope.newLocalEntry("b", of(String.class), true));   // duplicated

        // block scope
        scope.enterScope();

        e = scope.newLocalEntry("c", of(boolean.class), true);
        assertEquals(1, e.getIndex());
        assertEquals(of(boolean.class), e.getFieldType());
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        // override
        e = scope.newLocalEntry("b", of(String.class), false);
        assertEquals(2, e.getIndex());
        assertEquals(of(String.class), e.getFieldType());
        assertFalse(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        // lookup
        e = scope.findEntry("a");
        assertNotNull(e);
        assertEquals(-1, e.getIndex());
        assertEquals(of(int.class), e.getFieldType());
        assertFalse(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.INSTANCE_FIELD));

        e = scope.findEntry("b");
        assertNotNull(e);
        assertEquals(2, e.getIndex());
        assertEquals(of(String.class), e.getFieldType());
        assertFalse(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        e = scope.findEntry("c");
        assertNotNull(e);
        assertEquals(1, e.getIndex());
        assertEquals(of(boolean.class), e.getFieldType());
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        scope.exitScope();

        e = scope.findEntry("c");
        assertNull(e);  // not found due to outer scope

        assertEquals(3, scope.getMaximumLocalSize());

        // reenter block scope
        scope.enterScope();

        e = scope.newLocalEntry("d", of(float.class), true);
        assertEquals(1, e.getIndex());
        assertEquals(of(float.class), e.getFieldType());
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        scope.exitScope();

        assertEquals(3, scope.getMaximumLocalSize());

        // long or double(consume 2 entry)
        scope.enterScope();

        e = scope.newLocalEntry("d", of(long.class), false);
        assertEquals(1, e.getIndex());
        assertEquals(of(long.class), e.getFieldType());
        assertFalse(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        e = scope.newLocalEntry("e", of(double.class), false);
        assertEquals(3, e.getIndex());
        assertEquals(of(double.class), e.getFieldType());
        assertFalse(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));


        scope.exitScope();

        assertEquals(5, scope.getMaximumLocalSize());


        scope.exitMethod();
    }

    private MemberRef.StaticMethodRef findUnary(String opName) {
        MemberRef.StaticMethodRef ref = symbolTable.findOperator(opName, 1);
        assertNotNull(ref);
        return ref;
    }

    private MemberRef.StaticMethodRef findBinary(String opName) {
        MemberRef.StaticMethodRef ref = symbolTable.findOperator(opName, 2);
        assertNotNull(ref);
        return ref;
    }

    private static int countOverloadedMethods(MemberRef.StaticMethodRef ref) {
        int count = 1;
        for(; ref.getNext() != null; ref = (MemberRef.StaticMethodRef)ref.getNext()) {
            count++;
        }
        return count;
    }

    private static boolean matchMethod(MemberRef.StaticMethodRef ref,
                                       LType returnType, String methodName, LType ... paramTypes) {
        assertNotNull(returnType);
        assertEquals(paramTypes.length, ref.getParamTypes().size());
        assertNotNull(methodName);
        assertEquals(methodName, ref.getInternalName());
        for(int i = 0; i < paramTypes.length; i++) {
            if(!ref.paramTypes.get(i).equals(paramTypes[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchOverloadedMehtod(MemberRef.StaticMethodRef ref,
                                                 LType returnType, String methodName, LType ... paramTypes) {
        do {
            if(matchMethod(ref, returnType, methodName, paramTypes)) {
                return true;
            }
            ref = (MemberRef.StaticMethodRef)ref.getNext();
        } while(ref != null);
        return false;
    }


    /**
     * operator lookup test
     * @throws Exception
     */
    @Test
    public void test2() throws Exception {
        // unary op
        // PLUS
        MemberRef.StaticMethodRef op = this.findUnary("+");
        assertEquals(2, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "plus", intType));
        assertTrue(matchOverloadedMehtod(op, floatType, "plus", floatType));
        assertFalse(matchOverloadedMehtod(op, boolType, "plus", boolType));

        // MINUS
        op = this.findUnary("-");
        assertEquals(2, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "minus", intType));
        assertTrue(matchOverloadedMehtod(op, floatType, "minus", floatType));

        // NOT
        op = this.findUnary("!");
        assertEquals(2, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "not", intType));
        assertTrue(matchOverloadedMehtod(op, boolType, "not", boolType));

        // binary op
        // ADD
        op = this.findBinary("+");
        assertEquals(3, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "add", intType, intType));
        assertTrue(matchOverloadedMehtod(op, floatType, "add", floatType, floatType));
        assertTrue(matchOverloadedMehtod(op, stringType, "add", stringType, anyType));

        // SUB
        op = this.findBinary("-");
        assertEquals(2, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "sub", intType, intType));
        assertTrue(matchOverloadedMehtod(op, floatType, "sub", floatType, floatType));

        // MUL
        op = this.findBinary("*");
        assertEquals(2, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "mul", intType, intType));
        assertTrue(matchOverloadedMehtod(op, floatType, "mul", floatType, floatType));

        // DIV
        op = this.findBinary("/");
        assertEquals(2, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "div", intType, intType));
        assertTrue(matchOverloadedMehtod(op, floatType, "div", floatType, floatType));

        // MOD
        op = this.findBinary("%");
        assertEquals(1, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "mod", intType, intType));

        // EQ
        op = this.findBinary("==");
        assertEquals(4, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, boolType, "eq", intType, intType));
        assertTrue(matchOverloadedMehtod(op, boolType, "eq", floatType, floatType));
        assertTrue(matchOverloadedMehtod(op, boolType, "eq", boolType, boolType));
        assertTrue(matchOverloadedMehtod(op, boolType, "eq", stringType, stringType));

        // NE
        op = this.findBinary("!=");
        assertEquals(4, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, boolType, "ne", intType, intType));
        assertTrue(matchOverloadedMehtod(op, boolType, "ne", floatType, floatType));
        assertTrue(matchOverloadedMehtod(op, boolType, "ne", boolType, boolType));
        assertTrue(matchOverloadedMehtod(op, boolType, "ne", stringType, stringType));

        // LT
        op = this.findBinary("<");
        assertEquals(3, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, boolType, "lt", intType, intType));
        assertTrue(matchOverloadedMehtod(op, boolType, "lt", floatType, floatType));
        assertTrue(matchOverloadedMehtod(op, boolType, "lt", stringType, stringType));

        // GT
        op = this.findBinary(">");
        assertEquals(3, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, boolType, "gt", intType, intType));
        assertTrue(matchOverloadedMehtod(op, boolType, "gt", floatType, floatType));
        assertTrue(matchOverloadedMehtod(op, boolType, "gt", stringType, stringType));

        // LE
        op = this.findBinary("<=");
        assertEquals(3, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, boolType, "le", intType, intType));
        assertTrue(matchOverloadedMehtod(op, boolType, "le", floatType, floatType));
        assertTrue(matchOverloadedMehtod(op, boolType, "le", stringType, stringType));

        // GE
        op = this.findBinary(">=");
        assertEquals(3, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, boolType, "ge", intType, intType));
        assertTrue(matchOverloadedMehtod(op, boolType, "ge", floatType, floatType));
        assertTrue(matchOverloadedMehtod(op, boolType, "ge", stringType, stringType));

        // AND
        op = this.findBinary("&");
        assertEquals(1, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "and", intType, intType));

        // OR
        op = this.findBinary("|");
        assertEquals(1, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "or", intType, intType));

        // XOR
        op = this.findBinary("^");
        assertEquals(1, countOverloadedMethods(op));
        assertTrue(matchOverloadedMehtod(op, intType, "xor", intType, intType));
    }
}