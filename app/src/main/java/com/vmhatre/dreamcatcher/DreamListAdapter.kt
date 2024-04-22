package com.vmhatre.dreamcatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vmhatre.dreamcatcher.databinding.ListItemDreamBinding
import java.util.UUID

class DreamHolder(private val binding: ListItemDreamBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var boundDream: Dream  private set

    fun bind(dream: Dream, onDreamClicked: (UUID) -> Unit) {

        boundDream = dream

        binding.root.setOnClickListener{
            onDreamClicked(dream.id)
        }

        binding.listItemTitle.text = dream.title

        when {
            dream.isDeferred -> {
                binding.listItemImage.setImageResource(R.drawable.ic_dream_deferred)
                binding.listItemImage.visibility = View.VISIBLE
            }

            dream.isFulfilled -> {
                binding.listItemImage.setImageResource(R.drawable.ic_dream_fulfilled)
                binding.listItemImage.visibility = View.VISIBLE
            }

            else -> {
                binding.listItemImage.visibility = View.GONE
            }
        }

        val reflectionCount = dream.entries.count { it.kind == DreamEntryKind.REFLECTION }
        binding.listItemReflectionCount.text = binding.root.context.getString(R.string.dream_reflection_count, reflectionCount)

    }

}

class DreamListAdapter(private val dreams: List<Dream>,
                       private val onDreamClicked: (UUID) -> Unit) : RecyclerView.Adapter<DreamHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamBinding.inflate(inflater, parent, false)
        return DreamHolder(binding)
    }

    override fun getItemCount(): Int {
        return dreams.size
    }

    override fun onBindViewHolder(holder: DreamHolder, position: Int) {
        holder.bind(dreams[position], onDreamClicked)
    }

}