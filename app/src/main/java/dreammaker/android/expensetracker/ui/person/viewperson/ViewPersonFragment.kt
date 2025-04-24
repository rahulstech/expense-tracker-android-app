package dreammaker.android.expensetracker.ui.person.viewperson

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import dreammaker.android.expensetracker.R

class ViewPersonFragment: Fragment() {

    private val TAG = ViewPersonFragment::class.simpleName

    private lateinit var viewModel: ViewPersonViewModel
    private lateinit var navController: NavController

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application))[ViewPersonViewModel::class.java]
        setHasOptionsMenu(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.view_person_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete -> {
                onClickDelete()
                true
            }
            R.id.edit -> {
                onClickEdit()
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun onClickDelete() {}

    private fun onClickEdit() {}

    override fun onDestroyView() {
        super.onDestroyView()
    }
}