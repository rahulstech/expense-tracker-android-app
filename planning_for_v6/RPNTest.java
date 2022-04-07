import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RPNTest {

    static final int NUMERIC = 1;
    static final int OPERATOR = 2;

    static class Token {
        private final int type;
        private final Object value;

        public Token(int type, Object value) {
            this.type = type;
            this.value = value;
        }

        public int getType() {
            return type;
        }

        @SuppressWarnings("unchecked")
        public <T> T getValue() {
            return (T) value;
        }

        @Override
        public String toString() {
            return "Token{" +
                    "type=" + type +
                    ", value=" + value +
                    '}';
        }
    }

    static class Operator extends Token implements Comparable<Operator> {
        public static final Operator OPEN_BRACKET = new Operator("(",true,0);
        public static final Operator CLOSE_BRACKET = new Operator(")",true,0);
        public static final Operator ADDITION = new Operator("+",true,1);
        public static final Operator SUBTRACTION = new Operator("-",true,1);
        public static final Operator MULTIPLICATION = new Operator("*",true,2);
        public static final Operator DIVISION = new Operator("/",true,2);
        public static final Operator PERCENTAGE = new Operator("%",true,2);


        private final boolean leftAssociative;
        private final int precedence;

        private Operator(String value, boolean leftAssociative, int precedence) {
            super(OPERATOR, value);
            this.leftAssociative = leftAssociative;
            this.precedence = precedence;
        }

        public static Operator valueOf(String operator) {
            switch (operator) {
                case "+": return ADDITION;
                case "-": return SUBTRACTION;
                case "*": return MULTIPLICATION;
                case "/": return DIVISION;
                case "%": return PERCENTAGE;
                case "(": return OPEN_BRACKET;
                case ")": return CLOSE_BRACKET;
                default: throw new RuntimeException("unknown operator '"+operator+"'");
            }
        }

        public boolean isLeftAssociative() {
            return leftAssociative;
        }

        @Override
        public int compareTo(Operator o) {
            return this.precedence - o.precedence;
        }

        @Override
        public String toString() {
            return "Operator{" +
                    "type=" + getType() +
                    ", value=" + getValue() +
                    ", leftAssociative=" + leftAssociative +
                    ", precedence=" + precedence +
                    '}';
        }
    }

    static double calculate(List<Token> rpn) {
        Stack<Double> result = new Stack<>();
        for (Token t : rpn) {
            if (t.getType() == NUMERIC) result.push(t.getValue());
            else {
                String operator = t.getValue();
                double a = result.pop();
                double b = result.pop();
                switch (operator) {
                    case "+": result.push(a+b);
                    break;
                    case "-": result.push(b-a);
                    break;
                    case "*": result.push(a*b);
                    break;
                    case "/": {
                        // TODO: check division by zero
                        if (0 == a) throw new ArithmeticException("division by 0");
                        result.push(b/a);
                    }
                    break;
                    case "%": result.push(b*a/100);
                }
            }
        }
        return result.pop();
    }

    static List<Token> toRPN(List<Token> tokens) {
        List<Token> rpn = new ArrayList<>();
        Stack<Token> operators = new Stack<>();
        for (Token t : tokens) {
            if (NUMERIC == t.getType()) rpn.add(t);
            else if ("(".equals(t.getValue())) operators.push(t);
            else if (")".equals(t.getValue())) {
                while (!operators.isEmpty()) {
                    Token tok = operators.pop();
                    if ("(".equals(tok.getValue())) break;
                    rpn.add(tok);
                }
            }
            else {
                while (!operators.isEmpty()) {
                    if (OPERATOR != operators.peek().getType()) break;
                    Operator next = (Operator) t;
                    Operator top = (Operator) operators.peek();
                    if (next.isLeftAssociative() && next.compareTo(top) <= 0) rpn.add(operators.pop());
                    else break;
                }
                operators.push(t);
            }
        }
        while (!operators.isEmpty()){
            Token token = operators.pop();
            if ("(".equals(token.getValue())) throw new RuntimeException("bracket not closed");
            rpn.add(token);
        }
        return rpn;
    }

    static List<Token> tokenize(String expression) {
        List<Token> tokens = new ArrayList<>();

        Matcher numberMatcher = Pattern
                .compile("((\\d{1,2},)+(\\d{2},)+\\d{3}|(\\d{1,2},)\\d{3}|\\d{1,3})(\\.\\d{1,2})|" +
                        "(((\\d{1,2},)+(\\d{2},)+\\d{3}|(\\d{1,2},)\\d{3}|\\d{1,3}))|" +
                        "(\\.\\d{1,2})").matcher(expression);
        Matcher operatorMatcher = Pattern.compile("[\\(\\)\\+\\-\\*\\/\\%]{1}").matcher(expression);
        int start = 0, end;
        int tokenType;
        do {
            if (numberMatcher.find(start) && start == numberMatcher.start()){
                end = numberMatcher.end();
                tokenType = NUMERIC;
            }
            else if (operatorMatcher.find(start) && start == operatorMatcher.start()){
                end = operatorMatcher.end();
                tokenType = OPERATOR;
            }
            else throw new RuntimeException("illegal token @ "+start+" "+expression.charAt(start));

            String valueString = expression.substring(start,end);
            Token token;
            if (NUMERIC == tokenType) token = new Token(NUMERIC,parseDouble(valueString));
            else token = Operator.valueOf(valueString);
            tokens.add(token);
            start = end;
        }
        while (start < expression.length());

        return tokens;
    }

    static DecimalFormat decimalFormat = new DecimalFormat("##,##,##,##,##,##,###.##");

    static double parseDouble(String number) {
        try {
            return decimalFormat.parse(number).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    static String formatDecimal(double n) {
        long i = (long) n;
        String output = "";
        long by = 1000L;
        int r;
        boolean firstGroup = true;
        do {
            r = (int) (i%by);
            output = firstGroup ? String.valueOf(r) : r+","+output;
            firstGroup = false;
            i = i/by;
            by = 100;
        }
        while (i > 0);
        int decimal = (int) (Math.round(n*100))%100;
        if (decimal > 9) {
            decimal = decimal%10 == 0 ? decimal/10 : decimal;
            output = output+String.format(".%d",decimal);
        }
        else if (decimal > 0) output = output+".0"+decimal;
        return output;
    }

    public static void main(String[] args) {
        String expression = "2,53,127.1+3";
        List<Token> tokens = tokenize(expression);
        List<Token> rpn = toRPN(tokens);
        System.out.println(tokens);
        System.out.println(rpn);
        System.out.println(expression+" = "+decimalFormat.format(calculate(rpn)));

        double n = 5637.356;
        System.out.println(formatDecimal(n));
    }
}
