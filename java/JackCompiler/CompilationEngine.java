import java.io.*;

class CompilationEngine {

    private JackTokenizer tokenizer;
//    private PrintStream ps;
    private VMWriter vmw;
    private SymbolTable symbolTable;
    private int labelNumber;
    private String className;

    public CompilationEngine(File inFile, /*File xmlFile, */File vmFile) {
//        try {
            tokenizer = new JackTokenizer(inFile);
//            ps = new PrintStream(xmlFile);
            vmw = new VMWriter(vmFile);
            labelNumber = 0;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private int getLabelNumber() {
        return labelNumber++;
    }

    public void start() {
        if (!compileClass()) {
            System.out.println("error");
        }
//        ps.close();
        vmw.close();
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
//        JackTokenizer.printCurrent(ps, tokenizer);
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
//        JackTokenizer.printCurrent(ps, tokenizer);
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
//        JackTokenizer.printCurrent(ps, tokenizer);
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
//        ps.println(prefixTag);
//        JackTokenizer.printCurrent(ps, tokenizer);
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
//        JackTokenizer.printCurrent(ps, tokenizer);
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
//        JackTokenizer.printCurrent(ps, tokenizer);
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
        if (!compileKW(Keyword.CLASS, "<class>")) {
            return false;
        }
        className = getIdent();
        if (!compileIdentifier() || !compileSymbol('{')) {
            return false;
        }
        while (compileClassVarDec());
        while (compileSubroutine());
        if (!compileSymbol('}')) {
            return false;
        }
//        ps.println("</class>");
        return true;
    }

    public boolean compileType() {
        return compileKW(Keyword.INT) || compileKW(Keyword.BOOLEAN)
                || compileKW(Keyword.CHAR)|| compileKW(Keyword.VOID)
                || compileIdentifier();
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

    private Command getBinOp() {
        if (!tokenizer.hasMoreTokens()
                || tokenizer.getTokenType() != TokenType.SYMBOL) {
            return null;
        }
        char symbol = tokenizer.getSymbol();
        return Command.binFromSymbol(symbol);
    }

    private Command getUnOp() {
        if (!tokenizer.hasMoreTokens()
                || tokenizer.getTokenType() != TokenType.SYMBOL) {
            return null;
        }
        char symbol = tokenizer.getSymbol();
        return Command.unFromSymbol(symbol);
    }

    public int getInt() {
        if (!tokenizer.hasMoreTokens()) {
            return -1;
        }
        if (tokenizer.getTokenType() != TokenType.INT_CONST) {
            return -1;
        }
        return tokenizer.getIntVal();
    }

    public String getString() {
        if (!tokenizer.hasMoreTokens()) {
            return null;
        }
        if (tokenizer.getTokenType() != TokenType.STRING_CONST) {
            return null;
        }
        return tokenizer.getStringVal();
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
        case VOID:
            return "void";
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
        int index = symbolTable.define(name, type, kind);
        vmw.writeDefine(name, type, kind, index);
        while (compileSymbol(',')) {
            name = getIdent();
            if (!compileIdentifier()) {
                return false;
            }
            index = symbolTable.define(name, type, kind);
            vmw.writeDefine(name, type, kind, index);
        }
        if (!compileSymbol(';')) {
            return false;
        }
//        ps.println("</classVarDec>");
        return true;
    }

    public boolean compileStatements() {
//        ps.println("<statements>");
        while (compileStatement());
//        ps.println("</statements>");
        return true;
    }

    public boolean compileBody() {
        if (!compileSymbol('{')) {
            return false;
        }
        return compileStatements() && compileSymbol('}');
    }

    /* ('constructor' | 'function' | 'method')
     * ('void' | type)
     * subroutineName
     * '(' parameterList ')'
     *  subroutineBody */
    public boolean compileSubroutine() {
        Keyword kw = getKW();
        if (!compileKW(Keyword.CONSTRUCTOR, "<subroutineDec>")
                && !compileKW(Keyword.FUNCTION, "<subroutineDec>")
                && !compileKW(Keyword.METHOD, "<subroutineDec>")) {
            return false;
        }
        int index = 0;
        String type = makeType();
        if (!compileType()) {
            return false;
        }
        String name = getIdent();
        symbolTable.startSubroutine();
        if (!compileIdentifier()) {
            return false;
        }
        if (kw == Keyword.METHOD) {
            symbolTable.define(".", ".", Kind.ARGUMENT);
        }
        if (!compileSymbol('(')
                || compileParameterList() == -1
                || !compileSymbol(')')) {
            return false;
        }
//        ps.println(String.format("<%s>", "subroutineBody"));
        if (!compileSymbol('{')) {
            return false;
        }
        int vars = 0;
        int n;
        while ((n = compileVarDec()) != -1) {
            vars += n;
        }
        int fields = symbolTable.fields();
        vmw.writeSubroutine(kw, String.format("%s.%s", className, name), type, vars);
        if (kw == Keyword.CONSTRUCTOR && fields > 0) {
            vmw.writePush(Segment.CONST, fields);
            vmw.writeCall("Memory.alloc", 1);
            vmw.writePop(Segment.POINTER, 0);
        }
        if (kw == Keyword.METHOD) {
            vmw.writePush(Segment.ARG, 0);
            vmw.writePop(Segment.POINTER, 0);
        }
        if (!compileStatements() || !compileSymbol('}')) {
            return false;
        }
//        ps.println(String.format("</%s>", "subroutineBody"));
//        ps.println("</subroutineDec>");
        return true;
    }

    /* ((type varName) ( ',' type varName)*)? */
    public int compileParameterList() {
//        ps.println("<parameterList>");
        String type = makeType();
        int res = 0;
        if (compileType()) {
            String name = getIdent();
            if (!compileIdentifier()) {
                return -1;
            }
            res = 1;
            int index = symbolTable.define(name, type, Kind.ARGUMENT);
            vmw.writeDefine(name, type, Kind.ARGUMENT, index);
            while (compileSymbol(',')) {
                type = makeType();
                if (!compileType()) {
                    return -1;
                }
                name = getIdent();
                if (!compileIdentifier()) {
                    return -1;
                }
                index = symbolTable.define(name, type, Kind.ARGUMENT);
                vmw.writeDefine(name, type, Kind.ARGUMENT, index);
                res++;
            }
        }
//        ps.println("</parameterList>");
        return res;
    }

    /* 'var' type varName ( ',' varName)* ';' */
    public int compileVarDec() {
        int result = 0;
        if (!compileKW(Keyword.VAR, "<varDec>")) {
            return -1;
        }
        String type = makeType();
        if (!compileType()) {
            return -1;
        }
        String name = getIdent();
        if (!compileIdentifier()) {
            return -1;
        }
        result = 1;
        int index = symbolTable.define(name, type, Kind.LOCAL);
        vmw.writeDefine(name, type, Kind.LOCAL, index);
        while (compileSymbol(',')) {
            name = getIdent();
            if (!compileIdentifier()) {
                return -1;
            }
            index = symbolTable.define(name, type, Kind.LOCAL);
            vmw.writeDefine(name, type, Kind.LOCAL, index);
            result += 1;
        }
        if (!compileSymbol(';')) {
            return -1;
        }
//        ps.println("</varDec>");
        return result;
    }

    /* letStatement | ifStatement | whileStatement
     * | doStatement | returnStatement */
    public boolean compileStatement() {
        return compileLet() || compileIf() || compileWhile()
            || compileDo() || compileReturn();
    }

    /* 'do' id ('.' id) ? '(' expressionlist ')' ';' */
    public boolean compileDo() {
        int nArgs;
        if (!compileKW(Keyword.DO, "<doStatement>")) {
            return false;
        }
        String ident = getIdent();
        if (!compileIdentifier()) {
            return false;
        }
        String subMethod = null;
        if (compileSymbol('.')) {
            subMethod = getIdent();
            if (!compileIdentifier()) {
                return false;
            }
        }
        if (subMethod == null) {
            vmw.writePush(Segment.POINTER, 0);
        } else if (!symbolTable.isClass(ident)) {
            Segment segment = Segment.fromKind(symbolTable.kindOf(ident));
            vmw.writePush(segment, symbolTable.indexOf(ident));
        }
        if (!compileSymbol('(') || (nArgs = compileExpressionList()) == -1
                || !compileSymbol(')') || !compileSymbol(';')) {
            return false;
        }
        if (subMethod == null) {
            vmw.writeCall(String.format("%s.%s", className, ident), nArgs + 1);
        } else if (symbolTable.isClass(ident)) {
            vmw.writeCall(String.format("%s.%s", ident, subMethod), nArgs);
        } else {
            String otherClassName = symbolTable.typeOf(ident);
            vmw.writeCall(String.format("%s.%s", otherClassName, subMethod), nArgs + 1);
        }
        vmw.writePop(Segment.TEMP, 0);
//        ps.println("</doStatement>");
        return true;
    }

    /* 'let' varName ( '[' expression ']' )? '=' expression ';' */
    public boolean compileLet() {
        if (!compileKW(Keyword.LET, "<letStatement>")) {
            return false;
        }
        String name = getIdent();
        Kind kind = symbolTable.kindOf(name);
        String type = symbolTable.typeOf(name);
        int index = symbolTable.indexOf(name);
        Segment segment = Segment.fromKind(kind);
        if (!compileIdentifier()) {
            return false;
        }
        if (compileSymbol('[')) {
            vmw.writePush(segment, index);                          /* push arr */
            if (!compileExpression() || !compileSymbol(']')){    /* push expr1 */
                return false;
            }
            vmw.writeArithmetic(Command.ADD);                       /* add */
            if (!compileSymbol('=') || !compileExpression()) {   /* push expr2 */
                return false;
            }
            vmw.writePop(Segment.TEMP, 0);                    /* pop temp 0 */
            vmw.writePop(Segment.POINTER, 1);                 /* pop pointer 1 */
            vmw.writePush(Segment.TEMP, 0);                   /* push temp 0*/
            segment = Segment.THAT;                                 /* pop that 0 */
            index = 0;
            if (!compileSymbol(';')) {
                return false;
            }
        } else {
            if (!compileSymbol('=') || !compileExpression() || !compileSymbol(';')) {
                return false;
            }
        }
        vmw.writePop(segment, index);
//        ps.println("</letStatement>");
        return true;
    }

    /* 'while' '(' expression ')' '{' statements '}' */
    public boolean compileWhile() {
        if (!compileKW(Keyword.WHILE, "<whileStatement>")) {
            return false;
        }
        String l1 = String.format("label.while.test.%d", getLabelNumber());
        String l2 = String.format("label.while.end.%d", getLabelNumber());
        vmw.writeLabel(l1);
        if (!compileSymbol('(') || !compileExpression() || !compileSymbol(')')) {
            return false;
        }
        vmw.writeArithmetic(Command.NOT);
        vmw.writeIf(l2);
        if (!compileBody()) {
            return false;
        }
        vmw.writeGoto(l1);
        vmw.writeLabel(l2);
//        ps.println("</whileStatement>");
        return true;
    }

    /* 'return' expression? ';' */
    public boolean compileReturn() {
        if (!compileKW(Keyword.RETURN, "<returnStatement>")) {
            return false;
        }
        if (testSymbol(';')) {
            vmw.writePush(Segment.CONST, 0);
        } else {
            compileExpression();
        }
        vmw.writeReturn();
        if (!compileSymbol(';')) {
            return false;
        }
//        ps.println("</returnStatement>");
        return true;
    }

    /* 'if' '(' expression ')' '{' statements '}'
     * ( 'else' '{' statements '}' )? */
    public boolean compileIf() {
        if (!compileKW(Keyword.IF, "<ifStatement>")) {
            return false;
        }
        String l1 = String.format("label.if.alternative.%d", getLabelNumber());
        String l2 = String.format("label.if.end.%d", getLabelNumber());
        if (!compileSymbol('(') || !compileExpression() || !compileSymbol(')')) {
            return false;
        }
        vmw.writeArithmetic(Command.NOT);
        vmw.writeIf(l1);
        if (!compileBody()) {
            return false;
        }
        vmw.writeGoto(l2);
        vmw.writeLabel(l1);
        if (compileKW(Keyword.ELSE) && !compileBody()) {
            return false;
        }
        vmw.writeLabel(l2);
//        ps.println("</ifStatement>");
        return true;
    }

    public boolean compileOp() {
        return compileSymbol('+') || compileSymbol('-') || compileSymbol('*')
          || compileSymbol('/') || compileSymbol('&') || compileSymbol('|')
          || compileSymbol('<') || compileSymbol('>') || compileSymbol('=');
    }
    
    /* term (op term)* */
    public boolean compileExpression() {
//        ps.println("<expression>");
        if (!compileTerm()) {
            return false;
        }
        Command cmd = getBinOp();
        while (compileOp()) {
            if (!compileTerm()) {
                return false;
            }
            vmw.writeArithmetic(cmd);
            cmd = getBinOp();
        }
//        ps.println("</expression>");
        return true;
    }

    /* integerConstant | stringConstant | keywordConstant
     * | varName | varName '[' expression ']' | subroutineCall
     * | '(' expression ')' | unaryOp term */
    public boolean compileTerm() {
//        ps.println("<term>");
        int intVal = getInt();
        if (compileInt()) {
            vmw.writePush(Segment.CONST, intVal);
//            ps.println("</term>");
            return true;
        }
        String strVal = getString();
        if (compileStr()) {
            vmw.writePush(Segment.CONST, strVal.length());
            vmw.writeCall("String.new", 1);
            vmw.writePop(Segment.TEMP, 4);
            for (int i = 0; i < strVal.length(); i++) {
                vmw.writePush(Segment.TEMP, 4);
                vmw.writePush(Segment.CONST, strVal.charAt(i));
                vmw.writeCall("String.appendChar", 2);
                vmw.writePop(Segment.TEMP, 5);
            }
            vmw.writePush(Segment.TEMP, 4);
            //vmw.writePop(Segment.TEMP, 1);
//            ps.println("</term>");
            return true;
        }
        Keyword kw = getKW();
        if (compileKW(Keyword.TRUE) || compileKW(Keyword.FALSE)
                || compileKW(Keyword.NULL) || compileKW(Keyword.THIS)) {
            Segment segment = kw == Keyword.THIS ? Segment.POINTER : Segment.CONST;
            if (kw == Keyword.TRUE) {
                vmw.writePush(Segment.CONST, 0);
                vmw.writeArithmetic(Command.NOT);
            } else {
                int index = 0;
                vmw.writePush(segment, index);
            }
//            ps.println("</term>");
            return true;
        }
        Command cmd = getUnOp();
        if (compileSymbol('-') || compileSymbol('~')) {
            if (compileTerm()) {
                vmw.writeArithmetic(cmd);
//                ps.println("</term>");
                return true;
            } else {
                return false;
            }
        }
        if (compileSymbol('(') && compileExpression() && compileSymbol(')')) {
//            ps.println("</term>");
            return true;
        }
        final String ident = getIdent();
        Segment segment = null;
        int index = -1;
        if (!symbolTable.isClass(ident)) {
            segment = Segment.fromKind(symbolTable.kindOf(ident));
            index = symbolTable.indexOf(ident);
        }
        if (!compileIdentifier()) {
            return false;
        }
        /* id | id '[' expr ']' | id '(' list ')' | id '.' id ' (' list ')' */
        int nArgs;
        if (tokenizer.hasMoreTokens()
                && tokenizer.getTokenType() == TokenType.SYMBOL) {
            switch (tokenizer.getSymbol()) {
            case '[':
                vmw.writePush(segment, index);
                if (!compileSymbol('[') || !compileExpression()
                        || !compileSymbol(']')) {
                    return false;
                }
                vmw.writeArithmetic(Command.ADD);
                vmw.writePop(Segment.POINTER, 1);
                vmw.writePush(Segment.THAT, 0);
                break;
            case '(':
                if (!compileSymbol('(')
                        || (nArgs = compileExpressionList()) == -1
                        || !compileSymbol(')')) {
                    return false;
                }
                vmw.writePush(Segment.POINTER, 0);
                vmw.writeCall(String.format("%s.%s", className, ident), nArgs + 1);
                break;
            case '.':
                if (!compileSymbol('.')) {
                    return false;
                }
                String subname = getIdent();
                if (!compileIdentifier()
                        || !compileSymbol('(')
                        || (nArgs = compileExpressionList()) == -1
                        || !compileSymbol(')')) {
                    return false;
                }
                if (symbolTable.isClass(ident)) {
                    vmw.writeCall(String.format("%s.%s", ident, subname), nArgs);
                } else {
                    String otherClassName = symbolTable.typeOf(ident);
                    vmw.writePush(segment, index);
                    vmw.writeCall(String.format("%s.%s", otherClassName, subname), nArgs + 1);
                }
                break;
            default:                /* variable */
                vmw.writePush(segment, index);
                break;
            }
        }
//        ps.println("</term>");
        return true;
    }

    /* (expression ( ',' expression)* )? */
    public int compileExpressionList() {
        int n = 0;
//        ps.println("<expressionList>");
        if (testSymbol(')')) {
//            ps.println("</expressionList>");
            return n;
        }
        if (compileExpression()) {
            n = 1;
            while (compileSymbol(',')) {
                if (!compileExpression()) {
                    return -1;
                }
                n++;
            }
        }
//        ps.println("</expressionList>");
        return n;
    }
}
