package loglang;

import loglang.misc.Utils;

/**
 * Created by skgchxngsxyz-opensuse on 15/08/18.
 */
public final class Config {
    private Config() { }

    public final static boolean dumpPattern = Utils.checkProperty("dumpPattern", false);
    public final static boolean dumpByteCode = Utils.checkProperty("dumpByteCode", false);
    public final static boolean pegOnly = Utils.checkProperty("pegOnly", false);
    public final static boolean noAction = Utils.checkProperty("noAction", false);

    /**
     * for assert statement
     */
    public final static boolean noAssert = Utils.checkProperty("noAssert", false);
}
