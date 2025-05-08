package dreammaker.android.expensetracker.ui.util

import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import dreammaker.android.expensetracker.databinding.PickerChipBinding

fun createInputChip(parent: ViewGroup, contentText: String, canClose: Boolean = true): Chip {
    val inflater = LayoutInflater.from(parent.context)
    val binding = PickerChipBinding.inflate(inflater, parent, false)
    return binding.root.apply {
        text = contentText
        isCloseIconVisible = canClose
        isCheckable = false
        isCheckedIconVisible = false
    }
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