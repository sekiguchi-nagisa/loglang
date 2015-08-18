package loglang;

import loglang.misc.Utils;

/**
 * Created by skgchxngsxyz-opensuse on 15/08/18.
 */
public final class Config {
    private Config() { }

    public final static boolean deletePattern = Utils.checkProperty("deletePattern", true);
    public final static boolean dumpByteCode = Utils.checkProperty("dumpByteCode", false);
}
