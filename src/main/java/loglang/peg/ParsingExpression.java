package loglang.peg;

import loglang.type.LType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/28.
 */
public abstract class ParsingExpression {
    /**
     * may be null.
     */
    protected LType type;

    public void setType(LType type) {
        this.type = Objects.requireNonNull(type);
    }

    /**
     *
     * @return
     * may be null
     */
    public LType getType() {
        return type;
    }

    public abstract <T, P> T accept(ExpressionVisitor<T, P> visitor, P param);

    public static class AnyExpr extends ParsingExpression {
        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitAnyExpr(this, param);
        }
    }

    public static class StringExpr extends ParsingExpression {
        private final String text;

        /**
         *
         * @param text
         * must be raw string(not unquoted)
         */
        public StringExpr(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitStringExpr(this, param);
        }
    }

    public static class CharClassExpr extends ParsingExpression {
        private final String text;

        /**
         *
         * @param text
         * must be raw string
         */
        public CharClassExpr(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitCharClassExpr(this, param);
        }
    }

    /**
     * for Zero More and One More
     */
    public static class RepeatExpr extends ParsingExpression {
        private final ParsingExpression exprNode;

        /**
         * if true, represents zero more.
         * if false, represents one more.
         */
        private final boolean zereMore;

        private RepeatExpr(ParsingExpression exprNode, boolean zeroMore) {
            this.exprNode = Objects.requireNonNull(exprNode);
            this.zereMore = zeroMore;
        }

        public static RepeatExpr oneMore(ParsingExpression exprNode) {
            return new RepeatExpr(exprNode, false);
        }

        public static RepeatExpr zeroMore(ParsingExpression exprNode) {
            return new RepeatExpr(exprNode, true);
        }

        public ParsingExpression getExprNode() {
            return exprNode;
        }

        public boolean isZereMore() {
            return zereMore;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitRepeatExpr(this, param);
        }
    }

    public static class OptionalExpr extends ParsingExpression {
        private final ParsingExpression expr;

        public OptionalExpr(ParsingExpression expr) {
            this.expr = Objects.requireNonNull(expr);
        }

        public ParsingExpression getExpr() {
            return expr;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitOptionalExpr(this, param);
        }
    }

    /**
     * for And predicate or Not predicate
     */
    public static class PredicateExpr extends ParsingExpression {
        private final ParsingExpression expr;

        /**
         * if true, represents and predicate
         * if fase, represents not predicate
         */
        private final boolean andPredicate;

        private PredicateExpr(ParsingExpression expr, boolean andPredicate) {
            this.expr = Objects.requireNonNull(expr);
            this.andPredicate = andPredicate;
        }

        public static PredicateExpr andPredicate(ParsingExpression exprNode) {
            return new PredicateExpr(exprNode, true);
        }

        public static PredicateExpr notPredicate(ParsingExpression exprNode) {
            return new PredicateExpr(exprNode, false);
        }

        public ParsingExpression getExpr() {
            return expr;
        }

        public boolean isAndPredicate() {
            return andPredicate;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitPredicateExpr(this, param);
        }
    }

    public static class SequenceExpr extends ParsingExpression {
        private List<ParsingExpression> exprs = new ArrayList<>();

        /**
         * if leftNode or rightNode is SequenceExpr, merge to exprs.
         * @param leftNode
         * @param rightNode
         */
        public SequenceExpr(ParsingExpression leftNode, ParsingExpression rightNode) {
            if(leftNode instanceof SequenceExpr) {
                exprs.addAll(((SequenceExpr) leftNode).getExprs());
            } else {
                exprs.add(Objects.requireNonNull(leftNode));
            }

            if(rightNode instanceof SequenceExpr) {
                exprs.addAll(((SequenceExpr) rightNode).getExprs());
            } else {
                exprs.add(Objects.requireNonNull(rightNode));
            }

            // freeze
            this.exprs = Collections.unmodifiableList(this.exprs);
        }

        /**
         *
         * @return
         * read only.
         */
        public List<ParsingExpression> getExprs() {
            return exprs;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitSequenceExpr(this, param);
        }
    }

    public static class ChoiceExpr extends ParsingExpression {
        private List<ParsingExpression> exprs = new ArrayList<>();

        public ChoiceExpr(ParsingExpression leftNode, ParsingExpression rightNode) {
            if(leftNode instanceof ChoiceExpr) {
                this.exprs.addAll(((ChoiceExpr) leftNode).getExprs());
            } else {
                this.exprs.add(Objects.requireNonNull(leftNode));
            }

            if(rightNode instanceof ChoiceExpr) {
                this.exprs.addAll(((ChoiceExpr) rightNode).getExprs());
            } else {
                this.exprs.add(Objects.requireNonNull(rightNode));
            }

            // freeze
            this.exprs = Collections.unmodifiableList(this.exprs);
        }

        /**
         *
         * @return
         * read only
         */
        public List<ParsingExpression> getExprs() {
            return exprs;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitChoiceExpr(this, param);
        }
    }

    public static class NonTerminalExpr extends ParsingExpression {
        private final String name;

        public NonTerminalExpr(String name) {
            this.name = Objects.requireNonNull(name);
        }

        public String getName() {
            return name;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitNonTerminalExpr(this, param);
        }
    }

    public static class LabeledExpr extends ParsingExpression {
        private final String labelName;
        private final ParsingExpression expr;

        public LabeledExpr(String labelName, ParsingExpression expr) {
            this.labelName = Objects.requireNonNull(labelName);
            this.expr = Objects.requireNonNull(expr);
        }

        public String getLabelName() {
            return labelName;
        }

        public ParsingExpression getExpr() {
            return expr;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitLabeledExpr(this, param);
        }
    }

    public static class RuleExpr extends ParsingExpression {
        protected final String ruleName;
        protected final ParsingExpression expr;

        public RuleExpr(String ruleName, ParsingExpression expr) {
            this.ruleName = ruleName;
            this.expr = expr;
        }

        public String getRuleName() {
            return ruleName;
        }

        public ParsingExpression getExpr() {
            return expr;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitRuleExpr(this, param);
        }
    }

    public static class TypedRuleExpr extends RuleExpr {
        private final String typeName;

        public TypedRuleExpr(String ruleName, String typeName, ParsingExpression expr) {
            super(ruleName, expr);
            this.typeName = typeName;
        }

        public String getTypeName() {
            return typeName;
        }

        @Override
        public <T, P> T accept(ExpressionVisitor<T, P> visitor, P param) {
            return visitor.visitTypedRuleExpr(this, param);
        }
    }

    public static void main(String[] args) {

    }
}
