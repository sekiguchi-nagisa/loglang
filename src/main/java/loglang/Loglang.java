package loglang;

import nez.SourceContext;
import nez.lang.Grammar;

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

    public void invoke(SourceContext inputSource) {

    }
}
