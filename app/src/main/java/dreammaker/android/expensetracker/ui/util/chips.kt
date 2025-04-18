package dreammaker.android.expensetracker.ui.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
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

class SelectionChipMaker<KeyType: Any>(
    val chipContainer: ViewGroup,
    val chipBuilder: (KeyType)->Chip?,
    val chipCloseListener: ((Chip, KeyType)->Unit)?
) {

    private val keyToChipMap = mutableMapOf<KeyType,Chip>()

    fun addChip(key: KeyType) {
        val chip = chipBuilder(key) ?: return
        chip.isCheckable = false
        chip.setOnCloseIconClickListener {
            removeChip(key)
            chipCloseListener?.invoke(chip, key)
        }
        chipContainer.addView(chip)
        keyToChipMap[key] = chip
    }

    fun addChips(keys: Set<KeyType>) {
        keys.forEach { addChip(it) }
    }

    fun removeChip(key: KeyType) {
        val chip = keyToChipMap[key]
        chipContainer.removeView(chip)
    }

    fun removeAllChips() {
        keyToChipMap.forEach {
            chipContainer.removeView(it.value)
        }
        keyToChipMap.clear()
    }
}