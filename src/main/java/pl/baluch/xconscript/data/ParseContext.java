package pl.baluch.xconscript.data;

import java.util.*;

public class ParseContext {
    private final Map<String, Block> methods = new HashMap<>();
    private final Map<String, Block> inline = new HashMap<>();

    public ParseContext() {
    }

    public Map<String, Block> getMethods() {
        return methods;
    }

    public void addMethod(String methodName, Block method) {
        //todo: check if block is method
        methods.put(methodName, method);
    }

    public Block getMethod(String name){
        return methods.get(name);
    }

    public void addInlineMethod(String blockName, Block block) {
        inline.put(blockName, block);
    }

    public Map<String, Block> getInlineMethods() {
        return inline;
    }

    public Block getInlineMethod(String name) {
        return inline.get(name);
    }
}
