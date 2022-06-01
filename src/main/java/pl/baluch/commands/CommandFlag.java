package pl.baluch.commands;

public enum CommandFlag {
    RUN_AFTER_COMPILE("-r", "run after compilation"),
    SAVE_TO_FILE("-s", "save to file"),
    DEBUG("-d", "debug mode"),
    ;

    private final String flag;
    private final String desc;

    CommandFlag(String flag, String desc) {
        this.flag = flag;
        this.desc = desc;
    }

    public String getFlag() {
        return flag;
    }

    public String getDescription() {
        return desc;
    }
}
