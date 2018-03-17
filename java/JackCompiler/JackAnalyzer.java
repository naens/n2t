import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;

public class JackAnalyzer {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 1) {
            System.out.println("one argument required");
            return;
        }

        /* get file and dir */
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println(args[0] + " does not exist");
        }
        File dir;

        /* create list of files */
        File[] inFiles;
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jack");
            }
        };
        /* inFiles: files to read, dir: where to write the output files */
        if (file.isDirectory()) {
            inFiles = file.listFiles(filter);
            dir = file.getAbsoluteFile();
        } else {
            inFiles = new File[1];
            inFiles[0] = file;
            dir = file.getAbsoluteFile().getParentFile();
        }

        /* loop through files */
        for (File f : inFiles) {
            String name = JackTokenizer.getNoExtName(f);
//            String xmlName = name + ".xml";
            String vmName = name + ".vm";
//            File xmlFile = new File(dir, xmlName);
            File vmFile = new File(dir, vmName);
            CompilationEngine ce = new CompilationEngine(f, /*xmlFile, */vmFile);
            ce.start();
        }
    }
}
