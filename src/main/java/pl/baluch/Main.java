package pl.baluch;

import pl.baluch.commands.Command;
import pl.baluch.commands.CompileCommand;
import pl.baluch.commands.DumpCommand;

import java.util.Arrays;

public class Main {
    private static final Command[] COMMANDS = new Command[]{
            new CompileCommand(),
            new DumpCommand()
    };
    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("You must provide at least 2 arguments!");
            System.err.println("Usage: java -jar xconscript.jar <action> <input file>");
            System.err.println("Actions:");
            for(Command command : COMMANDS) {
                System.err.println("\t" + command.getName() + "\t\t" + command.getDescription());
            }
            System.exit(1);
        }
        process(args);
    }

    private static void process(String[] args) {
        for (Command command : COMMANDS) {
            if(command.getName().equals(args[0])) {
                command.execute(command.getArgs().build(Arrays.copyOfRange(args, 1, args.length)));
                return;
            }
        }
        System.err.println("Unknown option: " + args[0]);
        System.exit(1);
    }
}