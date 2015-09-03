package nez.ast;

import loglang.misc.LongRange;

/**
 * Created by skgchxngsxyz-osx on 15/09/03.
 */
public class ASTHelper {
    private ASTHelper() {}

    public static LongRange range(CommonTree tree) {
        return new LongRange(tree.pos, tree.length);
    }
}
