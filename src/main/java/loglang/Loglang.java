package loglang;

import loglang.misc.Utils;
import nez.Grammar;
import nez.io.SourceContext;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/11.
 */
public class Loglang {
    private final Grammar patternGrammar;

    private CaseContext[] cases;

    Loglang(Grammar patternGrammar, int caseNum) {
        this.patternGrammar = patternGrammar;

        this.cases = new CaseContext[caseNum];

        // dummy: FIXME
        for(int i = 0; i < caseNum; i++) {
            this.cases[i] = (p, a) -> {
                if(p != null) {
                    System.out.print(p.toText());
                    System.out.print(" ");
                }
                System.out.println(a.toText());
            };
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

    public void invoke(SourceContext inputSource) { //FIXME
//        Objects.requireNonNull(inputSource);
//        while(inputSource.hasUnconsumed()) {
//            Tree<?> result = this.patternGrammar.parseCommonTree(inputSource);
//            if(result == null) {
//                System.err.println("not match");
//                System.exit(1);
//            }
//
//            Tree<?> prefixTreeWrapper = result.get(0);
//            Tree<?> caseTreeWrapper = result.get(1);
//
//            String tagName = caseTreeWrapper.getTag().getSymbol();
//            int id = Integer.parseInt(tagName);
//
//            System.out.println("matched: " + tagName);
//            this.cases[id].invoke(
//                    prefixTreeWrapper.isEmpty() ? null : prefixTreeWrapper.get(0),
//                    caseTreeWrapper.get(0)
//            );
//
//            System.out.println();
//        }
    }
}
