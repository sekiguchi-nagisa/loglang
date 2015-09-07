package loglang;

import nez.ast.CommonTree;

/**
 * Created by skgchxngsxyz-osx on 15/08/14.
 */
@FunctionalInterface
public interface CaseContext {
    /**
     *
     * @param prefixTree
     * may be null if has no  prefix
     * @param bodyTree
     */
    void invoke(CommonTree prefixTree, CommonTree bodyTree);
}
