package dreammaker.android.expensetracker.math;

public final class Token {

    public enum Type {
        NUMBER,
        OPERATOR
    }

    private final Type type;
    private final String literal;
    private final int start;

    public Token(Type type, String literal, int start) {
        this.type = type;
        this.literal = literal;
        this.start = start;
    }

    public Type getType() {
        return type;
    }

    public String getLiteral() {
        return literal;
    }

    public int getStart() {
        return start;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Token
                && type == ((Token) obj).type
                && literal.equals(((Token) obj).literal)
                && start == ((Token) obj).start;
    }

    @Override
    public String toString() {
        return type + "=" + literal + "@" + start;
    }

}
