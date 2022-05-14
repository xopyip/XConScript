package pl.baluch.xconscript.data;

public record TokenLocation(String filename, int line, int position) {

    @Override
    public String toString() {
        return filename + ":" + line + ":" + position;
    }
}
