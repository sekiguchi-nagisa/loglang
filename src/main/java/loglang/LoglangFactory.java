package loglang;

import loglang.jvm.ByteCodeGenerator;
import loglang.jvm.ByteCodeLoader;
import loglang.misc.Pair;
import loglang.misc.Utils;
import nez.Grammar;
import nez.Parser;
import nez.Strategy;
import nez.ast.Source;
import nez.ast.Symbol;
import nez.ast.Tree;
import nez.io.SourceContext;
import nez.lang.GrammarFileLoader;
import nez.peg.tpeg.SemanticException;
import nez.peg.tpeg.type.TypeEnv;

import java.io.IOException;
import java.io.PrintStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by skgchxngsxyz-osx on 15/08/13.
 */
public class LoglangFactory {
    public final static String grammarFileName = "loglang.nez";

    public Loglang newLoglang(String scriptName) {
        Tree<?> scriptTree = this.newScriptTree(scriptName);
        Tree<?> patternTree = getAndCheckTag(scriptTree, 0, "PatternDefinition");
        Tree<?> prefixTree = getAndCheckTag(scriptTree, 1, "PrefixDefinition");
        Tree<?> matcherTree = getAndCheckTag(scriptTree, 2, "Match");

        List<Tree<?>> caseTrees = this.getCaseTrees(matcherTree);
        Grammar patternGrammar = this.newPatternGrammar(patternTree, prefixTree, caseTrees);

        if(Config.pegOnly) {
            System.out.println("+++ peg only +++");
            System.exit(0);
        }

        Node.RootNode rootNode = (Node.RootNode) new Tree2NodeTranslator().translate(matcherTree);
        try {
            new TypeChecker(TypeEnv.getInstance()).visit(rootNode);
        } catch(Exception e) {
            reportErrorAndExit(matcherTree.getSource(), e);
        }

        ByteCodeGenerator gen = new ByteCodeGenerator(TypeEnv.getInstance().getPackageName());
        ByteCodeLoader loader = new ByteCodeLoader(TypeEnv.getInstance().getPackageName());

        final int caseSize = rootNode.getCaseNodes().size();
        CaseContext[] cases = new CaseContext[caseSize];

        for(int i = 0; i < caseSize; i++) {
            Pair<String, byte[]> pair = gen.generateCode(rootNode.getCaseNodes().get(i));
            Class<?> clazz = loader.definedAndLoadClass(pair.getLeft(), pair.getRight());
            cases[i] = newInstance(clazz, CaseContext.class);
        }

        return new Loglang(patternGrammar, cases);
    }

    @SuppressWarnings("unchecked")
    private static <T> T newInstance(Class<?> owner, Class<T> superClass) {
        try {
            return (T) owner.newInstance();
        } catch(InstantiationException | IllegalAccessException e) {
            Utils.propagate(e);
            return null;
        }
    }

    /**
     * load loglang grammar definition and parse.
     * @param scriptName
     * @return
     */
    private Tree<?> newScriptTree(String scriptName) {
        // parse script
        Grammar g = null;
        try {
            g = GrammarFileLoader.loadGrammar(grammarFileName, Strategy.newDefaultStrategy());
        } catch(IOException e) {
            System.err.println("cannot load file: loglang.nez");
            System.exit(1);
        }
        Parser parser = g.newParser("File");

        SourceContext src = null;
        try {
            src = SourceContext.newFileContext(scriptName);
        } catch(IOException e) {
            System.err.println("cannot load file: " + scriptName);
            System.exit(1);
        }

        Tree<?> tree = parser.parseCommonTree(src);
        if(tree == null) {
            System.err.println(src.getSyntaxErrorMessage());
            System.exit(1);
        }
        if(src.hasUnconsumed()) {
            System.err.println(src.getUnconsumedMessage());
            System.exit(1);
        }
        return tree;
    }

    private List<Tree<?>> getCaseTrees(Tree<?> matcherTree) {
        ArrayList<Tree<?>> caseTrees = new ArrayList<>();
        for(Tree<?> caseTree : matcherTree) {
            caseTrees.add(caseTree.get(0));
        }
        return caseTrees;
    }

    private void printTypedPEGPattern(Tree<?> patternTree, Tree<?> prefixTree, List<Tree<?>> caseTrees,
                                      PrintStream stream) {
        TreePrinter printer = TreePrinter.newPrinter(stream);

        // create top level rule
        stream.print("File : __File__ = ");
        if(!prefixTree.isEmpty()) {
            stream.print("$Prefix : ");
            stream.print(TypeEnv.getAnonymousPrefixTypeName());
            stream.print(" ");
        }
        stream.print("$ResultAST : (");
        final int caseSize = caseTrees.size();
        for(int i = 0; i < caseSize; i++) {
            if(i > 0) {
                stream.print(" / ");
            }
            stream.print(TypeEnv.createAnonymousCaseTypeName(i));
        }
        stream.println(")");

        // create prefix rule
        if(!prefixTree.isEmpty()) {
            stream.print(TypeEnv.getAnonymousPrefixTypeName());
            stream.print(" : ");
            stream.print(TypeEnv.getAnonymousPrefixTypeName());
            stream.print(" = ");
            printer.print(prefixTree.get(0));
            stream.println();
        }

        // create case rule
        for(int i = 0; i < caseSize; i++) {
            stream.print(TypeEnv.createAnonymousCaseTypeName(i));
            stream.print(" : ");
            stream.print(TypeEnv.createAnonymousCaseTypeName(i));
            stream.print(" = ");
            printer.print(caseTrees.get(i));
            stream.println();
        }

        // create sub pattern
        patternTree.forEach(printer::print);
    }

    /**
     * convert to Typed PEG file.
     * @param patternTree
     * @param prefixTree
     * @param caseTrees
     * @return
     */
    private Grammar newPatternGrammar(Tree<?> patternTree, Tree<?> prefixTree, List<Tree<?>> caseTrees) {
        try {
            Path path = Files.createTempFile("ll_pattern", ".tpeg");
            try(PrintStream stream = new PrintStream(path.toFile())) {
                printTypedPEGPattern(patternTree, prefixTree, caseTrees, stream);
            } catch(Exception e) {
                Utils.propagate(e);
            }

            String pathName = path.toString();

            // delete pattern file before shutdown
            if(Config.dumpPattern) {
                System.err.println("@@@@ Dump Pattern File: " + pathName + " @@@@");
            } else {
                Runtime.getRuntime().addShutdownHook(
                        new Thread(() -> new File(pathName).delete())
                );
            }
            return GrammarFileLoader.loadGrammar(pathName, Strategy.newDefaultStrategy());
        } catch(IOException e) {
            Utils.propagate(e);
        }
        return null;
    }

    private static Tree<?> getAndCheckTag(Tree<?> tree, int index, String tagName) {
        Tree<?> child = tree.get(index);
        assert child.is(Symbol.tag(tagName));
        return child;
    }

    private static void reportErrorAndExit(Source source, Exception e) {
        Throwable cause = e;
        while(!(cause instanceof SemanticException)) {
            if(cause.getCause() == null) {
                break;
            }
            cause = cause.getCause();
        }

        if(cause instanceof SemanticException) {
            System.err.println(source.formatPositionLine(
                    "semantic error", ((SemanticException) cause).getRange().pos, e.getMessage()));
            System.exit(1);
        } else {
            Utils.propagate(e);
        }
    }
}
