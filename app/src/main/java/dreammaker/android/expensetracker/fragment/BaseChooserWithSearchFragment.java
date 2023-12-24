package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import dreammaker.android.expensetracker.databinding.LayoutChooserWithSearchBinding;
import dreammaker.android.expensetracker.listener.ChoiceModel;
import dreammaker.android.expensetracker.listener.OnItemCheckedListener;
import dreammaker.android.expensetracker.util.Constants;

@SuppressWarnings("unused")
public abstract class BaseChooserWithSearchFragment extends Fragment implements OnItemCheckedListener {

    public static final String EXTRA_TITLE = "extra_title";

    public static final String KEY_RESULT = "key_result";

    public static final String KEY_SELECTIONS = "key_selections";

    public static final String EXTRA_REQUEST_CODE = "extra_request_code";

    public static final String EXTRA_INITIAL = "extra_initial";

    private LayoutChooserWithSearchBinding mBinding;

    private NavController navController;

    private ChoiceModel mChoiceModel;

    private ChoiceModel.SavedStateViewModel mChoiceModelSavedState;

    public BaseChooserWithSearchFragment() { super(); }

    public final boolean hasExtraTitle() {
        return requireArguments().containsKey(EXTRA_TITLE);
    }

    public CharSequence getExtraTitle() {
        return requireArguments().getString(EXTRA_TITLE);
    }

    public boolean hasExtraAction() {
        return requireArguments().containsKey(Constants.EXTRA_ACTION);
    }

    public String getAction() {
        return requireArguments().getString(Constants.EXTRA_ACTION,Constants.ACTION_PICK);
    }

    public boolean hasRequestCode() {
        return requireArguments().containsKey(EXTRA_REQUEST_CODE);
    }

    public int getExtraRequestCode() {
        return requireArguments().getInt(EXTRA_REQUEST_CODE,0);
    }


    public boolean hasExtraInitial() {
        return requireArguments().containsKey(EXTRA_INITIAL);
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtraInitial() {
        return (T) requireArguments().get(EXTRA_INITIAL);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!hasExtraAction()) {
            throw new IllegalArgumentException("argument "+Constants.EXTRA_ACTION+" not found");
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mChoiceModelSavedState = new ViewModelProvider(this).get(ChoiceModel.SavedStateViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = LayoutChooserWithSearchBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        mBinding.list.setAdapter(adapter);
    }

    @SuppressWarnings("unchecked")
    public <T extends RecyclerView.Adapter<?>> T getAdapter() {
        return (T) mBinding.list.getAdapter();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
        if (hasExtraTitle()) {
            requireActivity().setTitle(getExtraTitle());
        }
        mBinding.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onChangeSearchQuery(s.toString());
            }
        });
        mBinding.btnPick.setOnClickListener(v->onClickPick());
        mBinding.btnClose.setOnClickListener(v->onClickClose());
        mChoiceModel = new ChoiceModel(mBinding.list,getChoiceModelCallback());
        String action = getAction();
        if (Constants.ACTION_PICK_MULTIPLE.equals(action)) {
            mChoiceModel.setChoiceMode(ChoiceModel.CHOICE_MODE_MULTIPLE);
        }
        else {
            mChoiceModel.setChoiceMode(ChoiceModel.CHOICE_MODE_SINGLE);
        }
        mChoiceModel.setOnItemCheckedListener(this);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (null != savedInstanceState) {
            mChoiceModel.onRestoreInstanceState(mChoiceModelSavedState);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mChoiceModel.onSaveInstanceState(mChoiceModelSavedState);
    }

    @NonNull
    public final RecyclerView getList() {
        return mBinding.list;
    }

    @NonNull
    public final EditText getSearchQueryInput() {
        return mBinding.search;
    }

    public CharSequence getSearchQueryText() {
        return mBinding.search.getEditableText().toString();
    }

    @NonNull
    public ChoiceModel getChoiceModel() {
        return mChoiceModel;
    }

    @NonNull
    public abstract ChoiceModel.Callback getChoiceModelCallback();

    public final void setResult(@NonNull Bundle result) {
        result.putInt(EXTRA_REQUEST_CODE,getExtraRequestCode());
        //noinspection ConstantConditions
        navController.getPreviousBackStackEntry().getSavedStateHandle().set(KEY_RESULT,result);
    }

    @NonNull
    protected Bundle onPrepareResult() {
        return new Bundle();
    }

    @Override
    public void onItemChecked(@NonNull RecyclerView recyclerView, @NonNull View view, int position, boolean checked) {}

    protected void onChangeSearchQuery(CharSequence query){}

    protected void onClickPick() {
        Bundle result = onPrepareResult();
        setResult(result);
        exit();
    }

    protected void onClickClose() { exit(); }

    protected final void exit() {
        navController.popBackStack();
    }
}
