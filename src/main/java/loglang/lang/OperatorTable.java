package loglang.lang;

import loglang.lang.Operator.Kind;

/**
 * Created by skgchxngsxyz-osx on 15/10/06.
 */
public class OperatorTable {
    // unary op
    @Operator(Kind.PLUS) public static int plus(int right) {
        return right;
    }

    @Operator(Kind.PLUS) public static float plus(float right) {
        return right;
    }

    @Operator(Kind.MINUS) public static int minus(int right) {
        return -right;
    }

    @Operator(Kind.MINUS) public static float minus(float right) {
        return -right;
    }

    @Operator(Kind.NOT) public static boolean not(boolean right) {
        return !right;
    }

    @Operator(Kind.NOT) public static int not(int right) {
        return ~right;
    }

    // binary op
    // ADD
    @Operator(Kind.ADD) public static int add(int left, int right) {
        return left + right;
    }

    @Operator(Kind.ADD) public static float add(float left, float right) {
        return left + right;
    }

    @Operator(Kind.ADD) public static String add(String left, Object right) {
        return left + right;
    }

    // SUB
    @Operator(Kind.SUB) public static int sub(int left, int right) {
        return left - right;
    }

    @Operator(Kind.SUB) public static float sub(float left, float right) {
        return left - right;
    }

    // MUL
    @Operator(Kind.MUL) public static int mul(int left, int right) {
        return left * right;
    }

    @Operator(Kind.MUL) public static float mul(float left, float right) {
        return left * right;
    }

    // DIV
    @Operator(Kind.DIV) public static int div(int left, int right) {
        return left / right;
    }

    @Operator(Kind.DIV) public static float div(float left, float right) {
        return left / right;
    }

    // MOD
    @Operator(Kind.MOD) public static int mod(int left, int right) {
        return left % right;
    }

    // EQ
    @Operator(Kind.EQ) public static boolean eq(int left, int right) {
        return left == right;
    }

    @Operator(Kind.EQ) public static boolean eq(float left, float right) {
        return left == right;
    }

    @Operator(Kind.EQ) public static boolean eq(boolean letf, boolean right) {
        return letf == right;
    }

    @Operator(Kind.EQ) public static boolean eq(String left, String right) {
        return left.equals(right);
    }

    // NE
    @Operator(Kind.NE) public static boolean ne(int left, int right) {
        return left != right;
    }

    @Operator(Kind.NE) public static boolean ne(float left, float right) {
        return left != right;
    }

    @Operator(Kind.NE) public static boolean ne(boolean letf, boolean right) {
        return letf != right;
    }

    @Operator(Kind.NE) public static boolean ne(String left, String right) {
        return !left.equals(right);
    }

    // LT
    @Operator(Kind.LT) public static boolean lt(int left, int right) {
        return left < right;
    }

    @Operator(Kind.LT) public static boolean lt(float left, float right) {
        return left < right;
    }

    @Operator(Kind.LT) public static boolean lt(String left, String right) {
        return left.compareTo(right) < 0;
    }

    // GT
    @Operator(Kind.GT) public static boolean gt(int left, int right) {
        return left > right;
    }

    @Operator(Kind.GT) public static boolean gt(float left, float right) {
        return left > right;
    }

    @Operator(Kind.GT) public static boolean gt(String left, String right) {
        return left.compareTo(right) > 0;
    }

    // LE
    @Operator(Kind.LE) public static boolean le(int left, int right) {
        return left <= right;
    }

    @Operator(Kind.LE) public static boolean le(float left, float right) {
        return left <- right;
    }

    @Operator(Kind.LE) public static boolean le(String left, String right) {
        return left.compareTo(right) <= 0;
    }

    // GE
    @Operator(Kind.GE) public static boolean ge(int left, int right) {
        return left >= right;
    }

    @Operator(Kind.GE) public static boolean ge(float left, float right) {
        return left >= right;
    }

    @Operator(Kind.GE) public static boolean ge(String left, String right) {
        return left.compareTo(right) >= 0;
    }

    // AND
    @Operator(Kind.AND) public static int and(int left, int right) {
        return left & right;
    }

    // OR
    @Operator(Kind.OR) public static int or(int left, int right) {
        return left | right;
    }

    // XOR
    @Operator(Kind.XOR) public static int xor(int left, int right) {
        return left ^ right;
    }
}
