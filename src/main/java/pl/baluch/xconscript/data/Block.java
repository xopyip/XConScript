package pl.baluch.xconscript.data;

import org.objectweb.asm.Type;
import pl.baluch.xconscript.operations.Operation;

import java.util.*;

//todo: extract method components to separate class
public class Block {
    private final Block parent;
    private final BlockType blockType;
    private final String blockName;
    public int localVarSize = 0;
    private final List<Operation<?>> ops = new ArrayList<>();
    private final List<MethodArgument> arguments;
    private final DataType returnType;
    private final Map<String, LocalVariable> variables = new HashMap<>();

    private final Stack<Operation<?>> opsToSetup = new Stack<>();
    private final Map<String, Object> contextConstants = new HashMap<>();


    public Block(Block parent, BlockType blockType) {
        this(parent, blockType, null);
    }

    public Block(Block parent, BlockType blockType, String blockName) {
        this(parent, blockType, blockName, new ArrayList<>(), DataType.VOID);
    }

    public Block(Block parent, BlockType blockType, String blockName, List<MethodArgument> arguments, DataType returnType) {
        this.parent = parent;
        this.blockType = blockType;
        this.blockName = blockName;
        this.arguments = arguments;
        this.returnType = returnType;
        if (parent != null && arguments.size() > 0) {
            throw new IllegalArgumentException("Arguments can't be added to a block with parent");
        }
        for (MethodArgument argument : arguments) {
            variables.put(argument.name(), new LocalVariable(reserveVariableSpace(argument.type().getASMType()), argument.type()));
        }
    }


    //returns id of current local variable
    public int reserveVariableSpace(Type type) {
        int idx = this.localVarSize;
        this.localVarSize += type.getSize();
        return idx;
    }

    public void addOperation(Operation<?> op) {
        ops.add(op);
    }

    public List<Operation<?>> getOperations() {
        return ops;
    }

    public Type[] getArgumentTypes() {
        return arguments.stream().map(MethodArgument::type).map(DataType::getASMType).toArray(Type[]::new);
    }

    public List<MethodArgument> getArguments() {
        return arguments;
    }

    public DataType getReturnType() {
        return returnType;
    }

    public int addVariable(String varName, DataType type) {
        //todo:implement variable scopes
        if(parent != null){
            return parent.addVariable(varName, type);
        }
        LocalVariable localVariable = new LocalVariable(reserveVariableSpace(type.getASMType()), type);
        variables.put(varName, localVariable);
        return localVariable.idx();
    }

    public Object[] getVarTypes() {
        if(parent != null){
            return parent.getVarTypes();
        }
        return this.variables.values().stream().sorted(Comparator.comparingInt(LocalVariable::idx))
                .map(LocalVariable::type).map(DataType::getObject).toArray();
    }

    public LocalVariable getVariable(String name) {
        if(parent != null){
            return parent.getVariable(name);
        }
        return variables.get(name);
    }
    public boolean hasVariable(String name){
        if(parent != null){
            return parent.hasVariable(name);
        }
        return variables.containsKey(name);
    }

    public boolean hasNoOpsToSetup() {
        return opsToSetup.isEmpty();
    }

    public Operation<?> getOpToSetup() {
        return opsToSetup.pop();
    }

    public Operation<?> peekOpToSetup() {
        return opsToSetup.peek();
    }

    public void addOpToSetup(Operation<?> op) {
        opsToSetup.push(op);
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void addOperations(List<Operation<?>> operations) {
        ops.addAll(operations);
    }

    public String getBlockName() {
        return blockName;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockType=" + blockType +
                ", blockName='" + blockName + '\'' +
                '}';
    }

    public void registerContextBlock(String name, Object value) {
        if(parent != null){
            parent.registerContextBlock(name, value);
            return;
        }
        contextConstants.put(name, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getContextValue(String name, T defaultValue) {
        if(!contextConstants.containsKey(name)){
            return defaultValue;
        }
        return (T) contextConstants.get(name);
    }
}
