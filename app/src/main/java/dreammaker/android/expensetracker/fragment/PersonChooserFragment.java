package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.BuildConfig;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.adapter.PeopleChooserAdapter;
import dreammaker.android.expensetracker.database.model.PersonModel;
import dreammaker.android.expensetracker.fragment.parcelable.PersonParcelable;
import dreammaker.android.expensetracker.itemdecoration.SimpleEmptyRecyclerViewDecoration;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.util.Constants;
import dreammaker.android.expensetracker.util.ResourceUtil;
import dreammaker.android.expensetracker.util.ToastUtil;
import dreammaker.android.expensetracker.viewmodel.PersonViewModel;

@SuppressWarnings("unused")
public class PersonChooserFragment extends BaseChooserWithSearchFragment {

    private static final String TAG = PersonChooserFragment.class.getSimpleName();

    @SuppressWarnings("FieldCanBeLocal")
    private PersonViewModel mViewModel;

    @SuppressWarnings("FieldCanBeLocal")
    private NavController navController;

    private PeopleChooserAdapter mAdapter;

    private List<PersonModel> mLoadedPeople;

    private ArrayList<PersonParcelable> mSelectedPeople;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(PersonViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        mViewModel.getAllPeopleWithUsageCount().observe(getViewLifecycleOwner(),this::onPeopleLoaded);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        navController = Navigation.findNavController(view);
        mAdapter = new PeopleChooserAdapter(requireContext());
        setAdapter(mAdapter);
        getList().addItemDecoration(new SimpleEmptyRecyclerViewDecoration(getText(R.string.label_no_person),
                ResourceUtil.getDrawable(requireContext(),R.drawable.ic_person_72)));
        super.onViewCreated(view, savedInstanceState);
        mAdapter.setChoiceModel(getChoiceModel());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mSelectedPeople = savedInstanceState.getParcelableArrayList(KEY_SELECTIONS);
        }
        if (null == mSelectedPeople) {
            mSelectedPeople = new ArrayList<>();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(KEY_SELECTIONS,mSelectedPeople);
    }

    @NonNull
    @Override
    public ChoiceModel.Callback getChoiceModelCallback() {
        return mAdapter;
    }

    @Override
    protected void onChangeSearchQuery(CharSequence query) {
        submitPeople();
    }

    @Override
    public void onItemChecked(@NonNull RecyclerView recyclerView, @NonNull View view, int position, boolean checked) {
        if (PeopleChooserAdapter.SECTION_ITEM_TYPE == mAdapter.getItemViewType(position)) {
            PersonModel person = mAdapter.getData(position);
            PersonParcelable parcelable = new PersonParcelable(person);
            if (checked) {
                mSelectedPeople.add(parcelable);
            }
            else {
                mSelectedPeople.remove(parcelable);
            }
        }
    }

    private void onPeopleLoaded(@Nullable List<PersonModel> people) {
        if (null == people) {
            String message = getString(R.string.no_person).toLowerCase();
            ToastUtil.showErrorShort(requireContext(),message);
            exit();
        }
        else {
            mLoadedPeople = people;
            submitPeople();
            if (!people.isEmpty() && hasExtraInitial()) {
                if (Constants.ACTION_PICK_MULTIPLE.equals(getAction())) {
                    ArrayList<PersonParcelable> initials = getExtraInitial();
                    if (null != initials && !initials.isEmpty()) {
                        ArrayList<Object> keys = new ArrayList<>();
                        for (PersonParcelable person : initials) {
                            keys.add(person.getId());
                        }
                        getChoiceModel().setChecked(keys,true);
                        mSelectedPeople.addAll(initials);
                    }
                }
                else {
                    PersonParcelable initial = getExtraInitial();
                    if (null != initial) {
                        Object key = initial.getId();
                        getChoiceModel().setChecked(key, true);
                        mSelectedPeople.add(initial);
                    }
                }
            }
        }
    }

    private void submitPeople() {
        CharSequence query = getSearchQueryText();
        mAdapter.filter(mLoadedPeople,null == query ? null : query.toString());
    }

    @NonNull
    @Override
    protected Bundle onPrepareResult() {
        Bundle result = new Bundle();
        final ArrayList<PersonParcelable> selections = mSelectedPeople;
        final int count = selections.size();
        final String action = getAction();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onPrepareResult: action="+action+" selections-size="+count);
        }
        if (count > 0) {
            if (Constants.ACTION_PICK_MULTIPLE.equals(action)) {
                result.putParcelableArrayList(Constants.KEY_RESULT, selections);
            } else if (Constants.ACTION_PICK.equals(action)) {
                PersonParcelable person = selections.get(count-1); // last added
                result.putParcelable(Constants.KEY_RESULT, person);
            }
        }
        return result;
    }
}
