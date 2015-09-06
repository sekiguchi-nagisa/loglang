package loglang;

import loglang.misc.Utils;
import nez.SourceContext;
import nez.ast.CommonTree;
import nez.lang.Grammar;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/11.
 */
public class Loglang {
    private final String scriptName;
    private final Grammar patternGrammar;

    private CaseContext[] cases;

    public Loglang(String scriptName, Grammar patternGrammar, int caseNum) {
        this.scriptName = scriptName;
        this.patternGrammar = patternGrammar;

        this.cases = new CaseContext[caseNum];

        // dummy: FIXME
        for(int i = 0; i < caseNum; i++) {
            this.cases[i] = (a) -> System.out.println(a.getText());
        }
    }

    public void invoke(String sourceName, String line) {
        this.invoke(SourceContext.newStringContext(Objects.requireNonNull(line)));
    }

    public void invoke(String inputName) {
        try {
            this.invoke(SourceContext.newFileContext(Objects.requireNonNull(inputName)));
        } catch(IOException e) {
            Utils.propagate(e);
        }
    }

    public void invoke(SourceContext inputSource) {
        Objects.requireNonNull(inputSource);
        while(inputSource.hasUnconsumed()) {
            CommonTree result = this.patternGrammar.parseCommonTree(inputSource);
            if(result == null) {
                System.err.println("not match");
                System.exit(1);
            }

            CommonTree tree = result.get(1);
            String tagName = tree.getTag().getName();
            int id = Integer.parseInt(tagName);

            System.out.println("matched: " + tagName);
            this.cases[id].invoke(tree.get(0));
            System.out.println();
        }
    }
}
