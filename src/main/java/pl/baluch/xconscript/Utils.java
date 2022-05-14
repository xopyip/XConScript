package pl.baluch.xconscript;

import pl.baluch.xconscript.data.DataType;
import pl.baluch.xconscript.data.Block;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static boolean checkMethod(Method method, DataType[] params, String name) {
        if (!method.getName().equals(name)) {
            return false;
        }
        if (method.getParameterCount() != params.length) {
            return false;
        }
        for (int i = 0; i < params.length; i++) {
            if (params[0] == DataType.INT) {
                if (method.getParameterTypes()[i] != int.class && method.getParameterTypes()[i] != Integer.class) {
                    return false;
                }
            } else if (params[0] == DataType.STRING) {
                if (method.getParameterTypes()[i] != String.class) {
                    return false;
                }
            } else if (params[0] == DataType.DOUBLE) {
                if (method.getParameterTypes()[i] != double.class && method.getParameterTypes()[i] != Double.class) {
                    return false;
                }
            } else if (params[0] == DataType.CHAR) {
                if (method.getParameterTypes()[i] != char.class && method.getParameterTypes()[i] != Character.class) {
                    return false;
                }
            } else if (params[0].getTypeClass() != method.getParameterTypes()[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkSelfMethod(Block callMethod, DataType[] args) {
        for (int i = 0; i < callMethod.getArgumentTypes().length; i++) {
            if(!callMethod.getArgumentTypes()[i].equals(args[i].getASMType())) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkConstructor(Constructor<?> constructor, DataType[] params) {
        if (constructor.getParameterCount() != params.length) {
            return false;
        }
        for (int i = 0; i < params.length; i++) {
            if (params[0] == DataType.INT) {
                if (constructor.getParameterTypes()[i] != int.class && constructor.getParameterTypes()[i] != Integer.class) {
                    return false;
                }
            } else if (params[0] == DataType.STRING) {
                if (constructor.getParameterTypes()[i] != String.class) {
                    return false;
                }
            } else if (params[0] == DataType.DOUBLE) {
                if (constructor.getParameterTypes()[i] != double.class && constructor.getParameterTypes()[i] != Double.class) {
                    return false;
                }
            } else if (params[0] == DataType.CHAR) {
                if (constructor.getParameterTypes()[i] != char.class && constructor.getParameterTypes()[i] != Character.class) {
                    return false;
                }
            } else if (params[0].getTypeClass() != constructor.getParameterTypes()[i]) {
                return false;
            }
        }
        return true;
    }

    public static List<String> loadScriptFile(File file) throws FileNotFoundException {
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        return reader.lines().collect(Collectors.toList());
    }
}
