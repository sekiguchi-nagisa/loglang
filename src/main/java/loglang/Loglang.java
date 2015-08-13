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

    public Loglang(String scriptName, Grammar patternGrammar) {
        this.scriptName = scriptName;
        this.patternGrammar = patternGrammar;
    }

    public void invoke(String sourceName, String line) {
        this.invoke(SourceContext.newStringContext(line));
    }

    public void invoke(String inputName) {
        try {
            this.invoke(SourceContext.newFileContext(Objects.requireNonNull(inputName)));
        } catch(IOException e) {
            Utils.propagate(e);
        }
    }

    public void invoke(SourceContext inputSource) {
        while(inputSource.hasUnconsumed()) {
            CommonTree result = this.patternGrammar.parseCommonTree(inputSource);
            if(result == null) {
                System.err.println("not match");
                System.exit(1);
            }
            String tagName = result.get(0).getTag().getName();
            System.out.println("matched: " + tagName);
            System.out.println(result.get(0).getText());
            System.out.println();
        }
    }
}
