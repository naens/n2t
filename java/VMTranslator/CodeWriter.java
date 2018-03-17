import java.io.*;

public class CodeWriter {

    private String name;
    private PrintStream printStream;
    private String moduleName;
    private String functionName;
    private int n;

    public CodeWriter(File file) {
        try {
            printStream = new PrintStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        n = 0;
        functionName = "Sys.init";
        name = VMTranslator.getNoExtName(file);
    }

    public void incN() {
        n++;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void writeInit() {
        printStream.println("// init");
        printStream.println("@256");
        printStream.println("D=A");
        printStream.println("@SP");
        printStream.println("M=D");
        printStream.println();
        writeCall("Sys.init", 0);
    }

    private void writeUnary(String operation) {
        printStream.println("@SP");
        printStream.println("A=M-1");
        if (operation.equals("neg")) {
            printStream.println("M=-M");
        } else {
            printStream.println("M=!M");
        }
        printStream.println();
    }

    private void writeRelational(String operation) {
        printStream.println("D=M-D");
        printStream.println("@relat.mt." + moduleName + "." + n);
        printStream.println("D;J" + operation.toUpperCase());
        printStream.println("@SP");
        printStream.println("A=M-1");
        printStream.println("M=0");
        printStream.println("@relat.end." + moduleName + "." + n);
        printStream.println("0;JMP");
        printStream.println("(relat.mt." + moduleName + "." + n + ")");
        printStream.println("@SP");
        printStream.println("A=M-1");
        printStream.println("M=-1");
        printStream.println("(relat.end." + moduleName + "." + n + ")");
    }


    private void writeBinary(String operation) {
        printStream.println("@SP");
        printStream.println("M=M-1");
        printStream.println("A=M");
        printStream.println("D=M");
        printStream.println("A=A-1");
        switch (operation) {
            case "eq":
            case "gt":
            case "lt":
                writeRelational(operation);
                break;
            case "add":
                printStream.println("M=M+D");
                break;
            case "sub":
                printStream.println("M=M-D");
                break;
            case "and":
                printStream.println("M=M&D");
                break;
            case "or":
                printStream.println("M=M|D");
                break;
            default:
                break;
        }
        printStream.println();
    }

    public void writeArithm(String operation) {
        printStream.println("// arithm " + operation);
        if (operation.equals("neg") || operation.equals("not")) {
            writeUnary(operation);
        } else {
            writeBinary(operation);
        }
    }

    private String segmentConstant(String segment) {
        switch (segment) {
            case "local":
                return "LCL";
            case "argument":
                return "ARG";
            case "this":
                return "THIS";
            case "that":
                return "THAT";
            default:
                return null;
        }
    }

    private void writeVD(String segment, int index) {
        switch (segment) {
            case "temp":
                printStream.println("@5");
                printStream.println("D=A");
                printStream.println("@" + index);
                printStream.println("A=A+D");
                printStream.println("D=M");
                break;
            case "constant":
                printStream.println("@" + index);
                printStream.println("D=A");
                break;
            case "static":
                printStream.println("@" + moduleName + "." + index);
                printStream.println("D=M");
                break;
            case "pointer":
                if (index == 0) {
                    printStream.println("@THIS");
                } else {
                    printStream.println("@THAT");
                }
                printStream.println("D=M");
                break;
            default:
                printStream.println("@" + segmentConstant(segment));
                printStream.println("D=M");
                printStream.println("@" + index);
                printStream.println("A=A+D");
                printStream.println("D=M");
                break;
        }
    }

    private void writePushExpr(String expr) {
        printStream.println("@SP");
        printStream.println("A=M");
        printStream.println("M=" + expr);
        printStream.println("@SP");
        printStream.println("M=M+1");
    }

    public void writePush(String segment, int index) {
        printStream.println("// push " + segment + " " + index);
        writeVD(segment, index);
        writePushExpr("D");
        printStream.println();
    }

    private void writeDV(String segment, int index) {
        switch (segment) {
            case "static":
                printStream.println("@" + moduleName + "." + index);
                printStream.println("M=D");
                break;
            case "pointer":
                if (index == 0) {
                    printStream.println("@THIS");
                } else {
                    printStream.println("@THAT");
                }
                printStream.println("M=D");
                break;
            default:
                printStream.println("@R13");
                printStream.println("M=D");
                if (segment.equals("temp")) {
                    printStream.println("@5");
                    printStream.println("D=A");
                } else {
                    printStream.println("@" + segmentConstant(segment));
                    printStream.println("D=M");
                }
                printStream.println("@" + index);
                printStream.println("D=A+D");
                printStream.println("@R14");
                printStream.println("M=D");
                printStream.println("@R13");
                printStream.println("D=M");
                printStream.println("@R14");
                printStream.println("A=M");
                printStream.println("M=D");
                printStream.println();
                break;
        }
    }

    public void writePop(String segment, int index) {
        printStream.println("// pop " + segment + " " + index);
        printStream.println("@SP");
        printStream.println("M=M-1");
        printStream.println("A=M");
        printStream.println("D=M");
        writeDV(segment, index);
        printStream.println();
    }

    public void writeGoto(String labelName) {
        printStream.println("// goto " + labelName);
        printStream.println("@" + functionName + "$" + labelName);
        printStream.println("0;JMP");
        printStream.println();
    }

    public void writeLabel(String labelName) {
        printStream.println("// label " + labelName);
        printStream.println("(" + functionName + "$" + labelName + ")");
        printStream.println();
    }

    public void writeIfGoto(String labelName) {
        printStream.println("// if-goto " + labelName);
        printStream.println("@SP");
        printStream.println("M=M-1");
        printStream.println("A=M");
        printStream.println("D=M");
        printStream.println("@" + functionName + "$" + labelName);
        printStream.println("D;JNE");
        printStream.println();
    }

    public void writeReturn() {
        printStream.println("// return to " + name + "$restore");
        printStream.println("@" + name + "$restore");
        printStream.println("0;JMP");
        printStream.println();
    }

    public void writeCall(String func, int nVars) {
        printStream.println("// call " + func + " " + nVars);
        String returnLabel = func + "$ret." + n;

        /* push return address */
        printStream.println("@" + returnLabel);
        printStream.println("D=A");
        writePushExpr("D");

        /* push LCL */
        printStream.println("@LCL");
        printStream.println("D=M");
        writePushExpr("D");

        /* push ARG */
        printStream.println("@ARG");
        printStream.println("D=M");
        writePushExpr("D");

        /* push THIS */
        printStream.println("@THIS");
        printStream.println("D=M");
        writePushExpr("D");

        /* push THAT */
        printStream.println("@THAT");
        printStream.println("D=M");
        writePushExpr("D");

        /* LCL=SP */
        printStream.println("@SP");
        printStream.println("D=M");
        printStream.println("@LCL");
        printStream.println("M=D");

        /* ARG=SP-5-NumArgs */
        printStream.println("@5");
        printStream.println("D=D-A");        /* D=[LCL]-5 */
        printStream.println("@" + nVars);
        printStream.println("D=D-A");
        printStream.println("@ARG");
        printStream.println("M=D");

        /* goto function */
        printStream.println("@" + func);
        printStream.println("0;JMP");

        printStream.println("(" + returnLabel + ")");

        printStream.println();
    }

    public void writeEndLoop() {
        printStream.println("(" + name + "$end)");
        printStream.println("@" + name + "$end");
        printStream.println("0;JMP");
        printStream.println();
    }

    public void writeRestore() {
        printStream.println("(" + name + "$restore)");

        printStream.println("@ARG");    /* save ARG */
        printStream.println("D=M");
        printStream.println("@R14");
        printStream.println("M=D");

        printStream.println("@LCL");
        printStream.println("D=M-1");  /* D=[LCL]-1 */
        printStream.println("@R13");
        printStream.println("AM=D");   /* [R13]=[LCL]-1 */
        printStream.println("D=M");    /* D=[[LCL]-1] */
        printStream.println("@THAT");
        printStream.println("M=D");

        printStream.println("@R13");
        printStream.println("D=M-1");  /* D=[LCL]-2 */
        printStream.println("AM=D");        /* [R13]=[LCL]-2 */
        printStream.println("D=M");    /* D=[[LCL]-2] */
        printStream.println("@THIS");
        printStream.println("M=D");

        printStream.println("@R13");
        printStream.println("D=M-1");  /* D=[LCL]-3 */
        printStream.println("AM=D");   /* [R13]=[LCL]-3 */
        printStream.println("D=M");    /* D=[[LCL]-3] */
        printStream.println("@ARG");
        printStream.println("M=D");

        printStream.println("@R13");
        printStream.println("D=M-1");  /* D=[LCL]-4 */
        printStream.println("AM=D");   /* [R13]=[LCL]-4 */
        printStream.println("D=M");    /* D=[[LCL]-4] */
        printStream.println("@LCL");
        printStream.println("M=D");

        printStream.println("@R13");    /* save return address */
        printStream.println("A=M-1");
        printStream.println("D=M");     /* D=[[LCL]-5] */
        printStream.println("@R13");
        printStream.println("M=D");     /* [R13]=[[LCL]-15] */

        printStream.println("@SP");     /* set return value */
        printStream.println("A=M-1");
        printStream.println("D=M");
        printStream.println("@R14");
        printStream.println("A=M");
        printStream.println("M=D");     /* [[old-ARG]]=[[SP]-1] */

        printStream.println("@R14");    /* set [SP] after return value */
        printStream.println("D=M+1");
        printStream.println("@SP");
        printStream.println("M=D");    /* [SP]=[old-ARG]+1 */

        printStream.println("@R13");
        printStream.println("A=M");
        printStream.println("0;JMP");
        printStream.println();
    }

    public void writeFunction(String func, int nArgs) {
        functionName = func;
        printStream.println("// function " + functionName + " " + nArgs);
        printStream.println("(" + functionName + ")");

        for (int i = 0; i < nArgs; i++) {
            writePushExpr("0");
        }

        printStream.println();
    }

    public void close() {
        printStream.close();
    }
}
