package dreammaker.android.expensetracker.ui.util

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.database.AccountModel
import dreammaker.android.expensetracker.database.PersonModel
import dreammaker.android.expensetracker.databinding.PickerChipBinding

fun createAccountChip(context: Context, account: AccountModel, canClose: Boolean = true): Chip {
    val inflater = LayoutInflater.from(context)
    val binding = PickerChipBinding.inflate(inflater)
    val chip = binding.root.apply {
        isCloseIconVisible = canClose
        text = account.name
    }
    return chip
}

fun createPersonChip(context: Context, person: PersonModel, canClose: Boolean = true): Chip {
    val inflater = LayoutInflater.from(context)
    val binding = PickerChipBinding.inflate(inflater)
    val chip = binding.root.apply {
        isCloseIconVisible = canClose
        text = person.name
    }
    return chip
}