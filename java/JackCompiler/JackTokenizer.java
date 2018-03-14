import java.io.*;

class JackTokenizer {

    private FileReader fileReader;
    private PushbackReader reader;
    private String stringVal;
    private int intVal;
    private char symbol;
    private boolean finished;
    private boolean valid;
    private TokenType tokenType;
    private Keyword keyword;

    /* Open the input file and gets ready to tokenize it */
    public JackTokenizer(File file) {
        try {
            fileReader = new FileReader(file);
            reader = new PushbackReader(fileReader);
            finished = false;
            valid = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Do we have more tokens in the input? */
    public boolean hasMoreTokens() {
        /* in order to answer, read one token in advance! */
        if (finished) {
            return false;
        }
        if (valid) {
            return true;
        }
        valid = readNext();
        return valid;
    }

    private static Keyword stringToKeyword(String s) {
        switch (s) {
        case "class":
            return Keyword.CLASS;
        case "method":
            return Keyword.METHOD;
        case "function":
            return Keyword.FUNCTION;
        case "constructor":
            return Keyword.CONSTRUCTOR;
        case "int":
        case "integer":
            return Keyword.INT;
        case "boolean":
            return Keyword.BOOLEAN;
        case "char":
            return Keyword.CHAR;
        case "void":
            return Keyword.VOID;
        case "var":
            return Keyword.VAR;
        case "static":
            return Keyword.STATIC;
        case "field":
            return Keyword.FIELD;
        case "let":
            return Keyword.LET;
        case "do":
            return Keyword.DO;
        case "if":
            return Keyword.IF;
        case "else":
            return Keyword.ELSE;
        case "while":
            return Keyword.WHILE;
        case "return":
            return Keyword.RETURN;
        case "true":
            return Keyword.TRUE;
        case "false":
            return Keyword.FALSE;
        case "null":
            return Keyword.NULL;
        case "this":
            return Keyword.THIS;
        default:
            return null;
        }
    }

    private static String keywordToString(Keyword keyword) {
        switch (keyword) {
        case CLASS:
            return "class";
        case METHOD:
            return "method";
        case FUNCTION:
            return "function";
        case CONSTRUCTOR:
            return "constructor";
        case INT:
            return "int";
        case BOOLEAN:
            return "boolean";
        case CHAR:
            return "char";
        case VOID:
            return "void";
        case VAR:
            return "var";
        case STATIC:
            return "static";
        case FIELD:
            return "field";
        case LET:
            return "let";
        case DO:
            return "do";
        case IF:
            return "if";
        case ELSE:
            return "else";
        case WHILE:
            return "while";
        case RETURN:
            return "return";
        case TRUE:
            return "true";
        case FALSE:
            return "false";
        case NULL:
            return "null";
        case THIS:
            return "this";
        default:
            return null;
        }
    }

    private void readCommentLine() {
        try {
            int ch = reader.read();
            while (ch != -1 && ch != '\n') {
                ch = reader.read();
            }
            if (ch == -1) {
                finished = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readCommentBlock() {
        try {
            int ch = reader.read();
            while (ch != -1) {
                if (ch == '*') {
                    ch = reader.read();
                    if (ch == -1) {
                        finished = true;
                        return;
                    }
                    if (ch == '/') {
                        return;
                    }
                    reader.unread(ch);
                }
                ch = reader.read();
            }
            if (ch == -1) {
                finished = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* read the string constant after a " and read the " */
    private String readStringConst() {
        StringBuffer result = new StringBuffer();
        try {
            int ch = reader.read();
            while (ch != -1 && ch != '"') {
                result.append((char)ch);
                ch = reader.read();
            }
            if (ch == -1) {
                finished = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /* read identifiers and keywords */
    private String readString(int chr) {
        int ch = chr;
        StringBuffer result = new StringBuffer();
        result.append((char)ch);
        try {
            ch = reader.read();
            while (ch != -1 && Character.isLetterOrDigit(ch)) {
                result.append((char)ch);
                ch = reader.read();
            }
            if (ch == -1) {
                finished = true;
            } else {
                reader.unread(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private int readInteger(int chr) {
        int ch = chr;
        int result = ch - '0';
        try {
            ch = reader.read();
            while (ch != -1 && Character.isDigit(ch)) {
                result *= 10;
                result += (ch - '0');
                ch = reader.read();
            }
            if (ch == -1) {
                finished = true;
            } else {
                reader.unread(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void advance() {
        valid = false;
    }

    /* read one token and return true if valid token */
    private boolean readNext() {
        if (finished) {
            return valid;
        }
        try {
            int ch = reader.read();
            while (!finished) {
                if (ch == '/') {
                    ch = reader.read();
                    switch (ch) {
                    case -1:
                        finished = true;
                        return false;
                    case '/':
                        readCommentLine();
                        break;
                    case '*':
                        readCommentBlock();
                        break;
                    default:
                        reader.unread(ch);
                        tokenType = TokenType.SYMBOL;
                        symbol = '/';
                        return true;
                    }
                } else if (ch == '"') {
                    stringVal = readStringConst();
                    tokenType = TokenType.STRING_CONST;
                    return true;
                } else if (Character.isLetter(ch)) {
                    String str = readString(ch);
                    keyword = stringToKeyword(str);
                    if (keyword != null) {
                        tokenType = TokenType.KEYWORD;
                    } else {
                        tokenType = TokenType.IDENTIFIER;
                        stringVal = str;
                    }
                    return true;
                } else if (Character.isDigit(ch)) {
                    tokenType = TokenType.INT_CONST;
                    intVal = readInteger(ch);
                    return true;
                } else if (ch == -1) {
                    finished = true;
                    return false;
                } else if (!Character.isWhitespace(ch)) {
                    tokenType = TokenType.SYMBOL;
                    symbol = (char)ch;
                    return true;
                } /* skip whitespace character and do nothig */
                if (!finished) {
                    ch = reader.read();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /* Return the type of the current token */
    public TokenType getTokenType() {
        return tokenType;
    }

    /* Return the keyword which is the current token
     * Should be called only when tokenType is KEYWORD */
    public Keyword getKeyword() {
        return keyword;
    }

    /* Return the character which is the current token.
     * Should be called only when tokenType is SYMBOL */
    public char getSymbol() {
        return symbol;
    }

    /* Return the identifier which is the current token
     * Should be called onlye when tokenType is IDENTIFIER */
    public String getIdentifier() {
        return stringVal;
    }

    /* Return the integer value of the current token
     * Should be called only when tokenType is INT_CONST */
    public int getIntVal() {
        return intVal;
    }

    /* Return the string value of the current token,
     * without the double quotes
     * Should be called only when tokenType is STRING_CONST */
    public String getStringVal() {
        return stringVal;
    }

    public void close() {
        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printXstr(PrintStream ps, String tag, String str) {
        ps.println(String.format("<%s>%s</%s>", tag, str, tag));
    }

    private static void printXint(PrintStream ps, String tag, int n) {
        ps.println(String.format("<%s>%d</%s>", tag, n, tag));
    }

    private String symbolToString(char symbol) {
        switch (symbol) {
        case '<':
            return "&lt;";
        case '>':
            return "&gt;";
        case '"':
            return "&quot;";
        case '&':
            return "&amp;";
        default:
            return Character.toString(symbol);
        }
    }

    public static void printCurrent(PrintStream ps, JackTokenizer tokenizer) {
        switch (tokenizer.getTokenType()) {
        case KEYWORD:
            printXstr(ps, "keyword", tokenizer.keywordToString(tokenizer.getKeyword()));
            break;
        case SYMBOL:
            printXstr(ps, "symbol",  tokenizer.symbolToString(tokenizer.getSymbol()));
            break;
        case IDENTIFIER:
            printXstr(ps, "identifier", tokenizer.getIdentifier());
            break;
        case INT_CONST:
            printXint(ps, "integerConstant", tokenizer.getIntVal());
            break;
        case STRING_CONST:
            printXstr(ps, "stringConstant", tokenizer.getStringVal());
            break;
        default:
            printXstr(ps, "unknown", tokenizer.getTokenType().toString());
            break;
        }
    }

    public static String getNoExtName(File file) {
        String name = file.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

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
            String name = getNoExtName(f);
            String outName = name + "T.xml";
            File outFile = new File(dir, outName);
            PrintStream printStream = new PrintStream(outFile);
            JackTokenizer tokenizer = new JackTokenizer(f);
            printStream.println("<tokens>");
            while (tokenizer.hasMoreTokens()) {
                tokenizer.advance();
                printCurrent(printStream, tokenizer);
            }
            printStream.println("</tokens>");
            printStream.close();
        }
    }
}
