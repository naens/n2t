import java.io.File;

public class Parser {

    private File file;
    private String cmd;
    private String arg1;
    private String arg2;

    public String getCmd() {
        return cmd;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public Parser(String fileName) {
        // TODO: open file
    }

    public void advance() {
        //TODO: read one line of code and set cmd, arg1 and arg2
    }

    public void close() {
        // TODO: close file
    }
}
