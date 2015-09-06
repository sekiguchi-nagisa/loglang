package loglang.peg;

import loglang.SemanticException;
import loglang.TypeException;
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
    private final LabeledExprVerifier labeledExprVerifier = new LabeledExprVerifier();
    private final LabeledExprDetector labeledExprDetector = new LabeledExprDetector();
    private final Set<ParsingExpression> visitedExprSet = new HashSet<>();

    public ExprTypeChecker(TypeEnv env) {
        this.env = Objects.requireNonNull(env);
    }

    public void checkType(List<RuleExpr> rules) {   // entry point
        // register rule
        for(RuleExpr ruleExpr : rules) {
            if(Objects.nonNull(this.ruleMap.put(ruleExpr.getRuleName(), ruleExpr))) {
                semanticError(ruleExpr.getRange(), "duplicated rule: " + ruleExpr.getRuleName());
            }
        }

        // verify and check type
        for(RuleExpr ruleExpr : rules) {
            this.visitedExprSet.clear();
            this. labeledExprVerifier.visit(ruleExpr);
            this.checkType(ruleExpr);
        }
    }

    public LType checkType(ParsingExpression expr) {
        return this.checkType(null, expr);
    }

    public LType checkType(LType requiredType, ParsingExpression expr) {
        LType type = expr.getType() != null ? expr.getType() : this.visit(expr);
        if(type == null) {
            semanticError(expr.getRange(), "broken visit" + expr.getClass().getSimpleName());
        }

        if(requiredType == null || requiredType.isSameOrBaseOf(type)) {
            return type;
        }

        semanticError(expr.getRange(), "require: " + requiredType + ", but is: " + type);
        return null;
    }

    @Override
    public LType visit(ParsingExpression expr, Void param) {
        if(!this.visitedExprSet.add(Objects.requireNonNull(expr))) {
            semanticError(expr.getRange(), "detect circular reference");
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
        try {
            return expr.setType(exprType.isVoid() ? this.env.getVoidType() : this.env.getArrayType(exprType));
        } catch(TypeException e) {
            throw new SemanticException(expr.getRange(), e);
        }
    }

    @Override
    public LType visitOptionalExpr(OptionalExpr expr, Void param) {
        LType exprType = this.checkType(expr.getExpr());
        try {
            return expr.setType(exprType.isVoid() ? this.env.getVoidType() : this.env.getOptionalType(exprType));
        } catch(TypeException e) {
            throw new SemanticException(expr.getRange(), e);
        }
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
            if(!type.isVoid()) {
                types.add(type);
            }
        }

        if(types.isEmpty()) {
            return expr.setType(this.env.getVoidType());
        } else if(types.size() == 1) {
            return expr.setType(types.get(0));
        } else {
            try {
                return expr.setType(this.env.getTupleType(types.toArray(new LType[types.size()])));
            } catch(TypeException e) {
                throw new SemanticException(expr.getRange(), e);
            }
        }
    }

    @Override
    public LType visitChoiceExpr(ChoiceExpr expr, Void param) {
        List<LType> types = new ArrayList<>();
        for(ParsingExpression e : expr.getExprs()) {
            LType type = this.checkType(e);
            if(!type.isVoid()) {
                types.add(type);
            }
        }
        if(!types.isEmpty() && types.size() < expr.getExprs().size()) {
            semanticError(expr.getRange(), "not allow void type");
        }

        if(types.isEmpty()) {
            return expr.setType(this.env.getVoidType());
        }

        boolean sameAll = true;
        final int size = types.size();
        for(int i = 1; i < size; i++) {
            if(!types.get(0).equals(types.get(i))) {
                sameAll = false;
                break;
            }
        }
        if(sameAll) {
            return expr.setType(types.get(0));
        } else {
            try {
                return expr.setType(this.env.getUnionType(types.toArray(new LType[types.size()])));
            } catch(TypeException e) {
                throw new SemanticException(expr.getRange(), e);
            }
        }
    }

    @Override
    public LType visitNonTerminalExpr(NonTerminalExpr expr, Void param) {
        ParsingExpression targetExpr = this.ruleMap.get(expr.getName());
        if(targetExpr == null) {
            semanticError(expr.getRange(), "undefined rule: " + expr.getName());
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
            semanticError(expr.getRange(), "not need label");
        }
        return expr.setType(this.checkType(expr.getExpr()));
    }

    @Override
    public LType visitTypedRuleExpr(TypedRuleExpr expr, Void param) {
        boolean primary = this.env.isPrimaryType(expr.getTypeName());
        boolean hasLabel = this.labeledExprDetector.visit(expr.getExpr());

        try {
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
                semanticError(expr.getRange(), "illegal type annotation");
                return null;
            }
        } catch(TypeException e) {
            throw new SemanticException(expr.getRange(), e);
        }
    }

    @Override
    public LType visitPrefixExpr(PrefixExpr expr, Void param) {
        this.labeledExprVerifier.visit(expr);
        if(!this.labeledExprDetector.visit(expr)) {
            semanticError(expr.getRange(), "require label");
        }
        for(ParsingExpression e : expr.getExprs()) {
            this.checkType(this.env.getVoidType(), e);
        }
        return expr.setType(this.env.getVoidType());
    }

    @Override
    public LType visitCaseExpr(CaseExpr expr, Void param) {
        this.labeledExprVerifier.visit(expr);
        if(!this.labeledExprDetector.visit(expr)) {
            semanticError(expr.getRange(), "require label");
        }
        for(ParsingExpression e : expr.getExprs()) {
            this.checkType(this.env.getVoidType(), e);
        }
        return expr.setType(this.env.getVoidType());
    }
}
