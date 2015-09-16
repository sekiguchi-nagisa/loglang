package loglang.peg;

import loglang.type.LType;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;

import static loglang.peg.TypedPEG.*;

/**
 * Created by skgchxngsxyz-osx on 15/09/03.
 */
public class NezGrammarGenerator implements ExpressionVisitor<Void, Void> {
    /**
     * not close it
     */
    private final PrintStream stream;

    public NezGrammarGenerator(PrintStream stream) {
        this.stream = Objects.requireNonNull(stream);
    }

    /**
     *
     * @param ruleExprs
     * @param prefixExpr
     * must be empty or singleton list
     * @param caseExprs
     */
    public void generate(List<RuleExpr> ruleExprs, List<RuleExpr> prefixExpr, List<RuleExpr> caseExprs) {
        // generate pattern
        ruleExprs.stream().forEach(this::visit);

        // generate prefix pattern
        prefixExpr.stream().forEach(this::visit);

        // generate case pattern
        caseExprs.stream().forEach(this::visit);

        // generate entry point
        this.stream.print("File = { ");

        this.stream.print("@{ ");
        if(!prefixExpr.isEmpty()) {
            this.stream.print("@");
            this.stream.print(prefixExpr.get(0).getRuleName());
        }
        this.stream.print(" #Prefix } ");

        this.stream.print("@{ ( ");
        int count = 0;
        for(RuleExpr caseExpr : caseExprs) {
            if(count > 0) {
                this.stream.print(" / ");
            }
            this.stream.print("@");
            this.stream.print(caseExpr.getRuleName());
            this.stream.print(" #" + count++);
        }
        this.stream.print(") }");

        this.stream.println(" [ \\t\\r\\n]* #ResultAST }");
    }

    private void printTypeId(LType type) {
        this.stream.print("#");
        this.stream.print(type.getUniqueName());
    }

    /**
     *
     * @param expr
     * type is not void
     */
    private void printExpr(TypedPEG expr) {
        this.stream.print("@");
        this.visit(expr);
    }

    @Override
    public Void visitAnyExpr(AnyExpr expr, Void param) {
        this.stream.print(".");
        return null;
    }

    @Override
    public Void visitStringExpr(StringExpr expr, Void param) {
        this.stream.print(expr.getText());
        return null;
    }

    @Override
    public Void visitCharClassExpr(CharClassExpr expr, Void param) {
        this.stream.print(expr.getText());
        return null;
    }

    @Override
    public Void visitRepeatExpr(RepeatExpr expr, Void param) {
        if(expr.getType().isVoid()) {
            this.stream.print("( ");
            this.visit(expr.getExpr());
            this.stream.print(" )");
            this.stream.print(expr.isZereMore() ? "*" : "+");
        } else {
            this.stream.print("{ ");
            this.stream.print("( ");
            this.printExpr(expr.getExpr());
            this.stream.print(" )");
            this.stream.print(expr.isZereMore() ? "*" : "+");
            this.stream.print(" ");
            this.printTypeId(expr.getType());
            this.stream.print(" }");
        }
        return null;
    }

    @Override
    public Void visitOptionalExpr(OptionalExpr expr, Void param) {
        if(expr.getType().isVoid()) {
            this.stream.print("( ");
            this.visit(expr.getExpr());
            this.stream.print(" )?");
        } else {
            this.stream.print("{ ");
            this.stream.print("( ");
            this.printExpr(expr.getExpr());
            this.stream.print(" )?");
            this.stream.print(" ");
            this.printTypeId(expr.getType());
            this.stream.print(" }");
        }
        return null;
    }

    @Override
    public Void visitPredicateExpr(PredicateExpr expr, Void param) {
        this.stream.print(expr.isAndPredicate() ? "&" : "!");
        this.stream.print("( ");
        this.visit(expr.getExpr());
        this.stream.print(" )");
        return null;
    }

    @Override
    public Void visitSequenceExpr(SequenceExpr expr, Void param) {
        if(!(expr.getType() instanceof LType.TupleType)) {
            this.stream.print("( ");
            int count = 0;
            for(TypedPEG e : expr.getExprs()) {
                if(count++ > 0) {
                    this.stream.print(" ");
                }
                this.visit(e);
            }
            this.stream.print(" )");
        } else {
            this.stream.print("{ ");
            int count = 0;
            for(TypedPEG e : expr.getExprs()) {
                if(count++ > 0) {
                    this.stream.print(" ");
                }
                this.printExpr(e);
            }
            this.printTypeId(expr.getType());
            this.stream.print(" }");
        }
        return null;
    }

    @Override
    public Void visitChoiceExpr(ChoiceExpr expr, Void param) {
        if(!(expr.getType() instanceof LType.UnionType)) {
            this.stream.print("( ");
            int count = 0;
            for(TypedPEG e : expr.getExprs()) {
                if(count++ > 0) {
                    this.stream.print(" / ");
                }
                this.visit(e);
            }
            this.stream.print(" )");
        } else {
            this.stream.print("{ (");
            int count = 0;
            for(TypedPEG e : expr.getExprs()) {
                if(count++ > 0) {
                    this.stream.print(" / ");
                }
                this.printExpr(e);
            }
            this.stream.print(") ");
            this.printTypeId(expr.getType());
            this.stream.print(" }");
        }
        return null;
    }

    @Override
    public Void visitNonTerminalExpr(NonTerminalExpr expr, Void param) {
        this.stream.print(expr.getName());
        return null;
    }

    @Override
    public Void visitLabeledExpr(LabeledExpr expr, Void param) {
        this.printExpr(expr.getExpr());
        return null;
    }

    @Override
    public Void visitRuleExpr(RuleExpr expr, Void param) {
        this.stream.println(expr.getRuleName());
        this.stream.print("    = ");
        this.visit(expr.getExpr());
        this.stream.println();
        return null;
    }

    @Override
    public Void visitTypedRuleExpr(TypedRuleExpr expr, Void param) {
        this.stream.println(expr.getRuleName());
        this.stream.print("    = { ");
        this.visit(expr.getExpr());
        this.stream.print(" ");
        this.printTypeId(expr.getType());
        this.stream.println(" }");
        return null;
    }

    @Override
    public Void visitRootExpr(RootExpr expr, Void param) {
        expr.getExprs().stream().forEach(this::visit);
        return null;
    }
}
