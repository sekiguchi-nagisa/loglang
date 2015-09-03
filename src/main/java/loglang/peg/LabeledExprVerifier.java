package loglang.peg;

import loglang.SemanticException;
import loglang.misc.Utils;

import java.util.ArrayList;

/**
 * Created by skgchxngsxyz-opensuse on 15/09/03.
 */
public class LabeledExprVerifier extends BaseVisitor<Void, Void> {
    private final ArrayList<ParsingExpression> exprStack = new ArrayList<>();

    @Override
    public Void visit(ParsingExpression expr, Void param) {
        Utils.push(this.exprStack, expr);
        expr.accept(this, param);
        Utils.pop(this.exprStack);
        return null;
    }

    @Override
    public Void visitDefault(ParsingExpression expr, Void param) {
        return null;
    }

    @Override
    public Void visitRepeatExpr(ParsingExpression.RepeatExpr expr, Void param) {
        return this.visit(expr.getExpr());
    }

    @Override
    public Void visitOptionalExpr(ParsingExpression.OptionalExpr expr, Void param) {
        return this.visit(expr.getExpr());
    }

    @Override
    public Void visitPredicateExpr(ParsingExpression.PredicateExpr expr, Void param) {
        return this.visit(expr.getExpr());
    }

    @Override
    public Void visitChoiceExpr(ParsingExpression.ChoiceExpr expr, Void param) {
        for(ParsingExpression e : expr.getExprs()) {
            this.visit(e);
        }
        return null;
    }

    @Override
    public Void visitSequenceExpr(ParsingExpression.SequenceExpr expr, Void param) {
        for(ParsingExpression e : expr.getExprs()) {
            this.visit(e);
        }
        return null;
    }

    @Override
    public Void visitRuleExpr(ParsingExpression.RuleExpr expr, Void param) {
        return this.visit(expr.getExpr());
    }

    @Override
    public Void visitTypedRuleExpr(ParsingExpression.TypedRuleExpr expr, Void param) {
        return this.visit(expr.getExpr());
    }

    @Override
    public Void visitLabeledExpr(ParsingExpression.LabeledExpr expr, Void param) {
        if(this.exprStack.size() == 3
                && this.exprStack.get(0) instanceof ParsingExpression.RuleExpr
                && this.exprStack.get(1) instanceof ParsingExpression.SequenceExpr) {
            return this.visit(expr.getExpr());
        }
        this.exprStack.clear();
        throw new SemanticException(expr.getRange(), "not allowed label");
    }
}
