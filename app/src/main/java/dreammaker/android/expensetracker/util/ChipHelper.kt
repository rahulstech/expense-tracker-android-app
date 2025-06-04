package dreammaker.android.expensetracker.util

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