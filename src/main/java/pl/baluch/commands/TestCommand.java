package pl.baluch.commands;

import pl.baluch.utils.ConsoleColors;
import pl.baluch.xconscript.BuildContext;
import pl.baluch.xconscript.data.Script;

import java.io.*;
import java.util.Locale;
import java.util.Objects;

public class TestCommand implements Command {
    @Override
    public void execute(CommandArgumentList args) {
        File file = args.getArgument(0);
        //todo: check if terminal supports colors
        TestCounter testCounter = new TestCounter();
        if (file.isDirectory()) {
            System.out.println(ConsoleColors.GREEN + "Testing all scripts in \"" + file.getName() + "\"..." + ConsoleColors.RESET);
            testDir(file, testCounter);
            testCounter.print();
        } else {
            testScript(file, testCounter);
        }
    }

    private void testScript(File file, TestCounter testCounter) {
        testCounter.incrTests();
        PrintStream defaultOut = System.out;

        String className = file.getName().substring(0, file.getName().length() - ".xript".length());
        System.out.println(ConsoleColors.GREEN + "Testing script \"" + className + "\"..." + ConsoleColors.RESET);

        File outFile = new File(file.getParentFile(), className + ".out");
        if (!outFile.exists()) {
            System.out.println(ConsoleColors.RED + "No output file found for \"" + className + "\"" + ConsoleColors.RESET);
            testCounter.incrSkipped();
            return;
        }

        String expectedOutput = "";
        try (FileInputStream fileInputStream = new FileInputStream(outFile)) {
            expectedOutput = new String(fileInputStream.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String output = "";
        BuildContext buildContext = new BuildContext();
        try {
            Script script = buildContext.build(file, "Test" + className.toUpperCase(Locale.ROOT));
            Class<?> scriptClass = script.getScriptClass();

            ByteArrayOutputStream scriptOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(scriptOut));

            scriptClass.getDeclaredMethod("main").invoke(null);
            System.out.flush();

            System.setOut(defaultOut);

            output = scriptOut.toString();
        } catch (Throwable e) {
            e.printStackTrace();
        }


        if (output.replaceAll("\r\n", "\n").equals(expectedOutput.replaceAll("\r\n", "\n"))) {
            testCounter.incrSuccesses();
        } else {
            System.out.println(ConsoleColors.RED + "Output for \"" + className + "\" is not as expected" + ConsoleColors.RESET);
            System.out.println(ConsoleColors.YELLOW + "Expected: " + expectedOutput + ConsoleColors.RESET);
            System.out.println(ConsoleColors.YELLOW + "Output: " + output + ConsoleColors.RESET);
        }

    }

    private void testDir(File file, TestCounter testCounter) {
        for (File f : Objects.requireNonNull(file.listFiles((dir, name) -> name.endsWith(".xript")))) {
            if (f.isDirectory()) {
                testDir(f, testCounter);
            } else {
                testScript(f, testCounter);
            }
        }
    }

    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getDescription() {
        return "Tests scripts";
    }

    @Override
    public CommandArgumentListBuilder getArgs() {
        return new CommandArgumentListBuilder()
                .add(CommandArgumentType.FILE.of("script/dir"));
    }

    private static class TestCounter {
        private int tests = 0;
        private int successes = 0;
        private int skipped = 0;

        public void incrTests() {
            tests++;
        }

        public void incrSuccesses() {
            successes++;
        }

        public void print() {
            String header = ConsoleColors.RESET + "Tests: " + tests + " (";
            String body = "";
            if (successes > 0)
                body = appendBody(body, ConsoleColors.GREEN + successes + " successful" + ConsoleColors.RESET);
            if (skipped > 0)
                body = appendBody(body, ConsoleColors.YELLOW + skipped + " skipped" + ConsoleColors.RESET);
            int failed = tests - successes - skipped;
            if (failed > 0)
                body = appendBody(body, ConsoleColors.RED + failed + " failed" + ConsoleColors.RESET);
            System.out.println(header + body + ")");
        }

        private static String appendBody(String body, String s) {
            if (body.length() > 0) {
                body += ", ";
            }
            body += s;
            return body;
        }

        public void incrSkipped() {
            skipped++;
        }
    }
}
