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
        CaseScope scope = symbolTable.newCaseScope(label);

        assertEquals(1, symbolTable.getCasesSize());

        scope.enterMethod();

        // state entry
        CaseScope.SymbolEntry e = scope.newStateEntry("a", int.class, false);
        assertEquals(-1, e.index);
        assertEquals(int.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, CaseScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.INSTANCE_FIELD));

        assertEquals(null, scope.newStateEntry("a", String.class, true));   // duplicated

        // local entry
        e = scope.newLocalEntry("b", float.class, true);
        assertEquals(0, e.index);
        assertEquals(float.class, e.type);
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.LOCAL_VAR));

        assertEquals(null, scope.newLocalEntry("b", String.class, true));   // duplicated

        // block scope
        scope.entryScope();

        e = scope.newLocalEntry("c", boolean.class, true);
        assertEquals(1, e.index);
        assertEquals(boolean.class, e.type);
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.LOCAL_VAR));

        // override
        e = scope.newLocalEntry("b", String.class, false);
        assertEquals(2, e.index);
        assertEquals(String.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, CaseScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.LOCAL_VAR));

        // lookup
        e = scope.findEntry("a");
        assertNotNull(e);
        assertEquals(-1, e.index);
        assertEquals(int.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, CaseScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.INSTANCE_FIELD));

        e = scope.findEntry("b");
        assertNotNull(e);
        assertEquals(2, e.index);
        assertEquals(String.class, e.type);
        assertFalse(Utils.hasFlag(e.attribute, CaseScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.LOCAL_VAR));

        e = scope.findEntry("c");
        assertNotNull(e);
        assertEquals(1, e.index);
        assertEquals(boolean.class, e.type);
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.READ_ONLY));
        assertTrue(Utils.hasFlag(e.attribute, CaseScope.LOCAL_VAR));

        scope.exitScope();

        e = scope.findEntry("c");
        assertNull(e);  // not found due to outer scope

        assertEquals(3, scope.getMaximumLocalSize());


        scope.exitMethod();
    }
}