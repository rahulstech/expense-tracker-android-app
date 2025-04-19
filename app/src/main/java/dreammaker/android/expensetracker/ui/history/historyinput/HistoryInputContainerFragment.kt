package dreammaker.android.expensetracker.ui.history.historyinput

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.HistoryInputContainerBinding

class HistoryInputContainerFragment : Fragment() {

    private lateinit var binding: HistoryInputContainerBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HistoryInputContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navHostFragment = childFragmentManager.findFragmentById(R.id.container) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.history_input_navigation, arguments)
    }
}
