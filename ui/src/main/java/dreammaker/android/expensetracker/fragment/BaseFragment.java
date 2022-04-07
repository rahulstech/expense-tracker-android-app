package dreammaker.android.expensetracker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import dreammaker.android.expensetracker.activity.BaseActivity;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.OnBackPressListener;

public abstract class BaseFragment<VH extends BaseFragment.FragmentViewHolder> extends Fragment
        implements OnBackPressListener, SearchView.OnQueryTextListener {

    private static final String TAG = "BaseFragment";

    private VH viewHolder;
    private String lastQuery = null;
    private String fragmentTitle;
    private String fragmentSubTitle;

    BaseFragment(){}

    public void setFragmentTitle(String title) {
        this.fragmentTitle = title;
    }

    public void setFragmentTitle(@StringRes int resId) {
        setFragmentTitle(getString(resId));
    }

    public void setFragmentSubTitle(String subTitle) {
        this.fragmentSubTitle = subTitle;
    }

    public void setFragmentSubTitle(@StringRes int resId) {
        setFragmentSubTitle(getString(resId));
    }

    public java.lang.String getFragmentTitle() {
        return fragmentTitle;
    }

    public String getFragmentSubTitle() {
        return fragmentSubTitle;
    }

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (Check.isNull(viewHolder)){ createFragmentViewHolder(inflater, container); }
        return viewHolder.getRoot();
    }

    public boolean isViewHolderCreated(){ return Check.isNonNull(viewHolder); }

    @Override
    public final void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        bindFragmentViewHolder();
    }

    @Override
    public void onPause() {
        unregisterOnBackPressListener();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerOnBackPressListener();
    }

    public boolean hasArguments(){ return Check.isNonNull(getArguments()); }

    public boolean onBackPressed(){ return false; }

    @Override
    public boolean onQueryTextChange(String newText) {
        this.lastQuery = newText;
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {return false;}

    @NonNull
    protected abstract VH onCreateFragmentViewHolder(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    protected void onFragmentViewHolderCreated(@NonNull VH vh) {}

    protected void onBindFragmentViewHolder(@NonNull VH vh){}

    protected VH getViewHolder() { return viewHolder; }

    protected SearchView onPrepareSearchMenu(MenuItem item, @StringRes int queryHint, String query){
        SearchView search = (SearchView) item.getActionView();
        search.setQueryHint(getString(queryHint));
        search.setOnQueryTextListener(this);
        if (!Check.isEmptyString(query)){
            item.expandActionView();
            search.setIconified(false);
            search.setQuery(query, false);
        }
        return search;
    }

    protected String getQuery(){ return lastQuery; }

    private void createFragmentViewHolder(LayoutInflater inflater, ViewGroup container){
        viewHolder = onCreateFragmentViewHolder(inflater, container);
        Check.isNonNull(viewHolder, "onCreateFragmentViewHolder returned null");
        onFragmentViewHolderCreated(viewHolder);
    }

    private void bindFragmentViewHolder(){
        if (isViewHolderCreated()) {
            onBindFragmentViewHolder(viewHolder);
        }
    }

    private void registerOnBackPressListener(){
        if (getActivity() instanceof BaseActivity){
            ((BaseActivity) getActivity()).registerOnBackPressListener(this);
        }
    }

    private void unregisterOnBackPressListener() {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).unregisterOnBackPressListener(this);
        }
    }

    public static abstract class FragmentViewHolder{
        private View root;

        FragmentViewHolder(@NonNull View root){ this.root = root; }

        public View getRoot(){
            return root;
        }

        @SuppressWarnings("unchecked")
        public <V extends  View> V findViewById(@IdRes int resId){ return (V) root.findViewById(resId); }
    }
}
