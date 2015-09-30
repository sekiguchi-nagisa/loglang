package loglang;

import nez.ast.Tree;

import java.io.PrintStream;

/**
 * Created by skgchxngsxyz-osx on 15/09/30.
 */
public class TreePrinter extends TreeTranslator<Void> {
    protected PrintStream stream = null;

    public void print(Tree<?> tree) {
        this.translate(tree);
    }

    public static TreePrinter newPrinter(PrintStream stream) {
        TreePrinter p = new TreePrinterImpl();
        p.stream = stream;
        return p;
    }
}

class TreePrinterImpl extends TreePrinter {{
    this.add("RuleExpr", t -> {
        this.stream.print(t.get(0).toText());
        if(!t.get(1).toText().isEmpty()) {
            this.stream.print(" : ");
            this.stream.print(t.get(1).toText());
        }
        this.stream.print(" = ");
        this.print(t.get(2));
        this.stream.println();
        return null;
    });

    this.add("ChoiceExpr", t -> {
        final int size = t.size();
        for(int i = 0; i < size; i++) {
            if(i > 0) {
                this.stream.print(" / ");
            }
            this.print(t.get(i));
        }
        return null;
    });

    this.add("SequenceExpr", t -> {
        final int size = t.size();
        for(int i = 0; i < size; i++) {
            if(i > 0) {
                this.stream.print(" ");
            }
            this.print(t.get(i));
        }
        return null;
    });

    this.add("LabeledExpr", t -> {
        this.stream.print("$");
        this.stream.print(t.get(0).toText());
        this.stream.print(" : ");
        this.print(t.get(1));
        return null;
    });

    this.add("AndExpr", t -> {
        this.stream.print("&(");
        this.print(t.get(0));
        this.stream.print(")");
        return null;
    });

    this.add("NotExpr", t -> {
        this.stream.print("!(");
        this.print(t.get(0));
        this.stream.print(")");
        return null;
    });

    this.add("ZeroMoreExpr", t -> {
        this.stream.print("(");
        this.print(t.get(0));
        this.stream.print(")*");
        return null;
    });

    this.add("OneMoreExpr", t -> {
        this.stream.print("(");
        this.print(t.get(0));
        this.stream.print(")+");
        return null;
    });

    this.add("OptionalExpr", t -> {
        this.stream.print("(");
        this.print(t.get(0));
        this.stream.print(")?");
        return null;
    });

    this.add("NonTerminalExpr", t -> {
        this.stream.print(t.toText());
        return null;
    });

    this.add("AnyExpr", t -> {
        this.stream.print(".");
        return null;
    });

    this.add("StringExpr", t -> {
        this.stream.print(t.toText());
        return null;
    });

    this.add("CharClassExpr", t -> {
        this.stream.print(t.toText());
        return null;
    });
}}
