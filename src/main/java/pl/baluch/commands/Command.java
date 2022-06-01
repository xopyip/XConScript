package pl.baluch.commands;

import java.util.List;
import java.util.Set;

public interface Command {
    void execute(CommandArgumentList args);
    String getName();
    String getDescription();
    CommandArgumentListBuilder getArgs();

    List<CommandFlag> getFlags();
}
