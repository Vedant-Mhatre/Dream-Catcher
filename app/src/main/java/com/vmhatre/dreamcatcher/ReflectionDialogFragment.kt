package com.vmhatre.dreamcatcher

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.vmhatre.dreamcatcher.databinding.FragmentReflectionDialogBinding

class ReflectionDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val binding = FragmentReflectionDialogBinding.inflate(layoutInflater)

        val positiveListener = DialogInterface.OnClickListener { _, _ ->
            val resultText = binding.reflectionText.text.toString()
            setFragmentResult(
                REFLECTION_TEXT,
                bundleOf(REFLECTION_TEXT to resultText)
            )
        }


        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .setTitle(R.string.reflection_dialog_title) // "Add Reflection"
            .setPositiveButton(R.string.reflection_dialog_positive, positiveListener) // "Add"
            .setNegativeButton(R.string.reflection_dialog_negative, null) // "Cancel"
            .show()
    }

    companion object {
        const val REFLECTION_TEXT = "REFLECTION_TEXT"
    }
}