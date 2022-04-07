package dreammaker.android.expensetracker.math;

public class Token {

    public enum Type {
        NUMBER("((\\d{1,2},)+(\\d{2},)+\\d{3}|(\\d{1,2},)\\d{3}|\\d{1,3})(\\.\\d{1,2})|" +
                "(((\\d{1,2},)+(\\d{2},)+\\d{3}|(\\d{1,2},)\\d{3}|\\d{1,3}))|" +
                "(\\.\\d{1,2})"),
        OPERATOR("[\\(\\)\\+\\-X\u00f7\\%]");

        private String pattern;

        Type(String pattern) {
            this.pattern = pattern;
        }

        public String getPattern() {
            return pattern;
        }
    }

    private final Type type;
    private final Object value;
    private final int start;

    public Token(Type type, Object value, int start) {
        this.type = type;
        this.value = value;
        this.start = start;
    }

    public Type getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    public int getStart() {
        return start;
    }

    @Override
    public String toString() {
        return type + "=" + value + "@" + start;
    }
}
