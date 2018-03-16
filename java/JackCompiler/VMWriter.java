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

    public void writeDefine(String name, String type, Kind kind, int index) {
        ps.println(String.format("// define %s: %s, %s[%d]",
                name, type, kind.toString(), index));
    }

    public void writeSubroutine(Keyword kw, String name, String type, int nArgs) {
        ps.println(String.format("// %s %s: %s", kw.toString(), name, type));
        ps.println(String.format("function %s %d", name, nArgs));
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

    public void writeReturn() {
        ps.println("return");
    }

    /* closes the output file */
    public void close() {
        ps.close();
    }
}
