package pl.baluch.xconscript.data;

import java.lang.reflect.Method;
import java.util.Map;

public abstract class StdLib {
    public abstract void globalMethods(Map<String, Method> map);
    public abstract void blockTypes(Map<String, BlockType> map);

    public Method getMethod(Class<?> owner, String methodName, Class<?>... args) {
        Method method = null;
        try {
            method = owner.getDeclaredMethod(methodName, args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        if (method == null) {
            throw new RuntimeException("Method not found: " + owner.getName() + "." + methodName);
        }

        return method;
    }
}
