package dreammaker.android.expensetracker.database;

import androidx.annotation.NonNull;

public class AboutPerson extends Person {

    public AboutPerson(long personId, @NonNull String personName, float due) {
        super(personId,personName,due);
    }

    public float getDue() {
        return Math.max(super.getDue(),0f);
    }

    public float getAdvanced() {
        return Math.abs(Math.min(super.getDue(),0f));
    }
}
