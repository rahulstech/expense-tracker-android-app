package dreammaker.android.expensetracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.backup.BackupRestoreHelper;
import dreammaker.android.expensetracker.util.CalculatorKeyboard;
import dreammaker.android.expensetracker.util.Check;

public class MainActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView drawer;
    private Toolbar toolbar;
    private NavHostFragment navHostFragment;
    private Snackbar current;
    private KeyboardView keyboardView;
    private CalculatorKeyboard calculatorKeyboard;

    public static void showQuickMessage(Activity activity, @StringRes int messageID, @StringRes int actionID) {
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).showQuickMessage(activity.getString(messageID), activity.getString(actionID),
                    v -> ((MainActivity) activity).dismissQuickMessage());
        }
    }

    public NavController getNavController() {
        return navHostFragment.getNavController();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_container);
        drawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer);
        keyboardView = findViewById(R.id.calculator_keyboard);
        calculatorKeyboard = new CalculatorKeyboard(keyboardView);

        setSupportActionBar(toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, 0, 0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle.setDrawerIndicatorEnabled(false);
        drawerToggle.setDrawerSlideAnimationEnabled(true);
        drawerToggle.setHomeAsUpIndicator(R.drawable.hamburger);
        drawerToggle.setToolbarNavigationClickListener(v -> drawerLayout.openDrawer(drawer, true));
        drawer.setNavigationItemSelectedListener(item ->
                onClickLeftDrawerItem(item));
        onCheckFirstBackupRequired();
    }

    private void onCheckFirstBackupRequired() {
        BackupRestoreHelper.checkAppFirstRestoreRequired(this, isRequired -> {
            // TODO: implement this
        });
    }

    @Override
    public void onBackPressed() {
        if (!dismissQuickMessage()) {
            if (!calculatorKeyboard.onBackPressed()) {
                if (drawerLayout.isDrawerOpen(drawer))
                    drawerLayout.closeDrawer(drawer);
                else
                    super.onBackPressed();
            }
        }
        return;
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
        drawerLayout.closeDrawer(drawer);
        int id = item.getItemId();
        if (R.id.backup_restore == id) {
            startActivity(new Intent(MainActivity.this, BackupRestoreActivity.class));
            return true;
        }
        return false;
    }

    public void showQuickMessage(String message, String action, View.OnClickListener onClickAction) {
        dismissQuickMessage();
        current = Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG);
        if (!Check.isEmptyString(action)) current.setAction(action, v -> {
            if (null != onClickAction)
                onClickAction.onClick(v);

        });
        current.show();
    }

    public boolean dismissQuickMessage() {
        if (Check.isNonNull(current) && current.isShown()) {
            current.dismiss();
            current = null;
            return true;
        }
        return false;
    }

    public CalculatorKeyboard getCalculatorKeyboard() {
        return calculatorKeyboard;
    }
}
