package com.vmhatre.dreamcatcher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.text.format.DateFormat.format
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vmhatre.dreamcatcher.databinding.FragmentDreamDetailBinding
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt

class DreamDetailFragment : Fragment() {

    private val args: DreamDetailFragmentArgs by navArgs()
    private var _binding: FragmentDreamDetailBinding? = null
    private val binding get () = checkNotNull(_binding) { "FragmentDreamDetailBinding is null" }

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) {
        didTakePhoto ->
            Log.w("DDF", "Did take photo? $didTakePhoto")

        if (didTakePhoto){
            binding.dreamPhoto.tag = null
            vm.dream.value?.let { updatePhoto(it) }
        }
    }

    private val vm: DreamDetailViewModel by viewModels {
        DreamDetailViewModelFactory(args.dreamID)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentDreamDetailBinding.inflate(inflater, container, false)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_dream_detail, menu)

                val takePhotoIntent = takePhoto.contract.createIntent(
                    requireContext(),
                    Uri.EMPTY
                )

                menu.findItem(R.id.take_photo_menu).isVisible = canResolveIntent(
                    takePhotoIntent
                )
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when(menuItem.itemId) {

                    R.id.take_photo_menu -> {

                        vm.dream.value?.let { dream ->
                            val photoFile = File(
                                requireActivity().applicationContext.filesDir,
                                dream.photoFileName
                            )

                            val photoURI = FileProvider.getUriForFile(
                                requireContext(),
                                "com.vmhatre.dreamcatcher.fileprovider",
                                photoFile
                            )

                            takePhoto.launch(photoURI)
                        }

                        true
                    }
                    R.id.share_dream_menu -> {
                        vm.dream.value?.let { shareDream(it) }
                        true
                    }
                    else -> false
                }


            }

        }, viewLifecycleOwner)

        return binding.root
    }

    fun shareDream(dream: Dream) {

        val dateFormat = format("'Last updated' yyyy-MM-dd 'at' hh:mm:ss A",
            dream.lastUpdated).toString()

        val lastUpdatedText = dateFormat.format(dream.lastUpdated)

        val reflections = dream.entries
            .filter { it.kind == DreamEntryKind.REFLECTION }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = "\n * ", prefix = "Reflections:\n * ") { it.text }
            ?: ""

        
        val isDeferred = dream.entries.any { it.kind == DreamEntryKind.DEFERRED }
        val isFulfilled = dream.entries.any { it.kind == DreamEntryKind.FULFILLED }
        val statusLine = when {
            isDeferred -> context?.getString(R.string.dream_status_deferred)
            isFulfilled -> context?.getString(R.string.dream_status_fulfilled)
            else -> null
        }

        val shareText = buildString {
            append("${dream.title}\n")
            append("$lastUpdatedText\n")
            if (reflections.isNotEmpty()) append("$reflections\n")
            statusLine?.let { append(it) }
        }

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        context?.startActivity(shareIntent)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getItemTouchHelper().attachToRecyclerView(binding.dreamEntryRecycler)
        binding.dreamEntryRecycler.layoutManager = LinearLayoutManager(context)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.dream.collect{ dream ->
                    dream?.let {
                        updateView(dream)
                        binding.dreamEntryRecycler.adapter = DreamEntryAdapter(it.entries)
                    }
                }
            }
        }

        setFragmentResultListener(
            ReflectionDialogFragment.REFLECTION_TEXT
        ) { _, bundle ->
            val newReflectionString = bundle.getString(ReflectionDialogFragment.REFLECTION_TEXT)
            if (newReflectionString != null) {
                vm.updateDream { oldDream ->
                    oldDream.copy().apply {
                        entries = oldDream.entries + DreamEntry(
                            kind = DreamEntryKind.REFLECTION,
                            dreamId = oldDream.id,
                            text = newReflectionString
                        )
                    }
                }
            }
        }

        binding.titleText.doOnTextChanged { text, _, _, _ ->
            val newTitleString = text.toString()
            vm.updateDream { oldDream ->
                oldDream.copy(title = newTitleString).apply { entries = oldDream.entries }
            }
        }

        binding.dreamPhoto.setOnClickListener {
            Log.w("DDF dreamphoto", binding.dreamPhoto.tag.toString())
            if (binding.dreamPhoto.tag != null){
                findNavController().navigate(
                    DreamDetailFragmentDirections.showPhotoDetail(binding.dreamPhoto.tag.toString())
                )
            }
        }

        binding.fulfilledCheckbox.setOnClickListener {
            vm.updateDream { oldDream ->
                oldDream.copy().apply { entries =
                    if (oldDream.isFulfilled){
                        oldDream.entries.filter { it.kind != DreamEntryKind.FULFILLED }
                    } else {
                        oldDream.entries + DreamEntry (
                            kind = DreamEntryKind.FULFILLED,
                            dreamId = oldDream.id
                        )
                    }
                }
            }
        }

        binding.deferredCheckbox.setOnClickListener {
            vm.updateDream { oldDream ->
                oldDream.copy().apply { entries =
                    if (oldDream.isDeferred){
                        oldDream.entries.filter { it.kind != DreamEntryKind.DEFERRED }
                    } else {
                        oldDream.entries + DreamEntry (
                            kind = DreamEntryKind.DEFERRED,
                            dreamId = oldDream.id
                        )
                    }
                }
            }
        }

        binding.addReflectionButton.setOnClickListener{
            findNavController().navigate(
                DreamDetailFragmentDirections.addReflection()
            )
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun updateView(dream: Dream) {

        updatePhoto(dream)

        if (binding.titleText.text.toString() != dream.title) {
            binding.titleText.setText(dream.title)
        }

        binding.fulfilledCheckbox.isChecked = dream.isFulfilled
        binding.deferredCheckbox.isChecked = dream.isDeferred
        binding.fulfilledCheckbox.isEnabled = !dream.isDeferred
        binding.deferredCheckbox.isEnabled = !dream.isFulfilled

        if (dream.isFulfilled) {
            binding.addReflectionButton.hide()
        } else {
            binding.addReflectionButton.show()
        }

        binding.lastUpdatedText.text = format("'Last updated' yyyy-MM-dd 'at' hh:mm:ss A",
            dream.lastUpdated).toString()
    }

    private fun updatePhoto(dream: Dream){
        with(binding.dreamPhoto) {
            if (tag != dream.photoFileName) {
                val photoFile =
                    File(requireContext().applicationContext.filesDir, dream.photoFileName)

                if (photoFile.exists()) {
                    doOnLayout { imageView ->
                        tag = dream.photoFileName
                        val scaledBitmap = getScaledBitmap(
                            photoFile.path,
                            imageView.width,
                            imageView.height
                        )
                        setImageBitmap(scaledBitmap)

                        isEnabled = true
                    }
                } else {
                    setImageBitmap(null)
                    tag = null
                    isEnabled = false
                }
            }
        }
    }

    private fun canResolveIntent(intent: Intent): Boolean{
        return requireActivity().packageManager.resolveActivity(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ) != null
    }

    private fun getItemTouchHelper(): ItemTouchHelper {
        return ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(0, 0) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ) : Boolean {
                    return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deHolder = viewHolder as DreamEntryHolder
                val swipedEntry = deHolder.boundEntry
                vm.updateDream { oldDream ->
                    oldDream.copy()
                        .apply {
                            entries = oldDream.entries
                                .filter { it != swipedEntry }
                        }
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val deHolder = viewHolder as DreamEntryHolder
                val swipedEntry = deHolder.boundEntry
                return if (swipedEntry.kind == DreamEntryKind.REFLECTION){
                    ItemTouchHelper.LEFT
                } else {0}
            }
        })
    }

}