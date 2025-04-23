package dreammaker.android.expensetracker.ui.person.personlist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.PeopleListBinding
import dreammaker.android.expensetracker.ui.util.Constants
import dreammaker.android.expensetracker.ui.util.visibilityGone
import dreammaker.android.expensetracker.ui.util.visible

class PeopleListFragment : Fragment() {

    private val TAG = PeopleListFragment::class.simpleName

    private var _binding: PeopleListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: PeopleListViewModel
    private lateinit var adapter: PeopleListAdapter
    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[PeopleListViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PeopleListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
        binding.list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = PeopleListAdapter()
        adapter.itemClickListener = { _,_,position -> handleClickPerson(adapter.currentList[position]) }
        binding.list.adapter = adapter
        binding.add.setOnClickListener {
            navController.navigate(R.id.action_persons_list_to_create_person, Bundle().apply {
                putString(Constants.ARG_ACTION, Constants.ACTION_CREATE)
            })
        }
        viewModel.getAllPeople().observe(viewLifecycleOwner, this::onPeopleLoaded)
    }

    private fun onPeopleLoaded(people: List<PersonModel>) {
        adapter.submitList(people)
        if (people.isEmpty()) {
            binding.list.visibilityGone()
            binding.emptyView.visible()
        }
        else {
            binding.emptyView.visibilityGone()
            binding.list.visible()
        }
    }

    private fun handleClickPerson(person: PersonModel) {
        navController.navigate(R.id.action_persons_list_to_view_person, Bundle().apply {
            putLong(Constants.ARG_ID,person.id!!)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.person_list_menu, menu)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}