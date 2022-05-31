package pl.baluch.commands;

import java.util.List;

public record CommandArgumentList(List<CommandArgument> types, String[] args) {

    public <T> T getArgument(int index) {
        Object value = types.get(index).type().process(args[index]);
        return (T) value;
    }
}
