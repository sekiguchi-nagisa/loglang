package loglang.misc;

import java.io.PrintStream;
import java.util.*;

public class ArgsParser {
    public final static int HAS_ARG     = 1;
    public final static int REQUIRE     = 1 << 1;
    public final static int IGNORE_REST = 1 << 2;

    @FunctionalInterface
    public interface OptionListener {
        void invoke(Optional<String> arg);
    }
    private static final OptionListener nullListener = (arg)-> { };

    private OptionListener defaultListener = nullListener;    // do nothing

    /**
     * contains recognized options
     */
    private final List<Option> optionList = new ArrayList<>();

    /**
     * key is option name (short name or long name)
     */
    private final Map<String, Option> optionMap = new HashMap<>();

    private final Set<Option> requireOptionSet = new HashSet<>();

    private int maxSizeOfUsage;

    /**
     *
     * @param shortName
     * may be null. not start with '--'
     * @param longName
     * may be null. not start with '-'
     * @param flag
     * @param description
     * not null
     * @param listener
     * not null
     * @return
     * @throws IllegalArgumentException
     */
    public ArgsParser addOption(String shortName, String longName,
                                        int flag, String description,
                                        OptionListener listener) throws IllegalArgumentException {
        // check short name format
        String actualShortName = null;
        if(shortName != null) {
            if(shortName.startsWith("--") || shortName.trim().length() == 0) {
                throw new IllegalArgumentException("illegal short name: " + shortName);
            }
            actualShortName = shortName.startsWith("-") ? shortName : "-" + shortName;
            if(this.optionMap.containsKey(actualShortName)) {
                throw new IllegalArgumentException("duplicated option: " + actualShortName);
            }
        }

        // check long name format
        String actualLongName = null;
        if(longName != null) {
            if((!longName.startsWith("--") && longName.startsWith("-")) ||
                    longName.trim().length() == 0) {
                throw new IllegalArgumentException("illegal long name: " + longName);
            }
            actualLongName = longName.startsWith("--") ? longName : "--" + longName;
            if(this.optionMap.containsKey(actualLongName)) {
                throw new IllegalArgumentException("duplicated option: " + actualLongName);
            }
        }

        // add option
        Option option = new Option(actualShortName, actualLongName, flag,
                Objects.requireNonNull(description),
                Objects.requireNonNull(listener));
        this.optionList.add(option);
        if(Objects.nonNull(actualShortName)) {
            this.optionMap.put(actualShortName, option);
        }
        if(Objects.nonNull(actualLongName)) {
            this.optionMap.put(actualLongName, option);
        }

        int usageSize = option.usage.length();
        if(this.maxSizeOfUsage < usageSize) {
            this.maxSizeOfUsage = usageSize;
        }
        if(option.require) {
            this.requireOptionSet.add(option);
        }
        return this;
    }

    /**
     *
     * @param listener
     * not null
     * @return
     */
    public ArgsParser addDefaultAction(OptionListener listener) {
        this.defaultListener = Objects.requireNonNull(listener);
        return this;
    }

    /**
     * parse and invoke action.
     * if args.length is 0, invoke default action.
     * @param args
     * not null
     * @throws IllegalArgumentException
     */

    /**
     * parse and invoke action.
     * if args.length is 0, invoke default action.
     * @param args
     * not null
     * @return
     * rest args. if has no rest, return empty array.
     * @throws IllegalArgumentException
     */
    public String[] parse(final String[] args) throws IllegalArgumentException {
        // parse arguments
        final int size = args.length;

        final Set<Option> foundOptionSet = new HashSet<>();
        int index = 0;
        for(; index < size; index++) {
            String optionSymbol = args[index];
            if(!optionSymbol.startsWith("-")) {
                break;
            }

            Option option = this.optionMap.get(optionSymbol);
            if(option == null) {
                throw new IllegalArgumentException("illegal option: " + optionSymbol);
            }
            Optional<String> arg = Optional.empty();
            if(option.hasArg) {
                if(index + 1 < size && !args[++index].startsWith("-")) {
                    arg = Optional.of(args[index]);
                } else {
                    throw new IllegalArgumentException("require argument: " + optionSymbol);
                }
            }

            // invoke
            option.listener.invoke(arg);
            foundOptionSet.add(option);

            if(option.ignoreRest) {
                index++;
                break;
            }
        }

        // check require option
        for(Option option : this.requireOptionSet) {
            if(!foundOptionSet.contains(option)) {
                throw new IllegalArgumentException("require option: " + option.usage);
            }
        }

        // default action
        if(size == 0) {
            this.defaultListener.invoke(Optional.empty());
        }
        return Arrays.copyOfRange(args, index, size);
    }

    public void printHelp(PrintStream stream) {
        StringBuilder sBuilder = new StringBuilder();
        for(int i = 0; i < this.maxSizeOfUsage; i++) {
            sBuilder.append(' ');
        }
        String spaces = sBuilder.toString();

        // format help message
        sBuilder = new StringBuilder();
        sBuilder.append("Options:");
        sBuilder.append(System.lineSeparator());
        for(Option option : this.optionList) {
            final int size = option.usage.length();
            sBuilder.append("    ");
            sBuilder.append(option.usage);
            for(int i = 0; i < this.maxSizeOfUsage - size; i++) {
                sBuilder.append(' ');
            }
            String[] descs = option.description.split(System.lineSeparator());
            for(int i = 0; i < descs.length; i++) {
                if(i > 0) {
                    sBuilder.append(System.lineSeparator());
                    sBuilder.append(spaces);
                }
                sBuilder.append("    ");
                sBuilder.append(descs[i]);
            }
            sBuilder.append(System.lineSeparator());
        }
        stream.print(sBuilder.toString());
    }

    private static class Option {
        final boolean hasArg;
        final boolean require;
        final boolean ignoreRest;
        final String description;
        final OptionListener listener;
        final String usage;

        /**
         *
         * @param shortName
         * starts with '-'. may be null
         * @param longName
         * starts with '--'. may be null
         * @param flag
         * @param description
         * not null
         * @param listener
         * not null
         */
        Option(String shortName, String longName, int flag,
               String description, OptionListener listener) {
            this.hasArg = hasFlag(flag, HAS_ARG);
            this.require = hasFlag(flag, REQUIRE);
            this.ignoreRest = hasFlag(flag, IGNORE_REST);
            this.listener = listener;
            this.description = description;

            // build usage
            final StringBuilder sBuilder = new StringBuilder();

            if(Objects.nonNull(shortName)) {
                sBuilder.append(shortName);
            }
            if(Objects.nonNull(shortName) && Objects.nonNull(longName)) {
                sBuilder.append(" | ");
            }
            if(Objects.nonNull(longName)) {
                sBuilder.append(longName);
            }
            if(this.hasArg) {
                sBuilder.append(" <arg>");
            }
            this.usage = sBuilder.toString();
        }
    }

    private static boolean hasFlag(int set, int flag) {
        return (set & flag) == flag;
    }
}
