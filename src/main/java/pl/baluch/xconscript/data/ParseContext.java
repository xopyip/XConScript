package pl.baluch.xconscript.data;

import java.util.*;

public class ParseContext {
    private final Map<String, Block> methods = new HashMap<>();

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
}
