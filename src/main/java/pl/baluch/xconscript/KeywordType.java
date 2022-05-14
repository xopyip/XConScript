package pl.baluch.xconscript;

public enum KeywordType {
    THEN("then"),
    ELSE("else"),

    WHILE("while"),
    DO("do"),

    END("end"),

    LOG("log"),
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    POW("**"),
    MOD("%"),

    EQ("="),
    NEQ("!="),
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),

    SWAP("swap"),
    DUP("dup"),
    DROP("drop"),
    DROP2("drop2"),

    VAR("var"),
    IMPORT("import"),
    DUMP("???"),
    ;

    private final String name;

    KeywordType(String name) {
        this.name = name;
    }

    public static KeywordType getType(String word) {
        for (KeywordType type : KeywordType.values()) {
            if (type.name.equalsIgnoreCase(word)) {
                return type;
            }
        }
        return null;
    }
}
