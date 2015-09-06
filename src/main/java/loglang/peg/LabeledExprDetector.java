package loglang.peg;

import static loglang.peg.ParsingExpression.*;

/**
 * Created by skgchxngsxyz-opensuse on 15/09/02.
 */
public class LabeledExprDetector extends BaseVisitor<Boolean, Void> {

    @Override
    public Boolean visitDefault(ParsingExpression expr, Void param) {
        return false;
    }

    @Override
    public Boolean visitSequenceExpr(SequenceExpr expr, Void param) {
        for(ParsingExpression e : expr.getExprs()) {
            if(this.visit(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visitLabeledExpr(LabeledExpr expr, Void param) {
        return true;
    }

    @Override
    public Boolean visitPrefixExpr(PrefixExpr expr, Void param) {
        for(ParsingExpression e : expr.getExprs()) {
            if(this.visit(e)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visitCaseExpr(CaseExpr expr, Void param) {
        for(ParsingExpression e : expr.getExprs()) {
            if(this.visit(e)) {
                return true;
            }
        }
        return false;
    }
}
