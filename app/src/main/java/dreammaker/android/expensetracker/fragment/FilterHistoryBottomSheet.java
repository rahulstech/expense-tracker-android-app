package dreammaker.android.expensetracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.datepicker.MaterialDatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import dreammaker.android.expensetracker.databinding.FragmentFilterHistoryBinding;

@SuppressWarnings("unused")
public class FilterHistoryBottomSheet extends Fragment {

    private static final String TAG = FilterHistoryBottomSheet.class.getSimpleName();

    public static final String EXTRA_HISTORY_FILTER_DATA = "history_filter_data";

    private NavController navController;

    private FragmentFilterHistoryBinding mBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentFilterHistoryBinding.inflate(inflater,container,false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        MaterialDatePicker.Builder<Pair<Long,Long>> picker = MaterialDatePicker.Builder.dateRangePicker();
        picker.build().showNow(getChildFragmentManager(),null);
    }
}
