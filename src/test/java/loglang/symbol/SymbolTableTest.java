package loglang.symbol;

import loglang.misc.Utils;
import loglang.symbol.ClassScope;
import loglang.symbol.MemberRef;
import loglang.symbol.SymbolTable;
import loglang.type.LType;
import loglang.type.TypeEnv;
import loglang.type.TypeException;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/25.
 */
public class SymbolTableTest {
    public static LType of(Class<?> clazz) {
        return new LType(clazz, null);
    }


    @Test
    public void test() throws TypeException {
        TypeEnv env = new TypeEnv();
        SymbolTable symbolTable = new SymbolTable();
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
        scope.entryScope();

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
        scope.entryScope();

        e = scope.newLocalEntry("d", of(float.class), true);
        assertEquals(1, e.getIndex());
        assertEquals(of(float.class), e.getFieldType());
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.READ_ONLY));
        assertTrue(Utils.hasFlag(e.getAttribute(), MemberRef.LOCAL_VAR));

        scope.exitScope();

        assertEquals(3, scope.getMaximumLocalSize());

        // long or double(consume 2 entry)
        scope.entryScope();

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
}