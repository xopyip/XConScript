package pl.baluch.commands;

import java.util.List;

public record CommandArgumentList(List<CommandFlag> flags, List<CommandArgument> types, List<String> args) {

    public <T> T getArgument(int index) {
        Object value = types.get(index).type().process(args.get(index));
        //noinspection unchecked
        return (T) value;
    }
    public <T> T getArgument(int index, CommandArgumentType type) {
        if(index < types.size()){
            if(types.get(index).type() != type){
                throw new IllegalArgumentException("Argument type mismatch");
            }
        }
        Object value = type.process(args.get(index));
        //noinspection unchecked
        return (T) value;
    }

    public int argCount() {
        return args.size();
    }

    public boolean hasFlag(CommandFlag flag) {
        return flags.contains(flag);
    }
}
