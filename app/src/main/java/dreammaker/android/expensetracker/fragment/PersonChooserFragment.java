package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.Transaction;
import dreammaker.android.expensetracker.database.model.PersonDisplayModel;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.view.adapter.PersonChooserRecyclerAdapter;
import dreammaker.android.expensetracker.viewmodel.SavedStateViewModel;
import dreammaker.android.expensetracker.viewmodel.TransactionInputViewModel;

public class PersonChooserFragment extends Fragment {

    private static final String TAG = "PrsnChooserFrag";

    private static final String KEY_PEOPLE_ADAPTER_STATE = "people_adapter_state";

    private NavController navController;
    private TransactionInputViewModel viewModel;
    private SavedStateViewModel mSavedState;
    private Button btnNext;
    private Button btnPrevious;
    private RecyclerView peopleChooser;
    private PersonChooserRecyclerAdapter peopleAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = new ViewModelProvider(requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(TransactionInputViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.payee_payer_chooser_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        mSavedState = new ViewModelProvider(this).get(SavedStateViewModel.class);
        TextView title = view.findViewById(R.id.title);
        btnNext = view.findViewById(R.id.btn2);
        btnPrevious= view.findViewById(R.id.btn1);
        peopleChooser = view.findViewById(R.id.list);

        peopleAdapter = new PersonChooserRecyclerAdapter(requireContext());
        peopleAdapter.changeChoiceMode(PersonChooserRecyclerAdapter.CHOICE_MODE_SINGLE);
        peopleChooser.setAdapter(peopleAdapter);
        viewModel.getPeopleDisplayLiveData().observe(getViewLifecycleOwner(),items -> peopleAdapter.submitList(items));
        title.setText(R.string.person);
        view.findViewById(R.id.container_two_buttons).setVisibility(View.VISIBLE);
        btnNext.setText(R.string.next);
        btnPrevious.setText(R.string.back);
        btnNext.setOnClickListener(v -> onClickNext());
        btnPrevious.setOnClickListener(v -> onClickPrevious());

        peopleAdapter.onRestoreState(mSavedState.getParcelable(KEY_PEOPLE_ADAPTER_STATE));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedState.put(KEY_PEOPLE_ADAPTER_STATE,peopleAdapter.onSaveState());
    }

    void onClickNext() {
        PersonDisplayModel person = peopleAdapter.getCheckedItem();
        if (null == person) {
            Toast.makeText(requireContext(),R.string.no_person_selected,Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle(getArguments());
        args.putLong(Constants.EXTRA_PERSON,person.getId());
        navController.navigate(R.id.people_to_accounts,args);
    }

    void onClickPrevious() {
        navController.popBackStack();
    }
}
