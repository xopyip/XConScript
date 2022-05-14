package pl.baluch.xconscript.data;

import java.util.*;

public abstract class BlockType {
    private final Map<String, ContextBlockType<?>> contextBlocks;
    private final BlockScope scope;
    private final boolean hasName;
    private final String name;
    private final DataType returnType;
    private final List<MethodArgument> arguments;

    public BlockType(BlockScope scope, String name, boolean hasName) {
        this(scope, name, hasName, new ArrayList<>());
    }
    public BlockType(BlockScope scope, String name, boolean hasName, List<ContextBlockType<?>> contextBlocks) {
        this(scope, name, hasName, contextBlocks, null);
    }
    public BlockType(BlockScope scope, String name, boolean hasName, List<ContextBlockType<?>> contextBlocks, DataType returnType, MethodArgument ... arguments) {
        this.scope = scope;
        this.name = name;
        this.hasName = hasName;
        this.returnType = returnType;
        this.arguments = Arrays.asList(arguments);
        this.contextBlocks = new HashMap<>();
        for (ContextBlockType<?> cb : contextBlocks) {
            this.contextBlocks.put(cb.getName(), cb);
        }
    }

    public BlockScope getScope() {
        return scope;
    }

    public String getName() {
        return name;
    }

    public boolean hasName() {
        return hasName;
    }

    @Override
    public String toString() {
        return name;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public List<MethodArgument> getArguments() {
        return arguments == null ? new ArrayList<>() : arguments;
    }

    public boolean hasContextBlock(String name) {
        return contextBlocks.containsKey(name);
    }

    public abstract void endBlock(Block block, Stack<Block> blockSet, ParseContext ctx) throws Exception;

    public ContextBlockType<?> getContextBlock(String value) {
        return contextBlocks.get(value);
    }

    public enum BlockScope {
        GLOBAL, LOCAL
    }
}
