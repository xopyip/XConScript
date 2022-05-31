package pl.baluch.commands;

import java.util.ArrayList;
import java.util.List;

public class CommandArgumentListBuilder {
    private final List<CommandArgument> args;

    public CommandArgumentListBuilder() {
        this.args = new ArrayList<>();
    }

    public List<CommandArgument> getArgs() {
        return args;
    }

    public CommandArgumentListBuilder add(CommandArgument arg) {
        args.add(arg);
        return this;
    }

    public CommandArgumentList build(String[] args){
        return new CommandArgumentList(this.args, args);
    }
}
