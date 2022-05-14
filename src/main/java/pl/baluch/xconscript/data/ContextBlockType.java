package pl.baluch.xconscript.data;

import pl.baluch.xconscript.tokens.Token;

import java.util.List;

public abstract class ContextBlockType<T> {
    private final String name;

    public ContextBlockType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract T parse(List<Token<?>> tokens) throws Exception;

}
