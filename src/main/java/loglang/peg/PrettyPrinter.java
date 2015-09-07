package loglang.peg;

import loglang.misc.TypeMatch;
import loglang.misc.Utils;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by skgchxngsxyz-osx on 15/08/31.
 */
public class PrettyPrinter {
    private int indentLevel = 0;
    private PrintStream stream = System.err;

    public void printPEG(PrintStream stream, ParsingExpression expr) {
        this.stream = stream != null ? stream : System.err;
        this.print(expr);
        this.stream.println();
        this.stream.flush();
    }

    private void printIndent() {
        for(int i = 0; i < this.indentLevel; i++) {
            this.stream.print("  ");
        }
    }

    private void printField(Object owner, Field field) {
        this.stream.print(field.getName());
        this.stream.print(" : ");
        try {
            field.setAccessible(true);
            Object fieldValue = field.get(owner);
            this.print(fieldValue);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Utils.fatal(e.getClass() + ":" + e.getMessage());
        }
    }

    private void print(Object value) {
        TypeMatch.match(value)
                .when(ParsingExpression.class, (t) -> {
                    this.stream.println("{");
                    this.indentLevel++;

                    // print class
                    this.printIndent();
                    this.stream.print("ExprType : ");
                    this.stream.print(t.getClass().getSimpleName());
                    this.stream.println(",");

                    // print field
                    List<Field> instanceFields = new ArrayList<>();
                    Class<?> clazz = value.getClass();
                    while(clazz != null) {
                        Field[] fields = clazz.getDeclaredFields();
                        for(Field field : fields) {
                            if(!Modifier.isStatic(field.getModifiers())) {
                                instanceFields.add(field);
                            }
                        }
                        clazz = clazz.getSuperclass();
                    }
                    int size = instanceFields.size();
                    for(int i = 0; i < size; i++) {
                        this.printIndent();
                        this.printField(value, instanceFields.get(i));
                        if(i != size - 1) {
                            this.stream.print(",");
                        }
                        this.stream.println();
                    }

                    this.indentLevel--;
                    this.printIndent();
                    this.stream.print("}");
                })
                .when(List.class, (t) -> {
                    final int size = t.size();
                    if(size == 0) {
                        this.stream.print("[]");
                        return;
                    }

                    this.stream.println("[");
                    this.indentLevel++;

                    for(int i = 0; i < size; i++) {
                        this.printIndent();
                        Object e = t.get(i);
                        this.print(e);
                        if(i != size - 1) {
                            this.stream.print(",");
                        }
                        this.stream.println();
                    }

                    this.indentLevel--;
                    this.printIndent();
                    this.stream.print("]");
                })
                .orElse((t) -> {
                    if(t.isPresent()) {
                        this.stream.print(t.get().toString());
                    } else {
                        this.stream.print("(null)");
                    }
                });
    }
}
