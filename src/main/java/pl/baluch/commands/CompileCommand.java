package pl.baluch.commands;

import org.objectweb.asm.tree.MethodNode;
import pl.baluch.xconscript.BuildContext;
import pl.baluch.xconscript.data.Script;

import java.io.File;

public class CompileCommand implements Command{

    @Override
    public void execute(CommandArgumentList args) {
        for(int i = 0; i<args.argCount(); i++){
            File scriptFile = args.getArgument(i, CommandArgumentType.FILE);
            if(!scriptFile.exists()){
                return;
            }
            BuildContext buildContext = new BuildContext();
            String className = scriptFile.getName().substring(0, scriptFile.getName().length() - ".xript".length());
            className = className.substring(0, 1).toUpperCase() + className.substring(1);
            try {
                Script script = buildContext.build(scriptFile, className);

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
