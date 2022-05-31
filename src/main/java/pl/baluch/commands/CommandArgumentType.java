package pl.baluch.commands;

import java.io.File;
import java.util.function.Function;

public enum CommandArgumentType {
    FILE(File::new),
    STRING(str -> str);


    private final Function<String, Object> transformer;

    CommandArgumentType(Function<String, Object> transformer) {
        this.transformer = transformer;
    }

    public Object process(String arg) {
        return transformer.apply(arg);
    }

    public CommandArgument of(String name){
        return new CommandArgument( this, name);
    }
}
