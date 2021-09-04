package dreammaker.android.expensetracker.math;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Tokenizer {

    private final CharStream chars;

    public Tokenizer(String input) {
        chars = new CharStream(input);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (chars.has(0)) {
            Token token;
            if (peek("\\d")
                    || peek("\\.","\\d")) {
                token = lexNumber();
            }
            else {
                token = lexOperator();
            }
            tokens.add(token);
        }
        return tokens;
    }

    Token lexNumber() {
        while (match("\\d"));
        if (peek("\\.")) {
            chars.advance();
        }
        else {
            return chars.emit(Token.Type.NUMBER);
        }
        while (match("\\d"));
        if (peek("\\.","\\d")) {
            throw new CalculatorException("multi decimal @"+chars.index);
        }
        return chars.emit(Token.Type.NUMBER);
    }

    Token lexOperator() {
        if (match("[\\(\\)\\%\\+\\-X\u00f7]")) {
            return chars.emit(Token.Type.OPERATOR);
        }
        throw new CalculatorException("'"+chars.get(0)+"' at "+chars.index+" is not an operator");
    }

    private boolean peek(String... patterns) {
        int offset = 0;
        for (String pattern : patterns) {
            if (!chars.has(offset)) return false;
            char c = chars.get(offset);
            Matcher matcher = Pattern.compile(pattern)
                    .matcher(String.valueOf(c));
            if (!matcher.matches()) return false;
            offset++;
        }
        return true;
    }

    private boolean match(String... patterns) {
        for (String pattern : patterns) {
            if (!peek(pattern)) {
                return false;
            }
            else {
                chars.advance();
            }
        }
        return true;
    }

    private static final class CharStream {

        private final String input;
        private int index = 0;
        private int length = 0;

        public CharStream(String input) {
            this.input = input;
        }

        public boolean has(int offset) {
            return index + offset < input.length();
        }

        public char get(int offset) {
            return input.charAt(index + offset);
        }

        public void advance() {
            index++;
            length++;
        }

        public void skip() {
            length = 0;
        }

        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }
}