package pl.baluch.xconscript;

import java.util.HashMap;
import java.util.Map;

public class ScriptClassLoader extends ClassLoader {
    private final Map<String, Class<?>> scripts = new HashMap<>();

    public Class<?> findClass(String name) throws ClassNotFoundException {
        if(!scripts.containsKey(name)){
            throw new ClassNotFoundException();
        }
        return scripts.get(name);
    }

    public void loadScript(String name, byte[] bytes) {
        this.scripts.put(name, defineClass(name, bytes, 0, bytes.length));
    }
}
