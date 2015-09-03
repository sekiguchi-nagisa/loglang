package loglang.peg;

import loglang.TreeTranslator;

import static loglang.peg.ParsingExpression.*;
import static nez.ast.ASTHelper.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/28.
 */
public class Tree2ExprTranslator extends TreeTranslator<ParsingExpression> {{
    this.add("RuleExpr", (t) -> {
        String typeName = t.get(1).getText();
        if(typeName.equals("")) {   // untyped
            return new RuleExpr(range(t), t.get(0).getText(), this.translate(t.get(2)));
        } else {
            return new TypedRuleExpr(range(t), t.get(0).getText(), typeName, this.translate(t.get(2)));
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
                    range(t), t.get(0).getText(), this.translate(t.get(1))
            )
    );

    this.add("AndExpr", (t) -> PredicateExpr.andPredicate(range(t), this.translate(t.get(0))));

    this.add("NotExpr", (t) -> PredicateExpr.notPredicate(range(t), this.translate(t.get(0))));

    this.add("ZeroMoreExpr", (t) -> RepeatExpr.zeroMore(range(t), this.translate(t.get(0))));

    this.add("OneMoreExpr", (t) -> RepeatExpr.oneMore(range(t), this.translate(t.get(0))));

    this.add("OptionalExpr", (t) -> new OptionalExpr(range(t), this.translate(t.get(0))));

    this.add("NonTerminalExpr", (t) -> new NonTerminalExpr(range(t), t.getText()));

    this.add("AnyExpr", (t) -> new AnyExpr(range(t)));

    this.add("StringExpr", (t) -> new StringExpr(range(t), t.getText()));

    this.add("CharClassExpr", (t) -> new CharClassExpr(range(t), t.getText()));
}}
