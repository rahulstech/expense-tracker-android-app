package dreammaker.android.expensetracker.math;

import java.util.Collections;
import java.util.List;

public class Calculator {

    private List<Token> tokens = Collections.emptyList();
    private int index = -1;

    public Double calculate(String expression) {
        tokens = new Tokenizer(expression).tokenize();
        if (tokens.isEmpty()) {
            tokens = Collections.emptyList();
            index = -1;
            throw new CalculatorException("no expression found");
        }
        index = 0;
        return evalAddition();
    }

    double evalAddition() {
        double result = evalMultiplication();
        while (check("+") || check("-")) {
            Token operator = peek();
            double right = evalMultiplication();
            switch (operator.getLiteral()) {
                case "+": {
                    result = result + right;
                }
                break;
                case "-": {
                    result = result - right;
                }
            }
        }
        return result;
    }

    double evalMultiplication() {
        double result = evalSecondary();
        while (check("\u00f7") || check("X")
                || check("%")) {
            Token operator = peek();
            double right = evalSecondary();
            switch (operator.getLiteral()) {
                case "\u00f7": {
                    if (0 == right) {
                        throw new CalculatorException("division by zero");
                    }
                    result = result / right;
                }
                break;
                case "X": {
                    result = result * right;
                }
                break;
                case "%": {
                    result = result * right / 100;
                }
            }
        }
        return result;
    }

    double evalSecondary() {
        if (check("(")) {
            peek();
            double value = evalAddition();
            if (!check(")")) {
                throw new CalculatorException("group not closed with ')'");
            }
            peek();
            return value;
        }
        else {
            return evalPrimary();
        }
    }

    double evalPrimary() {
        if (check(Token.Type.NUMBER)) {
            Token token = peek();
            return Double.parseDouble(token.getLiteral());
        }
        throw new CalculatorException("unable to evaluate next primary expression");
    }

    boolean check(Object... tokenTypeOrLiteral) {
        int offset = 0;
        for (Object o : tokenTypeOrLiteral) {
            if (index+offset < 0 || index+offset >= tokens.size()) {
                return false;
            }
            Token token = tokens.get(index+offset);
            if (o instanceof String) {
                if (!token.getLiteral().equals(o)) {
                    return false;
                }
            }
            else {
                if (!token.getType().equals(o)) {
                    return false;
                }
            }
            offset++;
        }
        return true;
    }

    Token peek() {
        if (index < 0 || index >= tokens.size()) {
            throw new CalculatorException("");
        }
        Token token = tokens.get(index);
        index++;
        return token;
    }
}
