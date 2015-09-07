package loglang.peg;

/**
 * Created by skgchxngsxyz-opensuse on 15/09/02.
 */
public abstract class BaseVisitor<R, P> implements ExpressionVisitor<R, P> {
    public abstract R visitDefault(ParsingExpression expr, P param);


    @Override
    public R visitAnyExpr(ParsingExpression.AnyExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitStringExpr(ParsingExpression.StringExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitCharClassExpr(ParsingExpression.CharClassExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitRepeatExpr(ParsingExpression.RepeatExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitOptionalExpr(ParsingExpression.OptionalExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitPredicateExpr(ParsingExpression.PredicateExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitSequenceExpr(ParsingExpression.SequenceExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitChoiceExpr(ParsingExpression.ChoiceExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitNonTerminalExpr(ParsingExpression.NonTerminalExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitLabeledExpr(ParsingExpression.LabeledExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitRuleExpr(ParsingExpression.RuleExpr expr, P param) {
        return this.visitDefault(expr, param);
    }

    @Override
    public R visitTypedRuleExpr(ParsingExpression.TypedRuleExpr expr, P param) {
        return this.visitDefault(expr, param);
    }
}
