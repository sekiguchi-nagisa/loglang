package loglang;

import loglang.misc.FatalError;
import loglang.misc.Utils;
import nez.ast.Tree;
import nez.peg.tpeg.LongRange;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by skgchxngsxyz-osx on 15/08/28.
 */
public abstract class TreeTranslator<R> {
    protected final Map<String, TagHandler<R>> handlerMap = new HashMap<>();

    protected void add(String tagName, TagHandler<R> handler) {
        if(Objects.nonNull(this.handlerMap.put(tagName, handler))) {
            throw new FatalError("duplicated tag: " + tagName);
        }
    }

    public R translate(Tree<?> tree) {
        String key = tree.getTag().getSymbol();
        TagHandler<R> handler = this.handlerMap.get(key);
        if(Objects.isNull(handler)) {
            throw new FatalError("undefined handler: " + key);
        }
        try {
            return handler.invoke(tree);
        } catch(Exception e) {
            throw Utils.propagate(e);
        }
    }

    @FunctionalInterface
    public interface TagHandler<T> {
        T invoke(Tree<?> tree) throws Exception;
    }

    public static LongRange range(Tree<?> tree) {
        return new LongRange(tree.getSourcePosition(), tree.getLength());
    }
}
