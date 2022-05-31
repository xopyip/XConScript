package pl.baluch.xconscript;

import pl.baluch.xconscript.data.ParseContext;
import pl.baluch.xconscript.data.Script;
import pl.baluch.xconscript.tokens.Token;

import java.io.File;
import java.util.List;

public class BuildContext {
    private final ScriptLexer lexer;
    private final ScriptParser parser;
    private final ScriptCompiler compiler;
    private Script script;

    public BuildContext() {
        this.lexer = new ScriptLexer();
        this.parser = new ScriptParser();
        this.compiler = new ScriptCompiler();
    }

    public Script build(File scriptFile, String className) throws Exception {
        List<Token<?>> tokens = lexer.tokenize(scriptFile);
        ParseContext ctx = parser.parse(tokens);
        this.script = compiler.compile(className, ctx);
        return this.script;
    }

    public Script getScript() {
        return script;
    }
}
