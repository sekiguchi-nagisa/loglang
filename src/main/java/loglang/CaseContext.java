package loglang;

import nez.ast.Tree;

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
    void invoke(Tree<?> prefixTree, Tree<?> bodyTree);
}
