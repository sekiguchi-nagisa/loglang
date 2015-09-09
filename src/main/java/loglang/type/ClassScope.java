package loglang.type;

import loglang.misc.Utils;
import static loglang.type.MemberRef.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * for local variable and instance field management
 */
public class ClassScope {
    private final LType.AbstractStructureType type;
    private final ArrayList<Scope> scopes = new ArrayList<>();
    private final ArrayList<Integer> indexCounters = new ArrayList<>();

    ClassScope(LType.AbstractStructureType type) {
        this.type = Objects.requireNonNull(type);
    }

    /**
     *
     * @param symbolName
     * not null
     * @return
     * if not found, return null.
     */
    public FieldRef findEntry(String symbolName) {
        // first, search state entry
        FieldRef e = this.findField(symbolName);
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

    private FieldRef findField(String fieldName) {
        return this.type.lookupField(fieldName);
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
    public FieldRef newLocalEntry(String symbolName, LType type, boolean readOnly) {
        int attribute = MemberRef.LOCAL_VAR;
        if(readOnly) {
            attribute = Utils.setFlag(attribute, MemberRef.READ_ONLY);
        }
        return this.newScopedEntry(symbolName, type, attribute, LType.anyType);
    }

    public FieldRef newPrefixTreeFieldEntry(String symbolName, LType type, LType ownerType) {
        int attribute = MemberRef.PREFIX_TREE_FIELD | MemberRef.READ_ONLY;
        return this.newScopedEntry(symbolName, type, attribute, ownerType);
    }

    public FieldRef newCaseTreeFieldEntry(String symbolName, LType type, LType ownerType) {
        int attribute = MemberRef.CASE_TREE_FIELD | MemberRef.READ_ONLY;
        return this.newScopedEntry(symbolName, type, attribute, ownerType);
    }

    /**
     * generate symbol entry in scope
     * @param symbolName
     * @param type
     * @param attribute
     * @return
     * if entry creation failed(found duplicated entry), return null.
     */
    private FieldRef newScopedEntry(String symbolName, LType type, int attribute, LType ownerType) {
        // check state variable
        if(this.findField(symbolName) != null) {
            return null;    // already defined
        }
        return Utils.peek(this.scopes).newEntry(symbolName, type, attribute, ownerType);
    }

    /**
     * generate symbol entry in instance field.
     * @param symbolName
     * @param type
     * @param readOnly
     * @return
     * if entry creation failed(found duplicated entry), return null.
     */
    public FieldRef newStateEntry(String symbolName, LType type, boolean readOnly) {
        return this.type.addField(symbolName, type);    //FIXME: read-only
    }


    /**
     * for local variable scope management
     */
    private static class Scope {
        private int curIndex;
        private final Map<String, FieldRef> entryMap;

        private Scope(int curIndex) {
            this.curIndex = curIndex;
            this.entryMap = new HashMap<>();
        }

        private FieldRef find(String symbolName) {
            return this.entryMap.get(symbolName);
        }

        /**
         *
         * @param name
         * @param type
         * @param attribute
         * @param ownerType
         * if generated entry represents local variable, ownerType is always anyType
         * @return
         */
        private FieldRef newEntry(String name, LType type, int attribute, LType ownerType) {
            Objects.requireNonNull(name);
            Objects.requireNonNull(type);

            if(this.entryMap.containsKey(name)) {
                return null;
            }

            FieldRef entry = new FieldRef(this.curIndex, type, name, ownerType, attribute);
            this.entryMap.put(name, entry);
            this.curIndex += type.stackConsumption();
            return entry;
        }
    }
}
