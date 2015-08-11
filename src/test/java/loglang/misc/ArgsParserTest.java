package loglang.misc;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/11.
 */
public class ArgsParserTest {

    @Test
    public void test() {    // empty args
        String[] args = {
        };

        boolean value[] = {false};
        ArgsParser parser = new ArgsParser();
        parser.addOption("h", "help", 0, "show this help message",
                (a) -> parser.printHelp(System.out));
        parser.addDefaultAction((a) -> value[0] = true);

        parser.parse(args);

        assertTrue(value[0]);
    }

    @Test
    public void test2() {
        String[] args = {"-d", "AAA"};

        boolean[] value = {false};

        ArgsParser parser = new ArgsParser();

        boolean f = false;
        try {
            parser.addOption("d", "double", 0, "hello",
                    (a) -> value[0] = true);
            parser.addOption("-d", null, 0, "hello",
                    (a) -> value[0] = true);
        } catch(IllegalArgumentException e) {
            assertEquals("duplicated option: -d", e.getMessage());
            f = true;
        }
        assertTrue(f);
    }

    @Test
    public void test3() {
        ArgsParser parser = new ArgsParser();

        boolean f = false;
        try {
            parser.addOption("d", "double", 0, "hello",
                    (a) -> {});
            parser.addOption("-f", "--double", 0, "hello",
                    (a) -> {});
        } catch(IllegalArgumentException e) {
            assertEquals("duplicated option: --double", e.getMessage());
            f = true;
        }
        assertTrue(f);
    }

    @Test
    public void test4() {
        ArgsParser parser = new ArgsParser();

        boolean f = false;
        try {
            parser.addOption("--d", "double", 0, "hello",
                    (a) -> {});
            parser.addOption("-f", "--ouble", 0, "hello",
                    (a) -> {});
        } catch(IllegalArgumentException e) {
            assertEquals("illegal short name: --d", e.getMessage());
            f = true;
        }
        assertTrue(f);
    }

    @Test
    public void test5() {
        ArgsParser parser = new ArgsParser();

        boolean f = false;
        try {
            parser.addOption("-d", "-double", 0, "hello",
                    (a) -> {});
            parser.addOption("-f", "--ouble", 0, "hello",
                    (a) -> {});
        } catch(IllegalArgumentException e) {
            assertEquals("illegal long name: -double", e.getMessage());
            f = true;
        }
        assertTrue(f);
    }

    @Test
    public void test6() {
        String[] args = {
                "-g", "-d", "--double", "--global",
                "-o", "OUT", "AAA",
        };
        ArgsParser parser = new ArgsParser();

        int[] value = {0};
        int[] value2 = {0};

        parser.addOption("d", "double", 0, "",
                (a) -> value[0]++);
        parser.addOption("g", "global", 0, "",
                (a) -> value2[0]++);
        parser.addOption("o", null, ArgsParser.HAS_ARG, "",
                (a) -> assertEquals("OUT", a.get()));

        String[] rest = parser.parse(args);
        assertEquals(2, value[0]);
        assertEquals(2, value2[0]);
        assertEquals(rest.length, 1);
        assertEquals(rest[0], "AAA");
    }

    @Test
    public void test7() {
        String[] args = {
                "-g", "-d", "-o", "--double", "--global",
                "OUT", "AAA",
        };
        ArgsParser parser = new ArgsParser();

        int[] value = {0};
        int[] value2 = {0};

        parser.addOption("d", "double", 0, "",
                (a) -> value[0]++);
        parser.addOption("g", "global", 0, "",
                (a) -> value2[0]++);
        parser.addOption("o", null, ArgsParser.IGNORE_REST, "",
                (a) -> {});

        String[] rest = parser.parse(args);
        assertEquals(1, value[0]);
        assertEquals(1, value2[0]);
        assertEquals(rest.length, 4);
        assertEquals(rest[0], "--double");
        assertEquals(rest[1], "--global");
        assertEquals(rest[2], "OUT");
        assertEquals(rest[3], "AAA");
    }

    @Test
    public void test8() {
        String[] args = {
                "-g", "-d", "-o", "EE", "--global",
                "OUT", "AAA",
        };
        ArgsParser parser = new ArgsParser();

        int[] value = {0};
        int[] value2 = {0};

        parser.addOption("d", "double", 0, "",
                (a) -> value[0]++);
        parser.addOption("g", "global", 0, "",
                (a) -> value2[0]++);
        parser.addOption("o", null, ArgsParser.IGNORE_REST | ArgsParser.HAS_ARG, "",
                (a) -> assertEquals("EE", a.get()));

        String[] rest = parser.parse(args);
        assertEquals(1, value[0]);
        assertEquals(1, value2[0]);
        assertEquals(rest.length, 3);
        assertEquals(rest[0], "--global");
        assertEquals(rest[1], "OUT");
        assertEquals(rest[2], "AAA");
    }

    @Test
    public void test9() {
        String[] args = {
                "-g", "-d", "--double", "--global",
                "-o", "-OUT", "AAA",
        };
        ArgsParser parser = new ArgsParser();

        int[] value = {0};
        int[] value2 = {0};

        parser.addOption("d", "double", 0, "",
                (a) -> value[0]++);
        parser.addOption("g", "global", 0, "",
                (a) -> value2[0]++);
        parser.addOption("o", null, ArgsParser.HAS_ARG, "",
                (a) -> assertEquals("-OUT", a.get()));

        boolean fail = false;
        try {
            parser.parse(args);
        } catch(IllegalArgumentException e) {
            assertEquals("require argument: -o", e.getMessage());
            fail = true;
        }
        assertTrue(fail);
    }

    @Test
    public void test10() {
        String[] args = {
                "-g", "-d", "--double", "--global",
        };
        ArgsParser parser = new ArgsParser();

        int[] value = {0};
        int[] value2 = {0};

        parser.addOption("d", "double", 0, "",
                (a) -> value[0]++);
        parser.addOption("g", "global", 0, "",
                (a) -> value2[0]++);
        parser.addOption("o", null, ArgsParser.REQUIRE, "",
                (a) -> assertEquals("-OUT", a.get()));

        boolean fail = false;
        try {
            parser.parse(args);
        } catch(IllegalArgumentException e) {
            assertEquals("require option: -o", e.getMessage());
            fail = true;
        }
        assertTrue(fail);
    }
}