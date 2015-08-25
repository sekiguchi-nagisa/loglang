package loglang;

import loglang.misc.Utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/19.
 */
public class CaseScope {
    private final Map<String, SymbolEntry> fieldMap = new HashMap<>();
    private final ArrayList<Scope> scopes = new ArrayList<>();
    private final ArrayList<Integer> indexCounters = new ArrayList<>();

    CaseScope() { } // not allow direct construction

    /**
     *
     * @param symbolName
     * not null
     * @return
     * if not found, return null.
     */
    public SymbolEntry findEntry(String symbolName) {
        // first, search state entry
        SymbolEntry e = this.fieldMap.get(Objects.requireNonNull(symbolName));
        if(e != null) {
            return e;
        }

        // if not found, search local entry
        final int size = this.scopes.size();
        for(int i = size - 1; i > -1; i--) {
            e = this.scopes.get(i).find(symbolName);
            if(Objects.nonNull(e)) {
                return e;
            }
        }
        return null;
    }

    public void enterMethod() {
        this.scopes.add(new Scope(0));
        this.indexCounters.add(0);
    }

    public void exitMethod() {
        Utils.pop(this.scopes);
        Utils.pop(this.indexCounters);
    }

    public void entryScope() {
        int index = Utils.peek(this.scopes).curIndex;
        this.scopes.add(new Scope(index));
    }

    public void exitScope() {
        Scope scope = Utils.pop(this.scopes);
        final int index = scope.curIndex;
        if(index > Utils.peek(this.indexCounters)) {
            this.indexCounters.set(this.indexCounters.size() - 1, index);
        }
    }

    public int getMaximumLocalSize() {
        return Utils.peek(this.indexCounters);
    }

    /**
     *
     * @param symbolName
     * @param type
     * @param readOnly
     * @return
     * if entry creation failed(found duplicated entry), return null.
     */
    public SymbolEntry newLocalEntry(String symbolName, Type type, boolean readOnly) {
        int attribute = LOCAL_VAR;
        if(readOnly) {
            attribute = Utils.setFlag(attribute, READ_ONLY);
        }
        SymbolEntry e = new SymbolEntry(Utils.peek(this.scopes).curIndex, type, attribute);
        if(!Utils.peek(this.scopes).add(symbolName, e)) {
            return null;
        }
        return e;
    }

    /**
     *
     * @param symbolName
     * @param type
     * @param readOnly
     * @return
     * if entry creation failed(found duplicated entry), return null.
     */
    public SymbolEntry newStateEntry(String symbolName, Type type, boolean readOnly) {
        // check duplication
        if(this.fieldMap.containsKey(Objects.requireNonNull(symbolName))) {
            return null;
        }

        int attribute = INSTANCE_FIELD;
        if(readOnly) {
            attribute = Utils.setFlag(attribute, READ_ONLY);
        }
        SymbolEntry e = new SymbolEntry(-1, type, attribute);
        this.fieldMap.put(symbolName, e);
        return e;
    }


    private static class Scope {
        private int curIndex;
        private final Map<String, SymbolEntry> entryMap;

        private Scope(int curIndex) {
            this.curIndex = curIndex;
            this.entryMap = new HashMap<>();
        }

        private SymbolEntry find(String symbolName) {
            return this.entryMap.get(symbolName);
        }

        private boolean add(String name, SymbolEntry entry) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(entry);

            if(this.entryMap.containsKey(name)) {
                return false;
            }
            this.entryMap.put(name, entry);
            this.curIndex++;
            return true;
        }
    }

    /**
     * for symbol entry attribute
     */
    public final static int READ_ONLY      = 1;
    public final static int LOCAL_VAR      = 1 << 1;
    public final static int INSTANCE_FIELD = 1 << 2;

    public static class SymbolEntry {
        /**
         * if entry represents instance field(state), index is -1.
         */
        public final int index;

        public final Type type;
        public final int attribute;

        private SymbolEntry(int index, Type type, int attribute) {
            this.index = index;
            this.type = type;
            this.attribute = attribute;
        }
    }
}
