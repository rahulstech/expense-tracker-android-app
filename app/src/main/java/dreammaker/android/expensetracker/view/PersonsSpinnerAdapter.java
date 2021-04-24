package dreammaker.android.expensetracker.view;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.util.Check;

public class PersonsSpinnerAdapter extends BaseSpinnerAdapter<Person> {

    private static final Person PERSON_NONE = new Person(NO_ID, "None");
    private static final List<Person> onlyPersonNone = Collections.singletonList(PERSON_NONE);

    public PersonsSpinnerAdapter(Context context) {
        super(context);
    }

    @Override
    public void changeList(List<Person> newData) {
        List<Person> data;
        if (Check.isNull(newData) || newData.isEmpty()) {
            data = onlyPersonNone;
        }
        else {
            data = new ArrayList<>();
            data.add(PERSON_NONE);
            data.addAll(newData);
        }
        super.changeList(data);
    }

    @Override
    protected long getItemId(@NonNull Person item) {
        return item.getPersonId();
    }

    @Override
    protected void onBindViewHolder(SpinnerViewHolder vh, int position) {
        vh.setContentText(getItem(position).getPersonName());
    }
}
