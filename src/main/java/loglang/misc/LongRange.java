package loglang.misc;

/**
 * Created by skgchxngsxyz-osx on 15/09/03.
 */
public class LongRange {
    public final long pos;
    public final long len;
    
    public LongRange(long pos, long len) {
        this.pos = pos;
        this.len = len;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LongRange
                && this.pos == ((LongRange) obj).pos
                && this.len == ((LongRange) obj).len;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("(pos=")
                .append(this.pos)
                .append(", len=")
                .append(this.len)
                .append(")")
                .toString();
    }
}
