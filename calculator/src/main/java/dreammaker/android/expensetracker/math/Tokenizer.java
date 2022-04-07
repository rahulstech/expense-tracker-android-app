package dreammaker.android.expensetracker.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.NumberUtil;

import static dreammaker.android.expensetracker.math.Token.Type.NUMBER;
import static dreammaker.android.expensetracker.math.Token.Type.OPERATOR;

public final class Tokenizer {

    private String expression;

    public Tokenizer(String expression) {
        this.expression = expression;
    }

    public List<Token> tokenize() {
        if (Check.isEmptyString(expression)) return Collections.emptyList();

        List<Token> tokens = new ArrayList<>();

        Matcher numberMatcher = Pattern.compile(NUMBER.getPattern()).matcher(expression);
        Matcher operatorMatcher = Pattern.compile(OPERATOR.getPattern()).matcher(expression);
        int start = 0, end;
        Token.Type tokenType;
        do {
            if (numberMatcher.find(start) && start == numberMatcher.start()){
                end = numberMatcher.end();
                tokenType = NUMBER;
            }
            else if (operatorMatcher.find(start) && start == operatorMatcher.start()){
                end = operatorMatcher.end();
                tokenType = OPERATOR;
            }
            else throw new RuntimeException("illegal token @ "+start+" "+expression.charAt(start));

            String valueString = expression.substring(start,end);
            Token token;
            if (NUMBER == tokenType) token = new Token(NUMBER, NumberUtil.parse(valueString),start);
            else token = Operator.get(valueString,start);
            tokens.add(token);
            start = end;
        }
        while (start < expression.length());

        return tokens;
    }
}