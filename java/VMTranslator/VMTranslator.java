import java.io.*;

public class VMTranslator {

    public static String getNoExtName(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    public static void main(String[] args) throws IOException {
        boolean writeEnv;
        if (args.length != 1) {
            System.out.println("usage: java VMTranslator <file/dir>");
            return;
        }

        /* get file and dir */
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println(args[0] + " does not exist");
        }
        File dir;

        /* get file list and program name*/
        File[] inFiles;
        FilenameFilter vmFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".vm");
            }
        };
        String programName;
        if (file.isDirectory()) {
            programName = file.getName();
            inFiles = file.listFiles(vmFilter);
            dir = file.getAbsoluteFile();
        } else {
            inFiles = new File[1];
            inFiles[0] = file;
            programName = getNoExtName(file);
            dir = file.getAbsoluteFile().getParentFile();
        }
        writeEnv = new File(dir, "Sys.vm").exists();

        File outFile = new File(dir, programName + ".asm");
//        System.out.println("output file = " + outFile.getCanonicalPath());
        CodeWriter codeWriter = new CodeWriter(outFile);
        if (writeEnv) {
            codeWriter.writeInit();
        }
        for (File f : inFiles) {
            String moduleName = getNoExtName(f);
            codeWriter.setModuleName(moduleName);
            Parser parser = new Parser(f);
            String[] strs;
            while ((strs = parser.advance()) != null) {
                doCmd(codeWriter, strs);
            }
            parser.close();
        }
        codeWriter.writeEndLoop();
        codeWriter.writeRestore();
        codeWriter.close();
    }

    private static void doCmd(CodeWriter cw, String[] strs) {
        switch (strs[0]) {
        case "push":
            cw.writePush(strs[1], Integer.parseInt(strs[2]));
            break;
        case "pop":
            cw.writePop(strs[1], Integer.parseInt(strs[2]));
            break;
        case "label":
            cw.writeLabel(strs[1]);
            break;
        case "goto":
            cw.writeGoto(strs[1]);
            break;
        case "if-goto":
            cw.writeIfGoto(strs[1]);
            break;
        case "return":
            cw.writeReturn();
            break;
        case "call":
            cw.writeCall(strs[1], Integer.parseInt(strs[2]));
            break;
        case "function":
            cw.writeFunction(strs[1], Integer.parseInt(strs[2]));
            break;
        default:
            cw.writeArithm(strs[0]);
            break;
        }
        cw.incN();
    }

}
