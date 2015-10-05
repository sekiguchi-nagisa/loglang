package loglang.symbol;

import loglang.misc.Utils;
import loglang.TypeUtil;
import nez.peg.tpeg.type.LType;

import static loglang.symbol.MemberRef.*;

import java.util.*;

/**
 * for local variable and instance field management
 */
public class ClassScope {
    /**
     * owner of class scope
     */
    private final LType.AbstractStructureType ownerType;

    private final Map<String, FieldRef> fieldMap = new HashMap<>();
    private final ArrayDeque<Scope> scopes = new ArrayDeque<>();
    private final ArrayDeque<Integer> indexCounters = new ArrayDeque<>();

    ClassScope(LType.AbstractStructureType ownerType) {
        this.ownerType = Objects.requireNonNull(ownerType);
    }

    public LType.AbstractStructureType getOwnerType() {
        return this.ownerType;
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
        for(Scope scope : this.scopes) {
            e = scope.find(symbolName);
            if(Objects.nonNull(e)) {
                return e;
            }
        }
        return null;
    }

    private FieldRef findField(String fieldName) {
        return this.fieldMap.get(fieldName);
    }

    /**
     *
     * @param initLocalSize
     * if this method represents static method, initLocalSize is 0.
     * if this method represents instance method, initLocalSize is 1
     */
    public void enterMethod(int initLocalSize) {
        this.scopes.push(new Scope(initLocalSize));
        this.indexCounters.push(initLocalSize);
    }

    /**
     * equivalent ot enterMethod(0)
     */
    public void enterMethod() {
        this.enterMethod(0);
    }

    public void exitMethod() {
        this.scopes.pop();
        this.indexCounters.pop();
    }

    public void entryScope() {
        int index = this.scopes.peek().curIndex;
        this.scopes.push(new Scope(index));
    }

    public void exitScope() {
        Scope scope = this.scopes.pop();
        final int index = scope.curIndex;
        if(index > this.indexCounters.peek()) {
            this.indexCounters.pop();
            this.indexCounters.push(index);
        }
    }

    public int getMaximumLocalSize() {
        return this.indexCounters.peek();
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
        // check state variable
        if(this.findField(symbolName) != null) {
            return null;    // already defined
        }

        int attribute = MemberRef.LOCAL_VAR;
        if(readOnly) {
            attribute = Utils.setFlag(attribute, MemberRef.READ_ONLY);
        }
        return this.scopes.peek().newEntry(symbolName, type, attribute, LType.anyType);
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
        Objects.requireNonNull(symbolName); //FIXME: read only field
        Objects.requireNonNull(type);

        if(this.fieldMap.containsKey(symbolName)) {
            return null;
        }

        final int fieldIndex = -1;
        final int attribute = MemberRef.INSTANCE_FIELD;

        MemberRef.FieldRef ref = new MemberRef.FieldRef(fieldIndex, type, symbolName, ownerType, attribute);
        this.fieldMap.put(symbolName, ref);
        return ref;
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
            this.curIndex += TypeUtil.stackConsumption(type);
            return entry;
        }
    }
}
