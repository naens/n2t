import java.io.*;

public class CodeWriter {

    private File file;
    private FileWriter fileWriter;
    private String moduleName;
    private String functionName;
    private int n;

    public CodeWriter(File file) {
        // TODO: create/open file, set name
        this.file = file;
        try {
            fileWriter = new FileWriter(file);
        } catch (IOException e) {
	    e.printStackTrace();
        }
        n = 0;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void writeInit(int n) {}

    public void writeArithm(String operation) {}

    public void writePush(String segment, int index) {}

    public void writePop(String segment, int index) {}

    public void writeGoto(String labelName) {}

    public void writeLabel(String labelName) {}

    public void writeIfGoto(String labelName) {}

    public void writeReturn() {}

    public void writeCall(String func, int nVars) {}

    public void writeRestore() {}

    public void writeFunction(String func, int nArgs) {}

    public void close() {
        try {
            fileWriter.close();
        } catch (IOException e) {
	    e.printStackTrace();
        }
    }
}
