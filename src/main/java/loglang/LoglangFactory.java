package loglang;

import loglang.jvm.ByteCodeGenerator;
import loglang.jvm.ByteCodeLoader;
import loglang.misc.Pair;
import loglang.misc.Utils;
import loglang.peg.*;
import loglang.type.TypeEnv;
import nez.NezOption;
import nez.SourceContext;
import nez.ast.CommonTree;
import nez.ast.Source;
import nez.ast.Tag;
import nez.lang.Grammar;
import nez.lang.GrammarFile;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by skgchxngsxyz-osx on 15/08/13.
 */
public class LoglangFactory {
    public final static String grammarFileName = "loglang.nez";

    public Loglang newLoglang(String scriptName) {
        TypeEnv env = new TypeEnv();

        CommonTree scriptTree = this.newScriptTree(scriptName);
        CommonTree patternTree = getAndCheckTag(scriptTree, 0, "PatternDefinition");
        CommonTree prefixTree = getAndCheckTag(scriptTree, 1, "PrefixDefinition");
        CommonTree matcherTree = getAndCheckTag(scriptTree, 2, "Match");

        List<CommonTree> caseTrees = this.getCaseTrees(matcherTree);
        Grammar patternGrammar = this.newPatternGrammar(env, patternTree, prefixTree, caseTrees);

        if(Config.pegOnly) {
            System.out.println("+++ peg only +++");
            System.exit(0);
        }

        Node.RootNode rootNode = (Node.RootNode) new Tree2NodeTranslator().translate(matcherTree);
        try {
            new TypeChecker(env).visit(rootNode);
        } catch(Exception e) {
            reportErrorAndExit(matcherTree.getSource(), e);
        }
        ByteCodeGenerator gen = new ByteCodeGenerator(env.getPackageName());
        ByteCodeLoader loader = new ByteCodeLoader(env.getPackageName());
        for(Node.CaseNode caseNode : rootNode.getCaseNodes()) {
            Pair<String, byte[]> pair = gen.generateCode(caseNode);
            loader.definedAndLoadClass(pair.getLeft(), pair.getRight());
        }

        return new Loglang(patternGrammar, caseTrees.size());
    }

    /**
     * load loglang grammar definition and parse.
     * @param scriptName
     * @return
     */
    private CommonTree newScriptTree(String scriptName) {
        // parse script
        GrammarFile gf = null;
        try {
            gf = GrammarFile.loadNezFile(grammarFileName, NezOption.newDefaultOption());
        } catch(IOException e) {
            System.err.println("cannot load file: loglang.nez");
            System.exit(1);
        }
        Grammar g = gf.newGrammar("File");

        SourceContext src = null;
        try {
            src = SourceContext.newFileContext(scriptName);
        } catch(IOException e) {
            System.err.println("cannot load file: " + scriptName);
            System.exit(1);
        }

        CommonTree tree = g.parseCommonTree(src);
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

    private List<CommonTree> getCaseTrees(CommonTree matcherTree) {
        ArrayList<CommonTree> caseTrees = new ArrayList<>();
        for(CommonTree caseTree : matcherTree) {
            caseTrees.add(caseTree.get(0));
        }
        return caseTrees;
    }

    private List<ParsingExpression.RuleExpr> createRuleExprs(CommonTree patternTree) {
        Tree2ExprTranslator translator = new Tree2ExprTranslator();
        List<ParsingExpression.RuleExpr> ruleExprs = new ArrayList<>();
        for(CommonTree ruleTree : patternTree) {
            ruleExprs.add((ParsingExpression.RuleExpr) translator.translate(ruleTree));
        }
        return ruleExprs;
    }

    /**
     *
     * @param prefixTree
     * @return
     * empty or singleton list
     */
    private List<ParsingExpression.RuleExpr> createPrefixExpr(CommonTree prefixTree) {
        if(prefixTree.isEmpty()) {
            return Collections.emptyList();
        }
        ParsingExpression expr = new Tree2ExprTranslator().translate(prefixTree.get(0));
        String name = TypeEnv.getAnonymousPrefixTypeName();
        return Collections.singletonList(new ParsingExpression.TypedRuleExpr(expr.getRange(), name, name, expr));
    }

    private List<ParsingExpression.RuleExpr> createCaseExprs(List<CommonTree> caseTrees) {
        Tree2ExprTranslator translator = new Tree2ExprTranslator();
        List<ParsingExpression.RuleExpr> casePatterns = new ArrayList<>();
        for(int i = 0; i < caseTrees.size(); i++) {
            String name = TypeEnv.createAnonymousCaseTypeName(i);
            ParsingExpression expr = translator.translate(caseTrees.get(i));
            casePatterns.add(new ParsingExpression.TypedRuleExpr(expr.getRange(), name, name, expr));
        }
        return casePatterns;
    }

    private void dumpPattern(List<ParsingExpression.RuleExpr> ruleExprs,
                             List<ParsingExpression.RuleExpr> prefixExpr,
                             List<ParsingExpression.RuleExpr> caseExprs) {
        PrettyPrinter printer = new PrettyPrinter();

        System.err.println("@@ dump Rule @@");
        ruleExprs.stream().forEach((t) -> printer.printPEG(System.err, t));

        System.err.println("@@ dump Prefix Pattern @@");
        prefixExpr.stream().forEach((t) -> printer.printPEG(System.err, t));

        System.err.println("@@ dump Case Pattern @@");
        caseExprs.stream().forEach((t) -> printer.printPEG(System.err, t));

        System.err.println();
    }

    /**
     * convert to Nez grammar.
     * @param env
     * @param patternTree
     * @param prefixTree
     * @param caseTrees
     * @return
     */
    private Grammar newPatternGrammar(TypeEnv env, CommonTree patternTree,
                                      CommonTree prefixTree, List<CommonTree> caseTrees) {
        List<ParsingExpression.RuleExpr> ruleExprs = this.createRuleExprs(patternTree);
        List<ParsingExpression.RuleExpr> prefixExpr = this.createPrefixExpr(prefixTree);
        List<ParsingExpression.RuleExpr> caseExprs = this.createCaseExprs(caseTrees);

        if(Config.dumpPEG) {
            System.err.println("++++ dump PEG ++++");
            this.dumpPattern(ruleExprs, prefixExpr, caseExprs);
        }

        // check type
        try {
            ExprTypeChecker checker = new ExprTypeChecker(env);

            checker.checkType(ruleExprs);
            checker.checkType(prefixExpr);
            checker.checkType(caseExprs);
        } catch(Exception e) {
            reportErrorAndExit(patternTree.getSource(), e);
        }

        if(Config.dumpTypedPEG) {
            System.err.println("++++ dump typed PEG ++++");
            this.dumpPattern(ruleExprs, prefixExpr, caseExprs);
        }

        try {
            Path path = Files.createTempFile("ll_pattern", ".nez");
            try(PrintStream stream = new PrintStream(path.toFile())) {
                new NezGrammarGenerator(stream).generate(ruleExprs, prefixExpr, caseExprs);
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

            return GrammarFile.loadGrammarFile(pathName, NezOption.newDefaultOption()).newGrammar("File");
        } catch(IOException e) {
            Utils.propagate(e);
        }
        return null;
    }

    private static CommonTree getAndCheckTag(CommonTree tree, int index, String tagName) {
        CommonTree child = tree.get(index);
        assert child.is(Tag.tag(tagName));
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
