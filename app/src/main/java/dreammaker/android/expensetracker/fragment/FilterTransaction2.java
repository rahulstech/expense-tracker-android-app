package dreammaker.android.expensetracker.fragment;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager.widget.ViewPager;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.viewmodel.TransactionsViewModel;

public class FilterTransaction2 extends BaseFragment<FilterTransaction2.FilterTransaction2ViewHolder>
        implements View.OnClickListener {
    private static final String TAG = "FilterTransaction2";

    private TransactionsViewModel viewModel;
    private NavController navController;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setFragmentTitle(R.string.label_filter_transaction);
        if (Check.isNonNull(getActivity())){
            viewModel = new ViewModelProvider(getActivity(),
                    new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                    .get(TransactionsViewModel.class);
        }
    }

    @NonNull
    @Override
    protected FilterTransaction2ViewHolder onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return new FilterTransaction2ViewHolder(
                inflater.inflate(R.layout.layout_filter_transaction, container, false));
    }

    @Override
    protected void onFragmentViewHolderCreated(@NonNull FilterTransaction2ViewHolder vh) {
        List<Screen> screens = getScreens();
        FilterTransactionsScreensAdapter filterTransactionsScreensAdapter = new FilterTransactionsScreensAdapter(getChildFragmentManager(), screens);
        vh.cancel.setOnClickListener(this);
        vh.reset.setOnClickListener(this);
        vh.filter.setOnClickListener(this);
        vh.screens.setAdapter(filterTransactionsScreensAdapter);
    }

    @Override
    protected void onBindFragmentViewHolder(@NonNull FilterTransaction2ViewHolder vh) {
        navController = Navigation.findNavController(vh.getRoot());
        vh.screens.setCurrentItem(0);
    }

    @Override
    public void onClick(View v) {
        if (v == getViewHolder().cancel){
            onCancel();
        }
        else if (v == getViewHolder().reset){
            onReset();
        }
        else if (v == getViewHolder().filter){
            onFilter();
        }
    }

    private List<Screen> getScreens(){
        ArrayList<Screen> screens = new ArrayList<>();
        screens.add(new Screen(Screen.SCREEN_ACCOUNTS, getString(R.string.account)));
        screens.add(new Screen(Screen.SCREEN_PEOPLE, getString(R.string.person)));
        screens.add(new Screen(Screen.SCREEN_DATES, getString(R.string.screen_dates)));
        screens.add(new Screen(Screen.SCREEN_MISCELLANEOUS,getString(R.string.screen_miscellaneous)));
        return screens;
    }

    private void onFilter() {
        Log.d(TAG, "onFilter");
        viewModel.filter(viewModel.getWorkingFilterParams());
        navController.popBackStack();
    }

    private void onReset() {
        viewModel.resetToTop();
        navController.popBackStack();
    }

    private void onCancel() { navController.popBackStack(); }

    public static class Screen{
        public static final int SCREEN_ACCOUNTS = 1;
        public static final int SCREEN_PEOPLE = 2;
        public static final int SCREEN_DATES = 4;
        public static final int SCREEN_MISCELLANEOUS = 5;

        public int which;
        public String label;

        public Screen(int which, String label){
            this.which = which;
            this.label = label;
        }
    }

    public static class FilterTransaction2ViewHolder extends BaseFragment.FragmentViewHolder{

        TabLayout navigationTabs;
        ViewPager screens;
        Button cancel;
        Button reset;
        Button filter;

        public FilterTransaction2ViewHolder(@NonNull View root) {
            super(root);
            navigationTabs = findViewById(R.id.navigation_tabs);
            screens = findViewById(R.id.screens);
            cancel = findViewById(R.id.btn_cancel);
            reset = findViewById(R.id.reset);
            filter = findViewById(R.id.filter);
            navigationTabs.setupWithViewPager(screens);
        }
    }

    private static class FilterTransactionsScreensAdapter extends FragmentPagerAdapter {

        private List<Screen> screens;
        private SparseArray<Fragment> fragments;

        public FilterTransactionsScreensAdapter(@NonNull FragmentManager fm, List<Screen> screens) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.screens = screens;
            this.fragments = new SparseArray<>();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            int which = screens.get(position).which;
            Fragment fragment = fragments.get(which, null);
            if (Check.isNull(fragment)) {
                switch (which) {
                    case Screen.SCREEN_ACCOUNTS:
                        fragment = new FilterTransactionsScreenAccounts();
                        break;
                    case Screen.SCREEN_PEOPLE:
                        fragment = new FilterTransactionScreenPeople();
                        break;
                    case Screen.SCREEN_DATES:
                        fragment = new FilterTransactionScreenDates();
                        break;
                    case Screen.SCREEN_MISCELLANEOUS:
                        fragment = new FilterTransactionScreenMiscellaneous();
                        break;
                }
                if (Check.isNonNull(fragment))
                    fragments.put(which, fragment);
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return screens.size() ;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return screens.get(position).label;
        }
    }
}
