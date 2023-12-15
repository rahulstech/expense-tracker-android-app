package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.database.type.Currency;
import dreammaker.android.expensetracker.database.view.AssetLiabilitySummary;
import dreammaker.android.expensetracker.databinding.FragmentHomeBinding;
import dreammaker.android.expensetracker.viewmodel.HomeViewModel;

@SuppressWarnings("unused")
public class HomeFragment extends Fragment {

    private static final String TAG = "Home";

    private HomeViewModel mViewModel;

    private NavController navController;

    private FragmentHomeBinding mBinding;

    public HomeFragment() {super();}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mViewModel = new ViewModelProvider(this,(ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHomeBinding.inflate(inflater,container,false);
        mViewModel.getAssetLiabilitySummaryLiveData().observe(getViewLifecycleOwner(),this::onAssetLiabilitySummeryFetched);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle();
        navController = Navigation.findNavController(view);
        mBinding.navAccounts.setOnClickListener(v -> onClickAccounts());
        mBinding.navPeople.setOnClickListener(v -> onClickPeople());
        mBinding.navHistories.setOnClickListener(v -> onClickHistories());
        mBinding.addHistory.setOnClickListener(v->onClickAddHistory());
    }

    private void setTitle() {
        requireActivity().setTitle(R.string.app_name);
    }

    private void onClickAccounts() {
        navController.navigate(R.id.action_home_to_accounts);
    }

    private void onClickPeople() {
        navController.navigate(R.id.action_home_to_people);
    }

    private void onClickHistories() {
        navController.navigate(R.id.action_home_to_histories);
    }

    private void onClickAddHistory() {
        navController.navigate(R.id.action_home_to_add_history);
    }

    private void onAssetLiabilitySummeryFetched(@Nullable AssetLiabilitySummary summary) {
        if (null == summary) {
            showAsset(Currency.ZERO);
            showLiability(Currency.ZERO);
        }
        else {
            showAsset(summary.getTotalAsset());
            showLiability(summary.getTotalLiability());
        }
    }

    private void showAsset(Currency asset) {
        mBinding.totalAsset.setText(asset.toString());
    }

    private void showLiability(Currency liability) {
        mBinding.totalLiability.setText(liability.toString());
    }
}
