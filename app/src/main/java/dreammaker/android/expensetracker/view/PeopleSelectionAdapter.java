package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.Person;
import dreammaker.android.expensetracker.util.Check;

// TODO: NONE type person not handled properly
public class PeopleSelectionAdapter extends AbsSelectionListAdapter<Person, PeopleSelectionAdapter.PersonSelectionViewHolder> {
    private final Person NONE;
    private List<Person> original;

    public PeopleSelectionAdapter(Context context) {
        super(context);
        NONE = new Person(NO_ID, context.getString(R.string.item_none));
    }

    @Override
    public void changeList(List<Person> newData) {
        this.original = newData;
        ArrayList<Person> people = new ArrayList<>(newData);
        people.add(0, NONE);
        super.changeList(people);
    }

    @Override
    public List<Person> getItemsList() {
        return original;
    }

    @Override
    public void setCheckedItems(List<Person> items) {
        if (null != items && items.remove(null)) {
            items.add(NONE);
        }
        super.setCheckedItems(items);
    }

    @Override
    public boolean onMatch(@Nullable Person item, @NonNull String key) {
        if (null == item)
            item = NONE;
        return item.getPersonName().toLowerCase().contains(key.toLowerCase());
    }

    @Override
    public Person getItem(int position) {
        Person item = super.getItem(position);
        if (null != item && item.getPersonId() == NO_ID)
            return null;
        return item;
    }

    @Override
    protected long getItemId(@NonNull Person item) {
        return item == null ? NO_ID : item.getPersonId();
    }

    @Override
    protected PersonSelectionViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        return new PersonSelectionViewHolder(getLayoutInflater().inflate(
                android.R.layout.simple_list_item_multiple_choice, parent,
                false));
    }

    @Override
    protected void onBindViewHolder(@NonNull PersonSelectionViewHolder vh, int position, boolean checked) {
        final Person person = getItem(position);
        vh.bind(person == null ? NONE : person);
    }

    public static class PersonSelectionViewHolder extends ViewHolder{
        TextView text1;

        public PersonSelectionViewHolder(View root) {
            super(root);
            text1 = findViewById(android.R.id.text1);
        }

        public void bind(Person person){
            if (Check.isNonNull(person))text1.setText(person.getPersonName());
            else text1.setText(null);
        }
    }
}
