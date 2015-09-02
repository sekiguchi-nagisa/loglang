package loglang.peg;

import loglang.SemanticException;
import loglang.type.LType;
import loglang.type.TypeEnv;

import java.util.*;

import static loglang.peg.ParsingExpression.*;
import static loglang.SemanticException.*;

/**
 * Created by skgchxngsxyz-osx on 15/08/28.
 */
public class ExprTypeChecker implements ExpressionVisitor<LType, Void> {
    private final TypeEnv env;
    private final Map<String, RuleExpr> ruleMap = new HashMap<>();
    private final LabeledExprDetector labeledExprDetector = new LabeledExprDetector();
    private final Set<ParsingExpression> visitedExprSet = new HashSet<>();

    public ExprTypeChecker(TypeEnv env) {
        this.env = Objects.requireNonNull(env);
    }

    public boolean checkType(List<RuleExpr> rules) {   // entry point
        // register rule
        for(RuleExpr ruleExpr : rules) {
            if(Objects.nonNull(this.ruleMap.put(ruleExpr.getRuleName(), ruleExpr))) {
                semanticError("duplicated rule: " + ruleExpr.getRuleName());
            }
        }

        // verify and check type
        LabeledExprVerifier labeledExprVerifier = new LabeledExprVerifier();
        for(RuleExpr ruleExpr : rules) {
            this.visitedExprSet.clear();
            try {
                labeledExprVerifier.visit(ruleExpr);
                this.checkType(ruleExpr);
            } catch(SemanticException e) {
                System.err.println(e.getMessage());
                return false;
            }
        }
        return true;
    }

    public LType checkType(ParsingExpression expr) {
        return this.checkType(null, expr);
    }

    public LType checkType(LType requiredType, ParsingExpression expr) {
        LType type = expr.getType() != null ? expr.getType() : this.visit(expr);
        if(type == null) {
            semanticError("broken visit" + expr.getClass().getSimpleName());
        }

        if(requiredType == null || requiredType.isSameOrBaseOf(type)) {
            return type;
        }

        semanticError("require: " + requiredType + ", but is: " + type);
        return null;
    }

    @Override
    public LType visit(ParsingExpression expr, Void param) {
        if(!this.visitedExprSet.add(Objects.requireNonNull(expr))) {
            semanticError("detect circular reference");
        }
        return expr.accept(this, param);
    }

    @Override
    public LType visitAnyExpr(AnyExpr expr, Void param) {
        return expr.setType(this.env.getVoidType());
    }

    @Override
    public LType visitStringExpr(StringExpr expr, Void param) {
        return expr.setType(this.env.getVoidType());
    }

    @Override
    public LType visitCharClassExpr(CharClassExpr expr, Void param) {
        return expr.setType(this.env.getVoidType());
    }

    @Override
    public LType visitRepeatExpr(RepeatExpr expr, Void param) {
        LType exprType = this.checkType(expr.getExpr());
        return expr.setType(
                exprType.equals(this.env.getVoidType()) ? this.env.getVoidType() : this.env.getArrayType(exprType)
        );
    }

    @Override
    public LType visitOptionalExpr(OptionalExpr expr, Void param) {
        LType exprType = this.checkType(expr.getExpr());
        return expr.setType(
                exprType.equals(this.env.getVoidType()) ? this.env.getVoidType() : this.env.getOptionalType(exprType)
        );
    }

    @Override
    public LType visitPredicateExpr(PredicateExpr expr, Void param) {
        this.checkType(this.env.getVoidType(), expr.getExpr());
        return expr.setType(this.env.getVoidType());
    }

    @Override
    public LType visitSequenceExpr(SequenceExpr expr, Void param) {
        List<LType> types = new ArrayList<>();
        for(ParsingExpression e : expr.getExprs()) {
            LType type = this.checkType(e);
            if(!type.equals(this.env.getVoidType())) {
                types.add(type);
            }
        }
        return expr.setType(
                types.isEmpty() ? this.env.getVoidType() : this.env.getTupleType(types.toArray(new LType[0]))
        );
    }

    @Override
    public LType visitChoiceExpr(ChoiceExpr expr, Void param) {
        List<LType> types = new ArrayList<>();
        for(ParsingExpression e : expr.getExprs()) {
            LType type = this.checkType(e);
            if(!type.equals(this.env.getVoidType())) {
                types.add(type);
            }
        }
        if(!types.isEmpty() && types.size() < expr.getExprs().size()) {
            semanticError("not allow void type");
        }
        return expr.setType(
                types.isEmpty() ? this.env.getVoidType() : this.env.getUnionType(types.toArray(new LType[0]))
        );
    }

    @Override
    public LType visitNonTerminalExpr(NonTerminalExpr expr, Void param) {
        ParsingExpression targetExpr = this.ruleMap.get(expr.getName());
        if(targetExpr == null) {
            semanticError("undefined rule: " + expr.getName());
        }
        return expr.setType(this.checkType(targetExpr));
    }

    @Override
    public LType visitLabeledExpr(LabeledExpr expr, Void param) {
        this.checkType(this.env.getAnyType(), expr.getExpr());
        return expr.setType(this.env.getVoidType());    // actual type is expr.getExpr().getType()
    }

    @Override
    public LType visitRuleExpr(RuleExpr expr, Void param) {
        if(this.labeledExprDetector.visit(expr.getExpr())) {
            semanticError("not need label");
        }
        return expr.setType(this.checkType(expr.getExpr()));
    }

    @Override
    public LType visitTypedRuleExpr(TypedRuleExpr expr, Void param) {
        boolean primary = this.env.isPrimaryType(expr.getTypeName());
        boolean hasLabel = this.labeledExprDetector.visit(expr.getExpr());

        if(primary && !hasLabel) {  // treat as primary type
            LType type = this.env.getBasicType(expr.getTypeName());
            expr.setType(type);

            this.checkType(this.env.getVoidType(), expr.getExpr());
            return type;
        } else if(!primary && hasLabel) {   // treat as structure type
            LType.StructureType type = this.env.newStructureType(expr.getTypeName());
            expr.setType(type);

            this.checkType(this.env.getVoidType(), expr.getExpr());

            // define field
            for(ParsingExpression e : ((SequenceExpr) expr.getExpr()).getExprs()) {
                if(e instanceof LabeledExpr) {
                    this.env.defineField(type, ((LabeledExpr) e).getLabelName(), e.getType());
                }
            }
            return type;
        } else {
            semanticError("illegal type annotation");
            return null;
        }
    }
}
