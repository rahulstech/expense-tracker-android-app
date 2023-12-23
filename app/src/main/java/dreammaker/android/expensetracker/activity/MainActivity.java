package dreammaker.android.expensetracker.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.databinding.MainBinding;

public class MainActivity extends AppCompatActivity implements ActivityModelProvider {

    private ActivityModel mActivityModel;

    private MainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityModel = new ActivityModel(this);
        mBinding = MainBinding.inflate(getLayoutInflater(),null,false);
        setContentView(mBinding.getRoot());
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_container);
        NavController navController = navHostFragment.getNavController();
        setSupportActionBar(mBinding.actionBar.toolbar);
        mActivityModel.setSupportToolbar(mBinding.actionBar.toolbar);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mBinding.drawerLayout,
                mActivityModel.getSupportToolbar(), 0, 0);
        drawerToggle.setDrawerSlideAnimationEnabled(true);
        mBinding.drawer.setNavigationItemSelectedListener(this::onClickLeftDrawerItem);
        //noinspection ConstantConditions
        NavigationUI.setupWithNavController(mActivityModel.getSupportToolbar(),navController,mBinding.drawerLayout);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        checkFirstRestoreRequired();
        //checkForAutoDelete();
    }

    private void checkFirstRestoreRequired() {
        /*BackupRestoreHelper.checkAppFirstRestoreRequired(this, isRequired -> {
            if (isRequired)
                startActivity(new Intent(MainActivity.this,RestoreActivity.class));
        });*/
    }

    @Override
    public void onBackPressed() {
        if (shouldPopBack()) {
            super.onBackPressed();
        }
    }

    private boolean shouldPopBack() {
        if (mBinding.drawerLayout.isDrawerOpen(mBinding.drawer)) {
            mBinding.drawerLayout.closeDrawer(mBinding.drawer);
            return false;
        }
        else if (mActivityModel.onBackPressed()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public ActivityModel getActivityModel() {
        return mActivityModel;
    }

    private boolean onClickLeftDrawerItem(MenuItem item) {
        mBinding.drawerLayout.closeDrawer(mBinding.drawer);
        int id = item.getItemId();
        if (R.id.backup_restore == id) {
            //startActivity(new Intent(requireContext(), BackupRestoreActivity.class));
            return true;
        }
        else if (R.id.settings == id) {
            //startActivity(new Intent(requireContext(), SettingsActivity.class));
            return true;
        }
        return false;
    }
}
