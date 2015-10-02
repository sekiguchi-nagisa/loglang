package loglang;

import loglang.misc.Utils;
import nez.Grammar;
import nez.Parser;
import nez.ast.Tree;
import nez.io.SourceContext;
import nez.peg.tpeg.type.TypeEnv;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/11.
 */
public class Loglang {
    private final Parser patternParser;

    private CaseContext[] cases;

    Loglang(Grammar patternGrammar, int caseNum) {
        this.patternParser = patternGrammar.newParser("File");

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

    private static int parseCaseTag(String tagName) {
        // skip mangled name prefix
        int index = 0;
        final int size = tagName.length();
        for(; index < size; index++) {
            if(tagName.charAt(index) == '_') {
                break;
            }
        }

        // skip case type name prefix
        index += TypeEnv.getAnonymousCaseTypeNamePrefix().length();
        String id = tagName.substring(index, size - 2);
        return Integer.parseInt(id);
    }

    public void invoke(SourceContext inputSource) {
        Objects.requireNonNull(inputSource);
        while(inputSource.hasUnconsumed()) {
            Tree<?> result = this.patternParser.parseCommonTree(inputSource);
            if(result == null) {
                System.err.println("not match");
                System.exit(1);
            }

            Tree<?> prefixTreeWrapper = null;
            Tree<?> caseTreeWrapper = null;
            if(result.size() == 1) {
                caseTreeWrapper = result.get(0);
            } else if(result.size() == 2) {
                prefixTreeWrapper = result.get(0);
                caseTreeWrapper = result.get(1);
            }
            caseTreeWrapper = caseTreeWrapper.get(0);
            
            String tagName = caseTreeWrapper.getTag().getSymbol();
            int id = parseCaseTag(tagName);

            System.out.println("matched: " + tagName);
            this.cases[id].invoke(prefixTreeWrapper, caseTreeWrapper);

            System.out.println();
        }
    }
}
