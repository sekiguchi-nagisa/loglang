package loglang.peg;

import loglang.type.LType;
import loglang.type.TypeEnv;

import java.util.Objects;

import static loglang.peg.ParsingExpression.*;
import static loglang.SemanticException.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/28.
 */
public class ExprTypeChecker implements ExpressionVisitor<Void, Void> {
    private final TypeEnv env;

    public ExprTypeChecker(TypeEnv env) {
        this.env = Objects.requireNonNull(env);
    }

    public LType checkType(ParsingExpression expr) {
        return this.checkType(null, expr);
    }

    public LType checkType(LType requiredType, ParsingExpression expr) {
        LType type = expr.getType();
        if(type == null) {
            this.visit(expr);
        }

        type = expr.getType();
        if(type == null) {
            return null;
        }

        if(requiredType == null || requiredType.isSameOrBaseOf(type)) {
            return type;
        }

        semanticError("require: " + requiredType + ", but is: " + type);
        return null;
    }

    @Override
    public Void visitAnyExpr(AnyExpr expr, Void param) {
        expr.setType(this.env.getVoidType());
        return null;
    }

    @Override
    public Void visitStringExpr(StringExpr expr, Void param) {
        expr.setType(this.env.getVoidType());
        return null;
    }

    @Override
    public Void visitCharClassExpr(CharClassExpr expr, Void param) {
        expr.setType(this.env.getVoidType());
        return null;
    }

    @Override
    public Void visitRepeatExpr(RepeatExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitOptionalExpr(OptionalExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitPredicateExpr(PredicateExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitSequenceExpr(SequenceExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitChoiceExpr(ChoiceExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitNonTerminalExpr(NonTerminalExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitLabeledExpr(LabeledExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitRuleExpr(RuleExpr expr, Void param) {
        return null;
    }

    @Override
    public Void visitTypedRuleExpr(TypedRuleExpr expr, Void param) {
        return null;
    }
}
