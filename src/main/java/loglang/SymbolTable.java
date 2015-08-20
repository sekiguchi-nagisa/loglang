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
public class SymbolTable {
    private final ArrayList<Scope> scopes;
    private final ArrayList<Integer> indexCounters;


    public SymbolTable() {
        this.scopes = new ArrayList<>();
        this.indexCounters = new ArrayList<>();
    }


    /**
     *
     * @param symbolName
     * @return
     * if not found, return null.
     */
    public SymbolEntry findEntry(String symbolName) {
        final int size = this.scopes.size();
        for(int i = size - 1; i > -1; i--) {
            SymbolEntry e = this.scopes.get(i).find(symbolName);
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

    public int getMaximumIndex() {
        return Utils.peek(this.indexCounters);
    }

    /**
     *
     * @param symbolName
     * @param type
     * @param attribute
     * @return
     * if entry creation failed(found duplicated entry), return null.
     */
    public SymbolEntry newEntry(String symbolName, Type type, int attribute) {
        SymbolEntry e = new SymbolEntry(Utils.peek(this.scopes).curIndex, type, attribute);
        if(!Utils.peek(this.scopes).add(symbolName, e)) {
            return null;
        }
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
