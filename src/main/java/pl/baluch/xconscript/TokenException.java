package pl.baluch.xconscript;

import pl.baluch.xconscript.tokens.Token;

public class TokenException extends Exception {
    public TokenException(Token<?> token, String message) {
        super(token.getLocation() + ": " +message);
    }
}
