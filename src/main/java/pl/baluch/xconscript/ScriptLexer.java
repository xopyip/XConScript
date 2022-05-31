package pl.baluch.xconscript;

import org.apache.commons.lang3.StringEscapeUtils;
import pl.baluch.xconscript.data.TokenLocation;
import pl.baluch.xconscript.tokens.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class ScriptLexer {

    public List<Token<?>> tokenize(File file) throws FileNotFoundException {
        List<String> lines = Utils.loadScriptFile(file);
        List<Token<?>> tokens = new ArrayList<>();

        List<StringLocation> words = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int commentIndex = line.lastIndexOf("//");
            while(commentIndex != -1) {
                String lineCandidate = line.substring(0, commentIndex);
                if(lineCandidate.chars().filter(c -> c == '"').count() % 2 == 0){ // comment is not inside string
                    line = lineCandidate;
                }
                commentIndex = line.lastIndexOf("//", commentIndex - 1);
            }
            for (int x = line.indexOf(line.trim()); x < line.length(); x = line.indexOf(" ", x + 1) + 1) {
                int nextSpace = line.indexOf(" ", x + 1);
                if (nextSpace == -1) {
                    String word = line.substring(x).trim();
                    if (word.length() == 0) {
                        break;
                    }
                    words.add(new StringLocation(line.substring(x), new TokenLocation(file.getAbsolutePath(), i + 1, x + 1)));
                    break;
                } else {
                    String word = line.substring(x, nextSpace).trim();
                    if (word.length() == 0) {
                        continue;
                    }
                    words.add(new StringLocation(line.substring(x, nextSpace), new TokenLocation(file.getAbsolutePath(), i + 1, x + 1)));
                }
            }
        }
        while (!words.isEmpty()) {
            StringLocation stringLocation = words.remove(0);
            String word = stringLocation.string();
            if (word.equals("")) {
                continue;
            }
            if (word.matches("'\\\\?.'")) {
                String substring = word.substring(1, word.length() - 1);
                String s = StringEscapeUtils.unescapeJava(substring);
                if (s.length() > 1) {
                    throw new RuntimeException("Invalid char: " + substring);
                }
                tokens.add(new PushToken<>(stringLocation.location(), word, s.charAt(0)));
            } else if (word.matches("-?(0|([1-9][0-9]*))")) {
                tokens.add(new PushToken<>(stringLocation.location(), word, Integer.parseInt(word)));
            } else if (word.matches("-?(0|([1-9][0-9]*))\\.[0-9]*")) {
                tokens.add(new PushToken<>(stringLocation.location(), word, Double.parseDouble(word)));
            } else if (word.startsWith("\"")) {
                StringBuilder str = new StringBuilder(word);
                if (!word.endsWith("\"") || word.length() == 1) {
                    while (!words.isEmpty() && !words.get(0).string().endsWith("\"")) {
                        str.append(" ").append(words.remove(0).string());
                    }
                    if (words.isEmpty()) {
                        throw new RuntimeException("Unclosed string");
                    }
                    str.append(" ").append(words.remove(0).string());
                }
                String res = StringEscapeUtils.unescapeJava(str.substring(1, str.length() - 1));
                tokens.add(new PushToken<>(stringLocation.location(), word, res));
            } else {
                tokens.add(new WordToken(stringLocation.location(), word, word));
            }
        }

        return tokens;
    }

    private record StringLocation(String string, TokenLocation location) {

        @Override
            public String toString() {
                return location.toString() + ": " + string;
            }
        }
}
