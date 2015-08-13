package loglang;

import static loglang.misc.ArgsParser.*;
import loglang.misc.ArgsParser;

/**
 * Created by skgchxngsxyz-osx on 15/08/10.
 */
public class Main {
    private static String scriptFileName = null;
    private static String inputFileName = null;

    public static void main(String[] args) {
        ArgsParser parser = new ArgsParser();

        parser.addOption("s", "script", HAS_ARG | REQUIRE,
                "specify script file name",
                (a) -> scriptFileName = a.get());
        parser.addOption("i", "input", HAS_ARG | REQUIRE,
                "specify intput file name",
                (a) -> inputFileName = a.get());
        parser.addOption("h", "help", IGNORE_REST,
                "show this help message",
                (a) -> {parser.printHelp(System.out); System.exit(0); });

        try {
            parser.parse(args);
        } catch(IllegalArgumentException e) {
            System.err.println(e.getMessage());
            parser.printHelp(System.err);
            System.exit(1);
        }

        Loglang ll = new LoglangFactory().newLoglang(scriptFileName);
        ll.invoke(inputFileName);
    }
}
