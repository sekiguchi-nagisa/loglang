package loglang.misc;

/**
 * Created by skgchxngsxyz-osx on 15/08/13.
 */
public class Pair<L, R> {
    private L left;
    private R right;

    Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<>(left, right);
    }

    public L getLeft() {
        return this.left;
    }

    public void setLeft(L left) {
        this.left = left;
    }

    public R getRight() {
        return this.right;
    }

    public void setRight(R right) {
        this.right = right;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pair &&
                this.left != null && this.left.equals(((Pair) obj).left) &&
                this.right != null && this.right.equals(((Pair) obj).right);

    }
}
