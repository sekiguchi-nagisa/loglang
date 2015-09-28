package loglang;

import loglang.jvm.ByteCodeGenerator;
import loglang.jvm.ByteCodeLoader;
import loglang.misc.Pair;
import loglang.misc.Utils;
import nez.Grammar;
import nez.ast.Source;
import nez.ast.Tree;
import nez.peg.tpeg.SemanticException;
import nez.peg.tpeg.TypedPEG;
import nez.peg.tpeg.type.TypeEnv;

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

        Tree<?> scriptTree = this.newScriptTree(scriptName);
        Tree<?> patternTree = getAndCheckTag(scriptTree, 0, "PatternDefinition");
        Tree<?> prefixTree = getAndCheckTag(scriptTree, 1, "PrefixDefinition");
        Tree<?> matcherTree = getAndCheckTag(scriptTree, 2, "Match");

        List<Tree<?>> caseTrees = this.getCaseTrees(matcherTree);
//        Grammar patternGrammar = this.newPatternGrammar(env, patternTree, prefixTree, caseTrees);

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

//        return new Loglang(patternGrammar, caseTrees.size());
        return null;
    }

    /**
     * load loglang grammar definition and parse.
     * @param scriptName
     * @return
     */
    private Tree<?> newScriptTree(String scriptName) {   //FIXME:
//        // parse script
//        GrammarFile gf = null;
//        try {
//            gf = GrammarFile.loadNezFile(grammarFileName, NezOption.newDefaultOption());
//        } catch(IOException e) {
//            System.err.println("cannot load file: loglang.nez");
//            System.exit(1);
//        }
//        Grammar g = gf.newGrammar("File");
//
//        SourceContext src = null;
//        try {
//            src = SourceContext.newFileContext(scriptName);
//        } catch(IOException e) {
//            System.err.println("cannot load file: " + scriptName);
//            System.exit(1);
//        }
//
//        CommonTree tree = g.parseCommonTree(src);
//        if(tree == null) {
//            System.err.println(src.getSyntaxErrorMessage());
//            System.exit(1);
//        }
//        if(src.hasUnconsumed()) {
//            System.err.println(src.getUnconsumedMessage());
//            System.exit(1);
//        }
//        return tree;

        return null;
    }

    private List<Tree<?>> getCaseTrees(Tree<?> matcherTree) {
        ArrayList<Tree<?>> caseTrees = new ArrayList<>();
        for(Tree<?> caseTree : matcherTree) {
            caseTrees.add(caseTree.get(0));
        }
        return caseTrees;
    }

    private List<TypedPEG.RuleExpr> createRuleExprs(Tree<?> patternTree) {  //FIXME:
//        Tree2ExprTranslator translator = new Tree2ExprTranslator();
//        List<TypedPEG.RuleExpr> ruleExprs = new ArrayList<>();
//        for(Tree<?> ruleTree : patternTree) {
//            ruleExprs.add((TypedPEG.RuleExpr) translator.translate(ruleTree));
//        }
//        return ruleExprs;

        return null;
    }

    /**
     *
     * @param prefixTree
     * @return
     * empty or singleton list
     */
    private List<TypedPEG.RuleExpr> createPrefixExpr(Tree<?> prefixTree) {  //FIXME:
        if(prefixTree.isEmpty()) {
            return Collections.emptyList();
        }
//        TypedPEG expr = new Tree2ExprTranslator().translate(prefixTree.get(0));
//        String name = TypeEnv.getAnonymousPrefixTypeName();
//        return Collections.singletonList(new TypedPEG.TypedRuleExpr(expr.getRange(), name, name, expr));
        return null;
    }

    private List<TypedPEG.RuleExpr> createCaseExprs(List<Tree<?>> caseTrees) {  //FIXME:
//        Tree2ExprTranslator translator = new Tree2ExprTranslator();
//        List<TypedPEG.RuleExpr> casePatterns = new ArrayList<>();
//        for(int i = 0; i < caseTrees.size(); i++) {
//            String name = TypeEnv.createAnonymousCaseTypeName(i);
//            TypedPEG expr = translator.translate(caseTrees.get(i));
//            casePatterns.add(new TypedPEG.TypedRuleExpr(expr.getRange(), name, name, expr));
//        }
//        return casePatterns;

        return null;
    }

    private void dumpPattern(List<TypedPEG.RuleExpr> ruleExprs,
                             List<TypedPEG.RuleExpr> prefixExpr,
                             List<TypedPEG.RuleExpr> caseExprs) {
        TypedPEGPrettyPrinter printer = new TypedPEGPrettyPrinter();

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
    private Grammar newPatternGrammar(TypeEnv env, Tree<?> patternTree,
                                      Tree<?> prefixTree, List<Tree<?>> caseTrees) {
//        List<TypedPEG.RuleExpr> ruleExprs = this.createRuleExprs(patternTree);
//        List<TypedPEG.RuleExpr> prefixExpr = this.createPrefixExpr(prefixTree);
//        List<TypedPEG.RuleExpr> caseExprs = this.createCaseExprs(caseTrees);
//
//        if(Config.dumpPEG) {
//            System.err.println("++++ dump PEG ++++");
//            this.dumpPattern(ruleExprs, prefixExpr, caseExprs);
//        }
//
//        // check type
//        try {
//            ExprTypeChecker checker = new ExprTypeChecker(env);
//
//            checker.checkType(ruleExprs);
//            checker.checkType(prefixExpr);
//            checker.checkType(caseExprs);
//        } catch(Exception e) {
//            reportErrorAndExit(patternTree.getSource(), e);
//        }
//
//        if(Config.dumpTypedPEG) {
//            System.err.println("++++ dump typed PEG ++++");
//            this.dumpPattern(ruleExprs, prefixExpr, caseExprs);
//        }
//
//        try {
//            Path path = Files.createTempFile("ll_pattern", ".nez");
//            try(PrintStream stream = new PrintStream(path.toFile())) {
//                new NezGrammarGenerator(stream).generate(ruleExprs, prefixExpr, caseExprs);
//            } catch(Exception e) {
//                Utils.propagate(e);
//            }
//
//            String pathName = path.toString();
//
//            // delete pattern file before shutdown
//            if(Config.dumpPattern) {
//                System.err.println("@@@@ Dump Pattern File: " + pathName + " @@@@");
//            } else {
//                Runtime.getRuntime().addShutdownHook(
//                        new Thread(() -> new File(pathName).delete())
//                );
//            }

//            return GrammarFile.loadGrammarFile(pathName, NezOption.newDefaultOption()).newGrammar("File");    //FIXME;
//        } catch(IOException e) {
//            Utils.propagate(e);
//        }
        return null;
    }

    private static Tree<?> getAndCheckTag(Tree<?> tree, int index, String tagName) {
        Tree<?> child = tree.get(index);
//        assert child.is(Tag.tag(tagName));    //FIXME:
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
