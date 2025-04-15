package dreammaker.android.expensetracker.ui.history

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.ListContentBinding

class HistoryListFragment : Fragment() {

    private val TAG = HistoryListFragment::class.simpleName

    private lateinit var binding: ListContentBinding

    private lateinit var viewModel: HistoryListViewModel

    private lateinit var adapter: HistoryListAdapter

    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(requireActivity(),ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[HistoryListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ListContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        binding.list.visibility = View.VISIBLE
        binding.empty.visibility = View.GONE
        binding.list.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = HistoryListAdapter()
        binding.list.adapter = adapter
        viewModel.getHistories().observe(getViewLifecycleOwner()) {
            Log.i(TAG,"histories fetched ${it.size}")
            adapter.submitList(it)
        }
        binding.add.setOnClickListener{ onClickCreateHistory() }
    }

    private fun onClickCreateHistory() {
        navController.navigate(R.id.action_history_list_to_history_input)
    }
}