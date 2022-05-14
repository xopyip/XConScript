package pl.baluch.xconscript.tokens;

import pl.baluch.xconscript.data.TokenLocation;

public class WordToken extends Token<String> {

    public WordToken(TokenLocation location, String text, String value) {
        super(location, text, value);
    }
}
