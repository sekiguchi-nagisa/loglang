package loglang;

import loglang.jvm.ByteCodeGenerator;
import loglang.jvm.ByteCodeLoader;
import loglang.misc.Pair;
import loglang.misc.Utils;
import nez.NezOption;
import nez.SourceContext;
import nez.ast.CommonTree;
import nez.ast.Tag;
import nez.lang.Grammar;
import nez.lang.GrammarFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
        CommonTree scriptTree = this.newScriptTree(scriptName);
        CommonTree patternTree = getAndCheckTag(scriptTree, 0, "PatternDefinition");
        CommonTree matcherTree = getAndCheckTag(scriptTree, 1, "Match");

        List<String> casePatterns = this.getCasePatterns(matcherTree);
        Grammar patternGrammar = this.newPatternGrammar(patternTree, casePatterns);

        Node.RootNode rootNode = (Node.RootNode) TreeTranslator.create().translate(matcherTree);
        new TypeChecker().visit(rootNode);
        ByteCodeGenerator gen = new ByteCodeGenerator();
        ByteCodeLoader loader = new ByteCodeLoader(gen.getPackageName());
        for(Node.CaseNode caseNode : rootNode.getCaseNodes()) {
            Pair<String, byte[]> pair = gen.generateCode(caseNode);
            loader.definedAndLoadClass(pair.getLeft(), pair.getRight());
        }

        return new Loglang(scriptName, patternGrammar, casePatterns.size());
    }

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

    private List<String> getCasePatterns(CommonTree matcherTree) {
        ArrayList<String> casePatterns = new ArrayList<>();
        int size = matcherTree.size();
        for(int i = 0; i < size; i++) {
            CommonTree caseTree = matcherTree.get(i);
            casePatterns.add(caseTree.get(0).getText());
        }
        return casePatterns;
    }

    private Grammar newPatternGrammar(CommonTree patternTree, List<String> casePatterns) {
        try {
            Path path = Files.createTempFile("ll_pattern", ".nez");
            try(BufferedWriter bw = Files.newBufferedWriter(path, Charset.forName("UTF8"))) {
                bw.write(patternTree.getText());
                bw.write(System.lineSeparator());
                bw.write("File = { ");

                int size = casePatterns.size();
                for(int i = 0; i< size; i++) {
                    if(i > 0) {
                        bw.write(" / ");
                    }
                    bw.write("@{ @");
                    bw.write(casePatterns.get(i));
                    bw.write(" #");
                    bw.write(Integer.toString(i));
                    bw.write(" }");
                }

                bw.write(" #ResultAST }");
                bw.write(System.lineSeparator());
                bw.flush();
                bw.close();
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
}
