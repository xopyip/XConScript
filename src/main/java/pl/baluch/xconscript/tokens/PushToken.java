package pl.baluch.xconscript.tokens;

import pl.baluch.xconscript.data.TokenLocation;

public class PushToken<T> extends Token<T>{
    public PushToken(TokenLocation location,  String text, T value) {
        super(location, text, value);
    }
}
