package loglang.peg;

import static loglang.peg.TypedPEG.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/28.
 */
public interface ExpressionVisitor<T, P> {
    default T visit(TypedPEG expr) {
        return this.visit(expr, null);
    }

    default T visit(TypedPEG expr, P param) {
        return expr.accept(this, param);
    }

    T visitAnyExpr(AnyExpr expr, P param);
    T visitStringExpr(StringExpr expr, P param);
    T visitCharClassExpr(CharClassExpr expr, P param);
    T visitRepeatExpr(RepeatExpr expr, P param);
    T visitOptionalExpr(OptionalExpr expr, P param);
    T visitPredicateExpr(PredicateExpr expr, P param);
    T visitSequenceExpr(SequenceExpr expr, P param);
    T visitChoiceExpr(ChoiceExpr expr, P param);
    T visitNonTerminalExpr(NonTerminalExpr expr, P param);
    T visitLabeledExpr(LabeledExpr expr, P param);
    T visitRuleExpr(RuleExpr expr, P param);
    T visitTypedRuleExpr(TypedRuleExpr expr, P param);
    T visitRootExpr(RootExpr expr, P param);
}
