package pl.baluch.commands;

import org.objectweb.asm.tree.MethodNode;
import pl.baluch.xconscript.ScriptCompiler;
import pl.baluch.xconscript.ScriptLexer;
import pl.baluch.xconscript.ScriptParser;
import pl.baluch.xconscript.data.ParseContext;
import pl.baluch.xconscript.data.Script;
import pl.baluch.xconscript.tokens.Token;

import java.io.File;
import java.util.List;

public class CompileCommand implements Command{

    @Override
    public void execute(CommandArgumentList args) {
        File scriptFile = args.getArgument(0);
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

    @Override
    public String getName() {
        return "compile";
    }

    @Override
    public String getDescription() {
        return "Compile only";
    }

    @Override
    public CommandArgumentListBuilder getArgs() {
        return new CommandArgumentListBuilder()
                .add(CommandArgumentType.FILE.of("script file"));
    }
}
