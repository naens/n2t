enum Segment {
    CONST,
    ARG,
    LOCAL,
    STATIC,
    THIS,
    THAT,
    POINTER,
    TEMP;

    public static Segment fromKind(Kind kind) {
        switch (kind) {
        case LOCAL:
            return LOCAL;
        case ARGUMENT:
            return ARG;
        case FIELD:
            return THIS;
        case STATIC:
            return STATIC;
        default:
            return null;
        }
    }

    @Override
    public String toString() {
        switch (this) {
        case CONST:
            return "constant";
        case ARG:
            return "argument";
        case LOCAL:
            return "local";
        case STATIC:
            return "static";
        case THIS:
            return "this";
        case THAT:
            return "that";
        case POINTER:
            return "pointer";
        case TEMP:
            return "temp";
        default:
            return null;
        }
    }
}
