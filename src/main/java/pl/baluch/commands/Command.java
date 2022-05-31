package pl.baluch.commands;

public interface Command {
    void execute(CommandArgumentList args);
    String getName();
    String getDescription();
    CommandArgumentListBuilder getArgs();
}
