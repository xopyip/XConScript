package pl.baluch.xconscript.tokens;

import pl.baluch.xconscript.data.TokenLocation;

public class Token<T> {
    public final TokenLocation location;
    public final String text;
    public final T value;

    public Token(TokenLocation location, String text, T value) {
        this.location = location;
        this.text = text;
        this.value = value;
    }

    public String toString() {
        return value.getClass().getSimpleName() + " " + text;
    }

    public T getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public TokenLocation getLocation() {
        return location;
    }
}
