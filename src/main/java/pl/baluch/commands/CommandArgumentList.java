package pl.baluch.commands;

import java.util.List;

public record CommandArgumentList(List<CommandArgument> types, String[] args) {

    public <T> T getArgument(int index) {
        Object value = types.get(index).type().process(args[index]);
        return (T) value;
    }
    public <T> T getArgument(int index, CommandArgumentType type) {
        if(index < types.size()){
            if(types.get(index).type() != type){
                throw new IllegalArgumentException("Argument type mismatch");
            }
        }
        Object value = type.process(args[index]);
        return (T) value;
    }

    public int argCount() {
        return args.length;
    }
}
