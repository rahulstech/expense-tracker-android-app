package dreammaker.android.expensetracker.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.AboutPerson;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Helper;

public class PersonsAdapter extends BaseRecyclerViewListAdapterFilterable<AboutPerson, PersonsAdapter.PersonViewHolder> {

	private static DiffUtil.ItemCallback<AboutPerson> DIFF_CALLBACK = new DiffUtil.ItemCallback<AboutPerson>() {
		@Override
		public boolean areItemsTheSame(@NonNull AboutPerson oldItem, @NonNull AboutPerson newItem) {
			return oldItem.getPersonId() == newItem.getPersonId();
		}

		@Override
		public boolean areContentsTheSame(@NonNull AboutPerson oldItem, @NonNull AboutPerson newItem) {
			return oldItem.equalContent(newItem);
		}
	};

    public PersonsAdapter(Context context){
        super(context, DIFF_CALLBACK);
    }

	@Override
	protected long getItemId(@NonNull AboutPerson item) {
		return item.getPersonId();
	}

	@Override
	public PersonViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		PersonViewHolder vh = new PersonViewHolder(getLayoutInflater().inflate(R.layout.person_list_item, parent, false));
		vh.setOnChildClickListener(this);
		return vh;
	}

	@Override
	public void onBindViewHolder(PersonViewHolder vh, int position) {
		vh.bind(getItem(position));
	}

	@Override
	public boolean onMatch(@Nullable AboutPerson item, @NonNull String key) {
		return item.getPersonName().toLowerCase().contains(key.toLowerCase());
	}

	public static class PersonViewHolder extends RVAViewHolder{
		TextView person_name;
		TextView due_payment;
		TextView advanced_payment;
		View labelDuePayment;
		View labelAdvPayment;
		public ImageView options;

		PersonViewHolder(View root){
			super(root);
			person_name = findViewById(R.id.to_account);
			due_payment = findViewById(R.id.due_payment);
			advanced_payment = findViewById(R.id.advanced_payment);
			options = findViewById(R.id.options);
			labelDuePayment = findViewById(R.id.label_due_payment);
			labelAdvPayment = findViewById(R.id.label_advanced_payment);
			bindChildForClick(options);
			bindChildForClick(root);
		}

		public void bind(AboutPerson person){
			if (Check.isNonNull(person)){
				person_name.setText(person.getPersonName());
				float due = person.getDue();
				float advanced = person.getAdvanced();
				if (advanced > 0) {
					advanced_payment.setText(Helper.floatToString(advanced));
					changeDuePaymentAndAdvancedPaymentVisibility(false,true);
				}
				else if (due > 0){
					due_payment.setText(Helper.floatToString(due));
					changeDuePaymentAndAdvancedPaymentVisibility(true,false);
				}
				else {
					changeDuePaymentAndAdvancedPaymentVisibility(false,false);
				}
			}
		}

		void changeDuePaymentAndAdvancedPaymentVisibility(boolean showDue, boolean showAdv) {
			labelDuePayment.setVisibility(showDue ? View.VISIBLE : View.GONE);
			due_payment.setVisibility(showDue ? View.VISIBLE : View.GONE);
			labelAdvPayment.setVisibility(showAdv ? View.VISIBLE : View.GONE);
			advanced_payment.setVisibility(showAdv ? View.VISIBLE : View.GONE);
		}
	}
}
