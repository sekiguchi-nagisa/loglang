package loglang.misc;

/**
 * Created by skgchxngsxyz-opensuse on 15/09/02.
 */
public class ImmutablePair<L, R> {
    public final L left;
    public final R right;

    ImmutablePair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> ImmutablePair<L, R> of(L left, R right) {
        return new ImmutablePair<>(left, right);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImmutablePair &&
                this.left != null && this.left.equals(((ImmutablePair) obj).left) &&
                this.right != null && this.right.equals(((ImmutablePair) obj).right);
    }

    @Override
    public String toString() {
        return "(" + this.left + ", " + this.right + ")";
    }
}
