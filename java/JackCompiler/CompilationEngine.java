import java.io.*;

class CompilationEngine {

    private final JackTokenizer.Keyword CLASS = JackTokenizer.Keyword.CLASS;
    private final JackTokenizer.Keyword INT = JackTokenizer.Keyword.INT;
    private final JackTokenizer.Keyword BOOLEAN = JackTokenizer.Keyword.BOOLEAN;
    private final JackTokenizer.Keyword CHAR = JackTokenizer.Keyword.CHAR;
    private final JackTokenizer.Keyword STATIC = JackTokenizer.Keyword.STATIC;
    private final JackTokenizer.Keyword FIELD = JackTokenizer.Keyword.FIELD;
    private final JackTokenizer.Keyword CONSTRUCTOR = JackTokenizer.Keyword.CONSTRUCTOR;
    private final JackTokenizer.Keyword FUNCTION = JackTokenizer.Keyword.FUNCTION;
    private final JackTokenizer.Keyword METHOD = JackTokenizer.Keyword.METHOD;
    private final JackTokenizer.Keyword VOID = JackTokenizer.Keyword.VOID;
    private final JackTokenizer.Keyword VAR = JackTokenizer.Keyword.VAR;
    private final JackTokenizer.Keyword DO = JackTokenizer.Keyword.DO;
    private final JackTokenizer.Keyword LET = JackTokenizer.Keyword.LET;
    private final JackTokenizer.Keyword WHILE = JackTokenizer.Keyword.WHILE;
    private final JackTokenizer.Keyword RETURN = JackTokenizer.Keyword.RETURN;
    private final JackTokenizer.Keyword IF = JackTokenizer.Keyword.IF;
    private final JackTokenizer.Keyword ELSE = JackTokenizer.Keyword.ELSE;
    private final JackTokenizer.Keyword TRUE = JackTokenizer.Keyword.TRUE;
    private final JackTokenizer.Keyword FALSE = JackTokenizer.Keyword.FALSE;
    private final JackTokenizer.Keyword NULL = JackTokenizer.Keyword.NULL;
    private final JackTokenizer.Keyword THIS = JackTokenizer.Keyword.THIS;
    private final JackTokenizer.TokenType SYMBOL = JackTokenizer.TokenType.SYMBOL;

    private JackTokenizer tokenizer;
    private PrintStream printStream;

    public CompilationEngine(File inFile, File outFile) {
        try {
            tokenizer = new JackTokenizer(inFile);
            printStream = new PrintStream(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (!compileClass()) {
            System.out.println("error");
        }
        printStream.close();
    }

    private boolean compileSymbol(char c) {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != JackTokenizer.TokenType.SYMBOL) {
            return false;
        }
        if (tokenizer.getSymbol() != c) {
            return false;
        }
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    public boolean compileInt() {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != JackTokenizer.TokenType.INT_CONST) {
            return false;
        }
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    public boolean compileStr() {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != JackTokenizer.TokenType.STRING_CONST) {
            return false;
        }
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean compileKW(JackTokenizer.Keyword kw, String prefixTag) {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != JackTokenizer.TokenType.KEYWORD) {
            return false;
        }
        if (tokenizer.getKeyword() != kw) {
            return false;
        }
        printStream.println(prefixTag);
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean compileKW(JackTokenizer.Keyword kw) {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != JackTokenizer.TokenType.KEYWORD) {
            return false;
        }
        if (tokenizer.getKeyword() != kw) {
            return false;
        }
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean compileIdentifier() {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != JackTokenizer.TokenType.IDENTIFIER) {
            return false;
        }
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean testSymbol(char c) {
        return tokenizer.hasMoreTokens() && tokenizer.getTokenType() == SYMBOL
                && tokenizer.getSymbol() == c;
    }

    /* class' className '{' classVarDec* subroutineDec* '}' */
    public boolean compileClass() {
        if (!compileKW(CLASS, "<class>") || !compileIdentifier()
                || !compileSymbol('{')) {
            return false;
        }
        while (compileClassVarDec());
        while (compileSubroutine());
        if (!compileSymbol('}')) {
            return false;
        }
        printStream.println("</class>");
        return true;
    }

    public boolean compileType() {
        return compileKW(INT) || compileKW(BOOLEAN) || compileKW(CHAR) || compileIdentifier();
    }

    /* ('static' | 'field') type varName (',' varName)* */
    public boolean compileClassVarDec() {
        if (!compileKW(STATIC, "<classVarDec>") && !compileKW(FIELD, "<classVarDec>")) {
            return false;
        }
        if (!compileType() || !compileIdentifier()) {
            return false;
        }
        while (compileSymbol(',') && compileIdentifier());
        if (!compileSymbol(';')) {
            return false;
        }
        printStream.println("</classVarDec>");
        return true;
    }

    public boolean compileStatements() {
        printStream.println("<statements>");
        while (compileStatement());
        printStream.println("</statements>");
        return true;
    }

    public boolean compileBody(String tag) {
        printStream.println(String.format("<%s>", tag));
        if (!compileSymbol('{')) {
            return false;
        }
        while (compileVarDec());
        if (!compileStatements() || !compileSymbol('}')) {
            return false;
        }
        printStream.println(String.format("</%s>", tag));
        return true;
    }

    public boolean compileBody() {
        if (!compileSymbol('{')) {
            return false;
        }
        while (compileVarDec());
        return compileStatements() && compileSymbol('}');
    }

    /* ('constructor' | 'function' | 'method')
     * ('void' | type)
     * subroutineName
     * '(' parameterList ')'
     *  subroutineBody */
    public boolean compileSubroutine() {
        if (!compileKW(CONSTRUCTOR, "<subroutineDec>")
                && !compileKW(FUNCTION, "<subroutineDec>")
                && !compileKW(METHOD, "<subroutineDec>")
            || !compileType() && !compileKW(VOID)) {
            return false;
        }
        if (!compileIdentifier()) {
            return false;
        }
        if (!compileSymbol('(') || !compileParameterList() || !compileSymbol(')')) {
            return false;
        }
        if (!compileBody("subroutineBody")) {
            return false;
        }
        printStream.println("</subroutineDec>");
        return true;
    }

    /* ((type varName) ( ',' type varName)*)? */
    public boolean compileParameterList() {
        printStream.println("<parameterList>");
        if (compileType()) {
            if (!compileIdentifier()) {
                return false;
            }
            while (compileSymbol(',') && compileType() && compileIdentifier());
        }
        printStream.println("</parameterList>");
        return true;
    }

    /* 'var' type varName ( ',' varName)* ';' */
    public boolean compileVarDec() {
        if (!compileKW(VAR, "<varDec>") || !compileType() || !compileIdentifier()) {
            return false;
        }
        while (compileSymbol(',') && compileIdentifier());
        if (!compileSymbol(';')) {
            return false;
        }
        printStream.println("</varDec>");
        return true;
    }

    /* letStatement | ifStatement | whileStatement
     * | doStatement | returnStatement */
    public boolean compileStatement() {
        return compileLet() || compileIf() || compileWhile()
            || compileDo() || compileReturn();
    }

    /* 'do' id ('.' id) ? '(' expressionlist ')' ';' */
    public boolean compileDo() {
        if (!compileKW(DO, "<doStatement>")
          || !compileIdentifier()
          || compileSymbol('.') && !compileIdentifier()
          || !compileSymbol('(') || !compileExpressionList()
          || !compileSymbol(')') || !compileSymbol(';')) {
            return false;
        }
        printStream.println("</doStatement>");
        return true;
    }

    /* 'let' varName ( '[' expression ']' )? '=' expression ';' */
    public boolean compileLet() {
        if (!compileKW(LET, "<letStatement>") || !compileIdentifier()) {
            return false;
        }
        if (compileSymbol('[')
          && (!compileExpression() || !compileSymbol(']'))) {
            return false;
        }
        if (!compileSymbol('=') || !compileExpression() || !compileSymbol(';')) {
            return false;
        }
        printStream.println("</letStatement>");
        return true;
    }

    /* 'while' '(' expression ')' '{' statements '}' */
    public boolean compileWhile() {
        if (!compileKW(WHILE, "<whileStatement>")
          || !compileSymbol('(') || !compileExpression() || !compileSymbol(')')
          || !compileBody()) {
            return false;
        }
        printStream.println("</whileStatement>");
        return true;
    }

    /* 'return' expression? ';' */
    public boolean compileReturn() {
        if (!compileKW(RETURN, "<returnStatement>")) {
            return false;
        }
        if (!testSymbol(';')) {
            compileExpression();
        }
        if (!compileSymbol(';')) {
            return false;
        }
        printStream.println("</returnStatement>");
        return true;
    }

    /* 'if' '(' expression ')' '{' statements '}'
     * ( 'else' '{' statements '}' )? */
    public boolean compileIf() {
        if (!compileKW(IF, "<ifStatement>")
          || !compileSymbol('(') || !compileExpression() || !compileSymbol(')')
          || !compileBody()
          || compileKW(ELSE) && !compileBody()) {
            return false;
        }
        printStream.println("</ifStatement>");
        return true;
    }

    public boolean compileOp() {
        return compileSymbol('+') || compileSymbol('-') || compileSymbol('*')
          || compileSymbol('/') || compileSymbol('&') || compileSymbol('|')
          || compileSymbol('<') || compileSymbol('>') || compileSymbol('=');
    }
    
    /* term (op term)* */
    public boolean compileExpression() {
        printStream.println("<expression>");
        if (!compileTerm()) {
            return false;
        }
        while (compileOp() && compileTerm());
        printStream.println("</expression>");
        return true;
    }

    /* integerConstant | stringConstant | keywordConstant
     * | varName | varName '[' expression ']' | subroutineCall
     * | '(' expression ')' | unaryOp term */
    public boolean compileTerm() {
        printStream.println("<term>");
        if (!compileInt() && !compileStr() && !compileKW(TRUE) && !compileKW(FALSE)
          && !compileKW(NULL) && !compileKW(THIS)
          && !((compileSymbol('-') || compileSymbol('~')) && compileTerm())
          && !(compileSymbol('(') && compileExpression() && compileSymbol(')'))) {
            if (!compileIdentifier()) {
                return false;
            }
            /* id | id '[' expr ']' | id '(' list ')' | id '.' id ' (' list ')' */
            if (tokenizer.hasMoreTokens() && tokenizer.getTokenType() == SYMBOL) {
                switch (tokenizer.getSymbol()) {
                case '[':
                    if (!compileSymbol('[') || !compileExpression()
                      || !compileSymbol(']')) {
                        return false;
                    }
                    break;
                case '(':
                    if (!compileSymbol('(') || !compileExpressionList()
                      || !compileSymbol(')')) {
                        return false;
                    }
                    break;
                case '.':
                    if (!compileSymbol('.') || !compileIdentifier()
                      || !compileSymbol('(')
                      || !compileExpressionList() || !compileSymbol(')')) {
                        return false;
                    }
                    break;
                default:
                    /* it's ok, there's nothing to do */
                    break;
                }
            } /* else: nothing to do */
        }
        printStream.println("</term>");
        return true;
    }

    /* (expression ( ',' expression)* )? */
    public boolean compileExpressionList() {
        printStream.println("<expressionList>");
        if (testSymbol(')')) {
            printStream.println("</expressionList>");
            return true;
        }
        if (compileExpression()) {
            while (compileSymbol(',') && compileExpression());
        }
        printStream.println("</expressionList>");
        return true;
    }
}
