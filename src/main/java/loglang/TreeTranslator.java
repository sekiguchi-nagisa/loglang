package loglang;

import loglang.misc.Utils;
import nez.ast.CommonTree;

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
            Utils.fatal("duplicated tag: " + tagName);
        }
    }

    public R translate(CommonTree tree) {
        String key = tree.getTag().getName();
        TagHandler<R> handler = this.handlerMap.get(key);
        if(Objects.isNull(handler)) {
            Utils.fatal("undefined handler: " + key);
        }
        try {
            return handler.invoke(tree);
        } catch(Exception e) {
            Utils.propagate(e);
        }
        return null;
    }

    @FunctionalInterface
    public interface TagHandler<T> {
        T invoke(CommonTree tree) throws Exception;
    }
}
