import java.io.*;

class VMWriter {

    private PrintStream ps;

    public VMWriter(File file) {
        try {
            ps = new PrintStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writePush(Segment segment, int index) {
    }

    public void writePop(Segment segment, int index) {
    }

    public void writeArithmetic(Command command) {
    }

    public void writeLabel(String label) {
    }

    public void writeGoto(String label) {
    }

    public void writeIf(String label) {
    }

    public void writeCall(String name, int nArgs) {
    }

    public void writReturn() {
        ps.println("return");
    }

    /* closes the output file */
    public void close() {
    }

}
