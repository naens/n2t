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
            tokenizer = new JackTokenizer(inFile);
            printStream = new PrintStream(outFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean start() {
        return compileClass();
    }

    private boolean compileSymbol(char c) {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != SYMBOL) {
            return false;
        }
        if (tokenizer.getSymbol() != c) {
            return false;
        }
        printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    public boolean compileInt() {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != INT_CONST) {
            return false;
        }
        printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    public boolean compileStr() {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != STRING_CONST) {
            return false;
        }
        printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean compileKW(Keyword kw) {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != KEYWORD) {
            return false;
        }
        if (tokenizer.getKeyword() != kw) {
            return false;
        }
        printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    private boolean compileIdentifier() {
        if (!tokenizer.hasMoreTokens()) {
            return false;
        }
        if (tokenizer.getTokenType() != IDENTIFIER) {
            return false;
        }
        printCurrent(printStream, tokenizer);
        tokenizer.advance();
        return true;
    }

    /* class' className '{' classVarDec* subroutineDec* '}' */
    public boolean compileClass() {
        printStream.println("<class>");
        if (!compileIdentifier() || !compileSymbol('{')) {
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
        return compileKW(INT) || compileKW(BOOLEAN) ||compileKW(CHAR);
    }

    /* ('static' | 'field') type varName (',' varName)* */
    public boolean compileClassVarDec() {
        printStream.println("<classVarDec>");
        if (!compileKW(STATIC) && !compileKW(FIELD)) {
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

    public boolean compileBody() {
        if (!compileSymbol('{')) {
            return false;
        }
        while (compileStatement());
        return compileSymbol('}');
    }

    /* ('constructor' | 'function' | 'method')
     * ('void' | type)
     * subroutineName
     * '(' parameterList ')'
     *  subroutineBody */
    public boolean compileSubroutine() {
        printStream.println("<subRoutine>");
        if (!compileKW(CONSTRUCTOR) && !compileKW(FUNCTION) && !compileKW(METHOD)
            || !compileType() && !compileKW(VOID)) {
            return false;
        }
        if (!compileIdentifier()) {
            return false;
        }
        if (!compileSymbol('(') || !compileParameterList() || compileSymbol(')')) {
            return false;
        }
        if (!compileBody()) {
            return false;
        }
        printStream.println("</subRoutine>");
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
        printStream.println("<varDec>");
        if (!compileKW(VAR) || !compileType() || !compileIdentifier()) {
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

    /* 'do' id ('.' id) ? '(' exprlst ')' ';' */
    public boolean compileDo() {
        printStream.println("<doStatement>");
        if (!compileKW(DO)) {
            return false;
        }
        if (compileSymbol('.') && !compileIdentifier()) {
            return false;
        }
        if (!compileSymbol('(') || !compileExpressionList()
         || !compileSymbol(')') || !compileSymbol(';')) {
            return false;
        }
        printStream.println("</doStatement>");
        return true;
    }

    /* 'let' varName ( '[' expression ']' )? '=' expression ';' */
    public boolean compileLet() {
        printStream.println("<letStatement>");
        if (!compileKW(LET) || !compileIdentifier()) {
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
        printStream.println("<whileStatement>");
        if (!compileKW(WHILE)
          || !compileSymbol('(') || !compileExpression() || !compileSymbol(')')
          || !compileBody()) {
            return false;
        }
        printStream.println("</whileStatement>");
        return true;
    }

    /* 'return' expression? ';' */
    public boolean compileReturn() {
        printStream.println("<returnStatement>");
        if (!compileKW(RETURN)) {
            return false;
        }
        compileExpression();
        if (!compileSymbol(';')) {
            return false;
        }
        printStream.println("</returnStatement>");
        return true;
    }

    /* 'if' '(' expression ')' '{' statements '}'
     * ( 'else' '{' statements '}' )? */
    public boolean compileIf() {
        printStream.println("<ifStatement>");
        if (!compileKW(IF)
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
                    if (!compileSymbol('[') && !compileExpression()
                      && !compileSymbol(']')) {
                        return false;
                    }
                    break;
                case '(':
                    if (!compileSymbol('(') && !compileExpression()
                      && !compileSymbol(')')) {
                        return false;
                    }
                    break;
                case '.':
                    if (!compileSymbol('.') && !compileIdentifier()
                      && !compileSymbol('(')
                      && !compileExpression() && !compileSymbol(')')) {
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
        if (compileExpression()) {
            while (compileSymbol(',') && compileExpression());
        }
        printStream.println("</expressionList>");
        return true;
    }
    
}
