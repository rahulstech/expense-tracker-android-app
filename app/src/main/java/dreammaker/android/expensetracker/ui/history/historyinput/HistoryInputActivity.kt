package dreammaker.android.expensetracker.ui.history.historyinput

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dreammaker.android.expensetracker.Constants
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.ActivityHistoryInputBinding

class HistoryInputActivity: AppCompatActivity() {

    private lateinit var binding: ActivityHistoryInputBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        navController = binding.navHostFragmentContainer.getFragment<NavHostFragment>().navController
        val graph = navController.navInflater.inflate(R.navigation.history_input_activity_navigation)
        val historyInputType = intent.extras?.getString(Constants.ARG_HISTORY_INPUT_TYPE, Constants.HISTORY_INPUT_TYPE_TRANSACTION)
        if (historyInputType== Constants.HISTORY_INPUT_TYPE_MONEY_TRANSFER) {
            graph.setStartDestination(R.id.input_money_transfer)
        }

        navController.setGraph(graph, intent.extras)
    }
}