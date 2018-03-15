enum Command {
    ADD,
    NEG,
    EQ,
    GT,
    LT,
    AND,
    OR,
    NOT;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
