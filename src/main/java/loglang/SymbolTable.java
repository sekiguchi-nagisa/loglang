package loglang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by skgchxngsxyz-osx on 15/08/25.
 */
public class SymbolTable {
    private final ArrayList<ClassScope> cases = new ArrayList<>();
    private final Map<String, Integer> labelMap = new HashMap<>();

    /**
     *
     * @param labelName
     * may be null, if has no label name
     * @return
     * created case scope
     */
    public ClassScope newCaseScope(String labelName) {
        ClassScope scope = new ClassScope();
        this.cases.add(scope);
        int index = this.cases.size() - 1;
        if(labelName != null) {
            if(this.labelMap.put(labelName, index) != null) {
                return null;    // found duplicated label
            }
        }
        return scope;
    }

    /**
     *
     * @param index
     * @return
     * @throws IndexOutOfBoundsException
     * if not found
     */
    public ClassScope findCaseScope(int index) throws IndexOutOfBoundsException {
        return this.cases.get(index);
    }

    /**
     *
     * @param labelName
     * not null
     * @return
     * if not found, return null
     */
    public ClassScope findCaseScope(String labelName) {
        Integer boxed = this.labelMap.get(labelName);
        if(boxed != null) {
            return this.cases.get(boxed.intValue());
        }
        return null;
    }

    public int getCasesSize() {
        return this.cases.size();
    }
}