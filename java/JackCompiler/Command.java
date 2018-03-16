enum Command {
    ADD,
    SUB,
    MUL,
    DIV,
    NEG,
    EQ,
    GT,
    LT,
    AND,
    OR,
    NOT;

    @Override
    public String toString() {
        switch (this) {
        case MUL:
            return "call Math.multiply 2";
        case DIV:
            return "call Math.divide 2";
        default:
            return super.toString().toLowerCase();
        }
    }

    public static Command unFromSymbol(char symbol) {
        switch (symbol) {
        case '-':
            return NEG;
        case '~':
            return NOT;
        default:
            return null;
        }
    }
    public static Command binFromSymbol(char symbol) {
        switch (symbol) {
        case '+':
            return ADD;
        case '-':
            return SUB;
        case '*':
            return MUL;
        case '/':
            return DIV;
        case '&':
            return AND;
        case '|':
            return OR;
        case '<':
            return LT;
        case '>':
            return GT;
        case '=':
            return EQ;
        default:
            return null;
        }
    }
}
