package loglang;

import loglang.misc.Utils;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/25.
 */
public class SymbolTableTest {
    @Test
    public void test() {
        SymbolTable symbolTable = new SymbolTable();
        final String label = "hoge";
        ClassScope scope = symbolTable.newCaseScope(label);

        assertEquals(1, symbolTable.getCasesSize());

        scope.enterMethod();

        // state entry
        ClassScope.SymbolEntry e = scope.newStateEntry("a", int.class, false);
        assertEquals(-1, e.index);
        assertEquals(int.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.INSTANCE_FIELD));

        assertEquals(null, scope.newStateEntry("a", String.class, true));   // duplicated
        assertEquals(null, scope.newLocalEntry("a", String.class, true));   // duplicated

        // local entry
        e = scope.newLocalEntry("b", float.class, true);
        assertEquals(0, e.index);
        assertEquals(float.class, e.type);
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));

        assertEquals(null, scope.newLocalEntry("b", String.class, true));   // duplicated

        // block scope
        scope.entryScope();

        e = scope.newLocalEntry("c", boolean.class, true);
        assertEquals(1, e.index);
        assertEquals(boolean.class, e.type);
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));

        // override
        e = scope.newLocalEntry("b", String.class, false);
        assertEquals(2, e.index);
        assertEquals(String.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));

        // lookup
        e = scope.findEntry("a");
        assertNotNull(e);
        assertEquals(-1, e.index);
        assertEquals(int.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.INSTANCE_FIELD));

        e = scope.findEntry("b");
        assertNotNull(e);
        assertEquals(2, e.index);
        assertEquals(String.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));

        e = scope.findEntry("c");
        assertNotNull(e);
        assertEquals(1, e.index);
        assertEquals(boolean.class, e.type);
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));

        scope.exitScope();

        e = scope.findEntry("c");
        assertNull(e);  // not found due to outer scope

        assertEquals(3, scope.getMaximumLocalSize());

        // reenter block scope
        scope.entryScope();

        e = scope.newLocalEntry("d", float.class, true);
        assertEquals(1, e.index);
        assertEquals(float.class, e.type);
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));

        scope.exitScope();

        assertEquals(3, scope.getMaximumLocalSize());

        // long or double(consume 2 entry)
        scope.entryScope();

        e = scope.newLocalEntry("d", long.class, false);
        assertEquals(1, e.index);
        assertEquals(long.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));

        e = scope.newLocalEntry("e", double.class, false);
        assertEquals(3, e.index);
        assertEquals(double.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, ClassScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, ClassScope.LOCAL_VAR));


        scope.exitScope();

        assertEquals(5, scope.getMaximumLocalSize());


        scope.exitMethod();
    }
}