package pl.baluch.xconscript.data;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DataType {
    public static final DataType INT = new DataType("int", Opcodes.INTEGER, Type.INT_TYPE, int.class);
    public static final DataType LONG = new DataType("long", Opcodes.LONG, Type.LONG_TYPE, long.class);
    public static final DataType DOUBLE = new DataType("double", Opcodes.DOUBLE, Type.DOUBLE_TYPE, double.class);
    public static final DataType CHAR = new DataType("char", Opcodes.INTEGER, Type.INT_TYPE,  int.class);
    public static final DataType VOID = new DataType("void", -1, Type.VOID_TYPE,  void.class);
    public static final DataType STRING = new DataType("string", "java/lang/String", Type.getType("Ljava/lang/String;"), String.class);
    private static final Map<Class<?>, DataType> JAVA_TYPES = new HashMap<>();
    private static final Map<String, DataType> JAVA_ALIASES = new HashMap<>();

    private final String name;
    private final Object o;
    private final Type type;
    private final Class<?> typeClass;
    private static final DataType[] values = new DataType[]{INT, LONG, STRING, CHAR, VOID, DOUBLE};

    DataType(String name, Object o, Type type, Class<?> typeClass) {
        this.name = name;
        this.o = o;
        this.type = type;
        this.typeClass = typeClass;
    }

    public static DataType getByClass(Class<?> returnType) {
        //note: computational type 2 class and primitive cannot be merged
        if(returnType == long.class || returnType == Long.class) {
            return LONG;
        }
        if(returnType == Character.class || returnType == char.class) {
            return CHAR;
        }
        if(returnType == Integer.class || returnType == int.class) {
            return INT;
        }
        if(returnType == double.class) {
            return DOUBLE;
        }
        if(returnType == Void.class || returnType == void.class) {
            return VOID;
        }
        if(returnType == String.class) {
            return STRING;
        }
        if(!JAVA_TYPES.containsKey(returnType)){
            JAVA_TYPES.put(returnType, new DataType(returnType.getName(), returnType.getName().replace(".", "/"), Type.getType(returnType), returnType));
        }
        return JAVA_TYPES.get(returnType);
    }

    public static DataType getByType(Type type) throws Exception {
        for (DataType dt : values) {
            if (dt.type.equals(type)) {
                return dt;
            }
        }
        for (DataType value : JAVA_TYPES.values()) {
            if(value.type.equals(type)) {
                return value;
            }
        }
        throw new Exception("Unknown type: " + type);
    }

    public Object getObject() {
        return o;
    }

    public Type getASMType() {
        return type;
    }

    public static DataType getByName(ParseContext ctx, String name) {
        for (DataType dt : values) {
            if (dt.name.equalsIgnoreCase(name)) {
                return dt;
            }
        }
        if(JAVA_ALIASES.containsKey(name)) {
            return JAVA_ALIASES.get(name);
        }
        throw new RuntimeException("Unknown datatype: " + name);
    }

    public static void registerAlias(String alias, Class<?> className) {
        DataType dt = new DataType(alias, className.getName().replace(".", "/"), Type.getType(className), className);
        JAVA_ALIASES.put(alias, dt);
        JAVA_TYPES.put(className, dt);
    }

    public String getName() {
        return name;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public static boolean hasAliasDefined(String name){
        return JAVA_ALIASES.containsKey(name);
    }

    public static Class<?> getAliasClass(String name){
        return JAVA_ALIASES.get(name).getTypeClass();
    }

    public static boolean isType2(DataType dataType) {
        return dataType.equals(DOUBLE) || dataType.equals(LONG);
    }

    @Override
    public boolean equals(Object o1) {
        if (this == o1) return true;
        if (o1 == null || getClass() != o1.getClass()) return false;
        DataType dataType = (DataType) o1;
        return Objects.equals(name, dataType.name) && //todo: check this for internal types
                Objects.equals(o, dataType.o) &&
                Objects.equals(type, dataType.type) &&
                Objects.equals(typeClass, dataType.typeClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, o, type, typeClass);
    }
}
