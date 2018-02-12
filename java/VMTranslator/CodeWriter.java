import java.io.File;

public class CodeWriter {

    private File file;
    private String name;
    private String moduleName;
    private String functionName;

    public CodeWriter(String name) {
        // TODO: create/open file, set name
        this.name = name;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void writeInit(int n) {}

    public void writeArithm(int n, String operation) {}

    public void writePush(String segment, int index) {}

    public void writePop(String segment, int index) {}

    public void writeGoto(String labelName) {}

    public void writeLabel(String labelName) {}

    public void writeIfGoto(String labelName) {}

    public void writeReturn() {}

    public void writeCall(int n, int num) {}

    public void writeRestore() {}

    public void writeFunction(int num) {}

    public void close() {
        // TODO: close file
    }
}
