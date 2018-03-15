import java.io.*;

class CompilationEngine {

    private JackTokenizer tokenizer;
    private PrintStream printStream;
    private SymbolTable symbolTable;

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
        if (tokenizer.getTokenType() != TokenType.SYMBOL) {
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
        if (tokenizer.getTokenType() != TokenType.INT_CONST) {
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
        if (tokenizer.getTokenType() != TokenType.STRING_CONST) {
            return false;
        }
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean compileKW(Keyword kw, String prefixTag) {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != TokenType.KEYWORD) {
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

    private boolean compileKW(Keyword kw) {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != TokenType.KEYWORD) {
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
        if (tokenizer.getTokenType() != TokenType.IDENTIFIER) {
            return false;
        }
        JackTokenizer.printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean testSymbol(char c) {
        return tokenizer.hasMoreTokens()
             && tokenizer.getTokenType() == TokenType.SYMBOL
             && tokenizer.getSymbol() == c;
    }

    /* class' className '{' classVarDec* subroutineDec* '}' */
    public boolean compileClass() {
        symbolTable = new SymbolTable();
        if (!compileKW(Keyword.CLASS, "<class>") || !compileIdentifier()
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
        return compileKW(Keyword.INT) || compileKW(Keyword.BOOLEAN)
          || compileKW(Keyword.CHAR) || compileIdentifier();
    }

    private String getIdent() {
        if (!tokenizer.hasMoreTokens()
        || tokenizer.getTokenType() != TokenType.IDENTIFIER) {
            return null;
        }
        return tokenizer.getIdentifier();
    }

    private Keyword getKW() {
        if (!tokenizer.hasMoreTokens()
        || tokenizer.getTokenType() != TokenType.KEYWORD) {
            return null;
        }
        return tokenizer.getKeyword();
    }

    private Kind makeKind() {
        switch (getKW()) {
        case STATIC:
            return Kind.STATIC;
        case FIELD:
            return Kind.FIELD;
        default:
            return null;
        }
    }

    private String makeType() {
        String ident = getIdent();
        if (ident != null) {
            return ident;
        }
        Keyword kw = getKW();
        if (kw == null) {
            return null;
        }
        switch (getKW()) {
        case INT:
            return "int";
        case BOOLEAN:
            return "boolean";
        case CHAR:
            return "char";
        default:
            return null;
        }
    }

    /* ('static' | 'field') type varName (',' varName)* */
    public boolean compileClassVarDec() {
        Kind kind = makeKind();
        if (!compileKW(Keyword.STATIC, "<classVarDec>")
          && !compileKW(Keyword.FIELD, "<classVarDec>")) {
            return false;
        }
        String type = makeType();
        if (!compileType()) {
            return false;
        }
        String name = getIdent();
        if (!compileIdentifier()) {
            return false;
        }
        symbolTable.define(name, type, kind);
        while (compileSymbol(',')) {
            name = getIdent();
            if (!compileIdentifier()) {
                return false;
            }
            symbolTable.define(name, type, kind);
        }
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
        if (!compileKW(Keyword.CONSTRUCTOR, "<subroutineDec>")
                && !compileKW(Keyword.FUNCTION, "<subroutineDec>")
                && !compileKW(Keyword.METHOD, "<subroutineDec>")
            || !compileType() && !compileKW(Keyword.VOID)) {
            return false;
        }
        symbolTable.startSubroutine();
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
        String type = makeType();
        if (compileType()) {
            String name = getIdent();
            if (!compileIdentifier()) {
                return false;
            }
            symbolTable.define(name, type, Kind.ARGUMENT);
            while (compileSymbol(',')) {
                type = makeType();
                if (!compileType()) {
                    return false;
                }
                name = getIdent();
                if (!compileIdentifier()) {
                    return false;
                }
                symbolTable.define(name, type, Kind.ARGUMENT);
            }
        }
        printStream.println("</parameterList>");
        return true;
    }

    /* 'var' type varName ( ',' varName)* ';' */
    public boolean compileVarDec() {
        if (!compileKW(Keyword.VAR, "<varDec>")) {
            return false;
        }
        String type = makeType();
        if (!compileType()) {
            return false;
        }
        String name = getIdent();
        if (!compileIdentifier()) {
            return false;
        }
        symbolTable.define(name, type, Kind.LOCAL);
        while (compileSymbol(',')) {
            name = getIdent();
            if (!compileIdentifier()) {
                return false;
            }
            symbolTable.define(name, type, Kind.LOCAL);
        }
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
        if (!compileKW(Keyword.DO, "<doStatement>")
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
        if (!compileKW(Keyword.LET, "<letStatement>")) {
            return false;
        }
        String name = getIdent();
        symbolTable.println(name);
        if (!compileIdentifier()) {
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
        if (!compileKW(Keyword.WHILE, "<whileStatement>")
          || !compileSymbol('(') || !compileExpression() || !compileSymbol(')')
          || !compileBody()) {
            return false;
        }
        printStream.println("</whileStatement>");
        return true;
    }

    /* 'return' expression? ';' */
    public boolean compileReturn() {
        if (!compileKW(Keyword.RETURN, "<returnStatement>")) {
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
        if (!compileKW(Keyword.IF, "<ifStatement>")
          || !compileSymbol('(') || !compileExpression() || !compileSymbol(')')
          || !compileBody()
          || compileKW(Keyword.ELSE) && !compileBody()) {
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
        if (!compileInt() && !compileStr()
          && !compileKW(Keyword.TRUE) && !compileKW(Keyword.FALSE)
          && !compileKW(Keyword.NULL) && !compileKW(Keyword.THIS)
          && !((compileSymbol('-') || compileSymbol('~')) && compileTerm())
          && !(compileSymbol('(') && compileExpression() && compileSymbol(')'))) {
            String ident = getIdent();
            if (!compileIdentifier()) {
                return false;
            }
            /* id | id '[' expr ']' | id '(' list ')' | id '.' id ' (' list ')' */
            if (tokenizer.hasMoreTokens()
               && tokenizer.getTokenType() == TokenType.SYMBOL) {
                switch (tokenizer.getSymbol()) {
                case '[':
                    symbolTable.println(ident);
                    if (!compileSymbol('[') || !compileExpression()
                      || !compileSymbol(']')) {
                        return false;
                    }
                    break;
                case '(':
                    symbolTable.println(ident);
                    if (!compileSymbol('(') || !compileExpressionList()
                      || !compileSymbol(')')) {
                        return false;
                    }
                    break;
                case '.':
                    if (!compileSymbol('.')) {
                        return false;
                    }
                    String subname = getIdent();
                    System.out.println(String.format("extern: %s.%s", ident, subname));
                    if (!compileIdentifier() || !compileSymbol('(')
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
