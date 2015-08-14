package loglang;

import nez.ast.CommonTree;

/**
 * Created by skgchxngsxyz-osx on 15/08/14.
 */
@FunctionalInterface
public interface CaseContext {
    void invoke(CommonTree tree);
}
