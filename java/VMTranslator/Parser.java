import java.io.*;
import java.util.regex.*;

public class Parser {

    private FileReader fileReader;
    private BufferedReader bufferedReader;

    public Parser(File file) {
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] advance() {
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Pattern pattern = Pattern.compile("[^ ].*");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String s = matcher.group();
                    if (s.matches("[a-z].*")) {
                        return s.split("\\s+");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
