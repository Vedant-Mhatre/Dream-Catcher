package com.vmhatre.dreamcatcher

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.vmhatre.dreamcatcher.databinding.FragmentPhotoDialogBinding
import java.io.File

class PhotoDialogFragment : DialogFragment() {

    private val args: PhotoDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = FragmentPhotoDialogBinding.inflate(layoutInflater)

        val photoFilename = args.dreamPhotoFilename
        val photoFile = File(requireContext().applicationContext.filesDir, photoFilename)

        binding.root.doOnLayout {
            val scaledBitmap = getScaledBitmap(photoFile.path, it.width, it.height)
            binding.photoDetail.setImageBitmap(scaledBitmap)
        }

        return AlertDialog.Builder(requireContext())
            .setView(binding.root)
            .create()
    }

}

