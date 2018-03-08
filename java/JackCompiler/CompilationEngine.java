package jack;

import java.io.*;
import static jack.JackTokenizer.*;
import static jack.JackTokenizer.TokenType.*;
import static jack.JackTokenizer.Keyword.*;

class CompilationEngine {

    private JackTokenizer tokenizer;
    private PrintStream printStream;

    public CompilationEngine(File inFile, File outFile) {
        try {
            JackTokenizer tokenizer = new JackTokenizer(inFile);
            printStream = new PrintStream(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean ok() {
        return tokenizer.hasMoreTokens();
    }

    private JackTokenizer.TokenType type() {
        return tokenizer.getTokenType();
    }

    private JackTokenizer.Keyword kw() {
        if (tokenizer.getTokenType() != KEYWORD) {
            return null;
        }
        return tokenizer.getKeyword();
    }

    private char symbol() {
        if (tokenizer.getTokenType() != SYMBOL) {
            return 0;
        }
        return tokenizer.getSymbol();
    }

    public void start() {
        if (!ok() || kw() != CLASS) {
            return;
        }
        compileClass();
    }

    public void printXstr(String tag, String str) {
        printStream.println(String.format("<%s>%s</%s>", tag, str, tag));
    }

    private void printXint(String tag, int n) {
        printStream.println(String.format("<%s>%d</%s>", tag, n, tag));
    }

    private void compileChar(char c, boolean test) {
        if (!test || !ok()) {
            return;
        }
        if (symbol() != c) {
            return;
        }
        printCurrent(printStream, tokenizer);
    }

    private void compileKeyword(JackTokenizer.Keyword kw, boolean test) {
        if (!test || !ok()) {
            return;
        }
        if (kw() != kw) {
            return;
        }
        printCurrent(printStream, tokenizer);
    }

    private void compileIdentifier(boolean test) {
        if (!test || !ok()) {
            return;
        }
        if (type() != IDENTIFIER) {
            return;
        }
        printCurrent(printStream, tokenizer);


    }

    /* class' className '{' classVarDec* subroutineDec* '}' */
    public void compileClass() {
        printStream.println("<class>");
        /* identifier */
        compileIdentifier(true);

        /* '{' */
        compileChar('{', true);

        /* classVarDec* */
        while (ok() && (kw() == STATIC || kw() == FIELD)) {
            compileClassVarDec();
        }

        /* subroutineDec* */
        if (kw() == CONSTRUCTOR || kw() == FUNCTION || kw() == METHOD) {
            compileSubroutine();
        }
        while (ok() && (kw() == CONSTRUCTOR || kw() == FUNCTION || kw() == METHOD)) {
            compileSubroutine();
        }

        /* '}' */
        compileChar('}', false);
        printStream.println("</class>");
    }

    /* ('static' | 'field' ) */
    public void compileClassVarDec() {
        printStream.println("<classVarDec>");

        /* ('static' | 'field' ) */
        compileKeyword(STATIC, true);
        compileKeyword(FIELD, false);

        /* type */
        compileKeyword(INT, true);
        compileKeyword(BOOLEAN, false);
        compileKeyword(CHAR, false);

        /* varName */
        compileIdentifier(true);

        /* (',' varName)* */
        while (ok() && symbol() != ',') {
            printCurrent(printStream, tokenizer);
            compileIdentifier(true);
        }
        /* ';' */
        compileChar(';', true);
        printStream.println("</classVarDec>");
    }

    /* ('constructor' | 'function' | 'method')
     * ('void' | type)
     * subroutineName
     * '(' parameterList ')'
     *  subroutineBody */
    public void compileSubroutine() {
        printStream.println("<subRoutine>");

        /* ('constructor' | 'function' | 'method') */
        compileKeyword(CONSTRUCTOR, true);
        compileKeyword(FUNCTION, false);
        compileKeyword(METHOD, false);

        /* ('void' | type) */
        compileKeyword(INT, true);
        compileKeyword(BOOLEAN, false);
        compileKeyword(CHAR, false);
        compileKeyword(VOID, false);

        /* subroutineName */
        compileIdentifier(true);

        /* '(' parameterList ')' */
        compileChar('(', true);
        compileParameterList();
        compileChar(')', true);

        /* subroutineBody */
        compileStatements();
        printStream.println("</subRoutine>");
    }

    public void compileParameterList() {
        printStream.println("<parameterList>");
        printStream.println("</parameterList>");
    }

    public void compileVarDec() {
        printStream.println("<varDec>");
        printStream.println("</varDec>");
    }

    public void compileStatements() {
        printStream.println("<statements>");
        printStream.println("</statements>");
    }

    public void compileDo() {
        printStream.println("<do>");
        printStream.println("</do>");
    }

    public void compileLet() {
        printStream.println("<let>");
        printStream.println("</let>");
    }

    public void compileWhile() {
        printStream.println("<while>");
        printStream.println("</while>");
    }

    public void compileReturn() {
        printStream.println("<return>");
        printStream.println("</return>");
    }

    public void compileIf() {
        printStream.println("<if>");
        printStream.println("</if>");
    }
    
    public void compileExpression() {
        printStream.println("<expression>");
        printStream.println("</expression>");
    }

    public void compileTerm() {
        printStream.println("<term>");
        printStream.println("</term>");
    }

    public void compileExpressionList() {
        printStream.println("<expressionList>");
        printStream.println("</expressionList>");
    }
    
}
