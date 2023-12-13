package dreammaker.android.expensetracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import dreammaker.android.expensetracker.R;

public class MainActivity extends AppCompatActivity {

    @Deprecated
    public static void showQuickMessage(Activity activity, @StringRes int messageID, @StringRes int actionID) {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).showQuickMessage(activity.getString(messageID), activity.getString(actionID),
                    v -> ((MainActivity) activity).dismissQuickMessage());
        }
    }

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_container);
        navController = navHostFragment.getNavController();
        /*drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer);

        setSupportActionBar(toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, 0, 0);
        drawerToggle.setDrawerSlideAnimationEnabled(true);
        drawer.setNavigationItemSelectedListener(this::onClickLeftDrawerItem);
        NavigationUI.setupWithNavController(toolbar,navHostFragment.getNavController(),drawerLayout);*/
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
        //if (!dismissQuickMessage()) {
            //if (drawerLayout.isDrawerOpen(drawer))
            //    drawerLayout.closeDrawer(drawer);
            //else
            //    super.onBackPressed();
        //}
        if (!navController.popBackStack()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean onClickLeftDrawerItem(MenuItem item) {
        //drawerLayout.closeDrawer(drawer);
        int id = item.getItemId();
        if (R.id.backup_restore == id) {
            startActivity(new Intent(MainActivity.this, BackupRestoreActivity.class));
            return true;
        }
        else if (R.id.settings == id) {
            startActivity(new Intent(this,SettingsActivity.class));
            return true;
        }
        return false;
    }

    public void showQuickMessage(String message, String action, View.OnClickListener onClickAction) {
        dismissQuickMessage();
        //current = Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
        //if (!Check.isEmptyString(action)) current.setAction(action, v -> {
        //    if (null != onClickAction)
        //        onClickAction.onClick(v);
       // });
        //current.show();
    }

    public boolean dismissQuickMessage() {
        //if (Check.isNonNull(current) && current.isShown()) {
        //    current.dismiss();
        //    current = null;
        //    return true;
        //}
        return false;
    }
}
