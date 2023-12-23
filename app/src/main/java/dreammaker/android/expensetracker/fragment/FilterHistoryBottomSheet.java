package dreammaker.android.expensetracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.databinding.FragmentFilterHistoryBottomSheetBinding;

@SuppressWarnings("unused")
public class FilterHistoryBottomSheet extends Fragment {

    private static final String TAG = FilterHistoryBottomSheet.class.getSimpleName();

    public static final String EXTRA_HISTORY_FILTER_DATA = "history_filter_data";

    private NavController navController;

    private FragmentFilterHistoryBottomSheetBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentFilterHistoryBottomSheetBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);
    }
}
