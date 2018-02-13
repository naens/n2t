import java.io.*;

public class VMTranslator {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("usage: java VMTranslator <file/dir>");
            return;
        }

        /* get file and dir */
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println(args[0] + " does not exist");
        }
        File dir = file.getAbsoluteFile().getParentFile();

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
        } else {
            String name = file.getName();
            inFiles = new File[1];
            inFiles[0] = file;
            programName = name.substring(0, name.lastIndexOf('.'));
        }

        System.out.println("output file name = " + programName + ".asm");
        System.out.println("output file dir  = " + dir.getCanonicalPath());
        for (File f : inFiles) {
            Parser parser = new Parser(f);
            String[] strs;
            while ((strs = parser.advance()) != null) {
                System.out.println(String.join(" ", strs));
            }
            parser.close();
        }
    }
}
