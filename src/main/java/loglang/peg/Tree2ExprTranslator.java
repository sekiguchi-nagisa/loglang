package loglang.peg;

import loglang.TreeTranslator;

import static loglang.peg.ParsingExpression.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/28.
 */
public class Tree2ExprTranslator extends TreeTranslator<ParsingExpression> {{
    this.add("RuleExpr", (t) -> {
        String typeName = t.get(1).getText();
        if(typeName.equals("")) {   // untyped
            return new RuleExpr(t.get(0).getText(), this.translate(t.get(2)));
        } else {
            return new TypedRuleExpr(t.get(0).getText(), typeName, this.translate(t.get(2)));
        }
    });

    this.add("ChoiceExpr", (t) ->
            new ChoiceExpr(
                    this.translate(t.get(0)), this.translate(t.get(1))
            )
    );

    this.add("SequenceExpr", (t) ->
            new SequenceExpr(
                    this.translate(t.get(0)), this.translate(t.get(1))
            )
    );

    this.add("LabeledExpr", (t) ->
            new LabeledExpr(
                    t.get(0).getText(), this.translate(t.get(1))
            )
    );

    this.add("AndExpr", (t) -> PredicateExpr.andPredicate(this.translate(t.get(0))));

    this.add("NotExpr", (t) -> PredicateExpr.notPredicate(this.translate(t.get(0))));

    this.add("ZeroMoreExpr", (t) -> RepeatExpr.zeroMore(this.translate(t.get(0))));

    this.add("OneMoreExpr", (t) -> RepeatExpr.oneMore(this.translate(t.get(0))));

    this.add("OptionalExpr", (t) -> new OptionalExpr(this.translate(t.get(0))));

    this.add("NonTerminalExpr", (t) -> new NonTerminalExpr(t.getText()));

    this.add("AnyExpr", (t) -> new AnyExpr());

    this.add("StringExpr", (t) -> new StringExpr(t.getText()));

    this.add("CharClassExpr", (t) -> new CharClassExpr(t.getText()));
}}
