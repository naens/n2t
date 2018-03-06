import java.io.*;

class JackTokenizer {

    public enum TokenType {
        KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST
    }

    public enum Keyword {
        CLASS, METHOD, FUNCTION, CONSTRUCTOR,
        INT, BOOLEAN, CHAR, VOID, VAR, STATIC, FIELD, LET, DO,
        IF, ELSE, WHILE, RETURN, TRUE, FALSE, NULL, THIS
    }

    private File file;
    private FileReader fileReader;
    private PushbackReader reader;
    private String stringVal;
    private int intVal;
    private char symbol;
    private boolean finished;
    private TokenType tokenType;
    private Keyword keyword;

    /* Open the input file and gets ready to tokenize it */
    public JackTokenizer(File file) {
        try {
            this.file = file;
            fileReader = new FileReader(file);
            reader = new PushbackReader(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Do we have more tokens in the input? */
    public boolean hasMoreTokens() {
        return !finished;
    }

    private Keyword stringToKeyword(String s) {
        switch (s) {
        case "class":
            return Keyword.CLASS;
        case "method":
            return Keyword.METHOD;
        case "function":
            return Keyword.FUNCTION;
        case "constructor":
            return Keyword.CONSTRUCTOR;
        case "integer":
            return Keyword.INT;
        case "boolean":
            return Keyword.BOOLEAN;
        case "char":
            return Keyword.CHAR;
        case "void":
            return Keyword.VOID;
        case "VAR":
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
    private String readString(int ch) {
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

    private int readInteger(int ch) {
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

    /* Get the next token from the input
     * Should be called only if hasMoreTokens() returns true */
    public void advance() {
        try {
            int ch = reader.read();
            while (!finished) {
                if (ch == '/') {
                    ch = reader.read();
                    switch (ch) {
                    case -1:
                        finished = true;
                        return;
                    case '/':
                        readCommentLine();
                        break;
                    case '*':
                        readCommentBlock();
                        break;
                    default:
                        reader.unread(ch);
                        tokenType = TokenType.SYMBOL;
                        symbol = (char)ch;
                        return;
                    }
                } else if (ch == '"') {
                    stringVal = readStringConst();
                    tokenType = TokenType.STRING_CONST;
                    return;
                } else if (Character.isWhitespace(ch)) {
                    /* skip and do nothig */
                } else if (Character.isLetter(ch)) {
                    String str = readString(ch);
                    keyword = stringToKeyword(str);
                    if (keyword != null) {
                        tokenType = TokenType.KEYWORD;
                    } else {
                        tokenType = TokenType.IDENTIFIER;
                        stringVal = str;
                    }
                    return;
                } else if (Character.isDigit(ch)) {
                    tokenType = TokenType.INT_CONST;
                    intVal = readInteger(ch);
                    return;
                } else if (ch == -1) {
                    finished = true;
                    return;
                } else {
                    tokenType = TokenType.SYMBOL;
                    symbol = (char)ch;
                    return;
                }
                if (!finished) {
                    ch = reader.read();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
