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
        ps.println(String.format("push %s %d", segment.toString(), index));
    }

    public void writePop(Segment segment, int index) {
        ps.println(String.format("pop %s %d", segment.toString(), index));
    }

    public void writeArithmetic(Command command) {
        ps.println(command.toString());
    }

    public void writeLabel(String label) {
        ps.println(String.format("label %s", label));
    }

    public void writeGoto(String label) {
        ps.println(String.format("goto %s", label));
    }

    public void writeIf(String label) {
        ps.println(String.format("if-goto %s", label));
    }

    public void writeCall(String name, int nArgs) {
        ps.println(String.format("call %s %d", name, nArgs));
    }

    public void writReturn() {
        ps.println("return");
    }

    /* closes the output file */
    public void close() {
        ps.close();
    }

}
