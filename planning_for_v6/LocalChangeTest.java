
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

public class LocalChangeTest {

    public static void main(String... args) {

        String[] replacements = {
                "০","১","২","৩","৪","৫","৬","৭","৮","৯"};

        ResourceBundle res = ResourceBundle.getBundle("number_words",new Locale("bn"));
        String num = "12,45,87,465.23";
        String output = num;
        Set<String> digits = new TreeSet<>();
        char[] chars = num.toCharArray();
        for (int i=0; i<num.length(); i++) {
            if (!Character.isDigit(chars[i])) continue;
            digits.add(String.valueOf(chars[i]));
        }
        for (String k : digits) {
            String replace = replacements[Integer.parseInt(k)];
            System.out.println("k="+k+" replace="+replace);
            output = output.replaceAll(k,replace);
        }
        System.out.println(num+" => "+output);
    }
}
