package dreammaker.android.expensetracker.math;

public class Operator extends Token implements Comparable<Operator> {

    private final boolean leftAssociative;
    private final int precedence;

    public Operator(String value, boolean leftAssociative, int precedence, int start) {
        super(Type.OPERATOR, value, start);
        this.leftAssociative = leftAssociative;
        this.precedence = precedence;
    }

    public static Operator get(String value, int start) {
        switch (value) {
            case "+": return new Operator("+",true,1,start);
            case "-": return new Operator("-",true,1,start);
            case "X": return new Operator("X",true,2,start);
            case "\u00f7": return new Operator("\u00f7",true,2,start);
            case "%": return new Operator("%",true,2,start);
            default: throw new CalculatorException("unknown operator '"+value+"'");
        }
    }

    public boolean isLeftAssociative() {
        return leftAssociative;
    }

    public int getPrecedence() {
        return precedence;
    }

    @Override
    public String getValue() {
        return super.getValue();
    }

    @Override
    public int compareTo(Operator operator) {
        return precedence - operator.precedence;
    }
}
