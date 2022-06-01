package pl.baluch.commands;

import java.util.ArrayList;
import java.util.Arrays;
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
        List<String> argList = new ArrayList<>(Arrays.asList(args));
        List<CommandFlag> flags = new ArrayList<>();
        for (CommandFlag value : CommandFlag.values()) {
            if (argList.contains(value.getFlag())) {
                flags.add(value);
                argList.remove(value.getFlag());
            }
        }
        return new CommandArgumentList(flags, this.args, argList);
    }
}
