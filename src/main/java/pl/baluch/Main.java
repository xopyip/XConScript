package pl.baluch;

import org.objectweb.asm.tree.MethodNode;
import pl.baluch.xconscript.ScriptCompiler;
import pl.baluch.xconscript.ScriptLexer;
import pl.baluch.xconscript.ScriptParser;
import pl.baluch.xconscript.data.ParseContext;
import pl.baluch.xconscript.data.Script;
import pl.baluch.xconscript.tokens.Token;

import java.io.File;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("You must provide at least 2 arguments!");
            System.err.println("Usage: java -jar xconscript.jar <action> <input file>");
            System.err.println("Actions:");
            System.err.println("\tcompile\t\tCompile only");
            System.err.println("\tdump\t\tDump .class file");
            System.exit(1);
        }
        File inputFile = new File(args[1]);
        switch (args[0]) {
            case "compile" -> compile(inputFile);
            case "dump" -> ClassDumper.dump(inputFile);
            default -> {
                System.err.println("Unknown option: " + args[0]);
                System.exit(1);
            }
        }
    }

    private static void compile(File scriptFile) {
        if(!scriptFile.exists()){
            return;
        }
        ScriptLexer lexer = new ScriptLexer();
        ScriptParser parser = new ScriptParser();
        ScriptCompiler compiler = new ScriptCompiler();
        try {
            List<Token<?>> tokens = lexer.tokenize(scriptFile);
            ParseContext ctx = parser.parse(tokens);
            String className = scriptFile.getName().toLowerCase().split("\\.")[0];
            className = "Script" + className.substring(0, 1).toUpperCase() + className.substring(1);
            Script script = compiler.compile(className, ctx);

            System.out.println("\nMethods of \"" + className + "\":");
            for (MethodNode method : script.getMethods()) {
                System.out.println(method.name + method.desc + " (" + method.instructions.size() + " instructions)");
            }
            script.saveClass(new File(scriptFile.getParentFile(), className + ".class"));
            System.out.println("\nClass saved to " + scriptFile.getParentFile().getAbsolutePath());
            System.out.println("\nRun it using java -cp " + scriptFile.getParentFile().getAbsolutePath() + " " + className);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}