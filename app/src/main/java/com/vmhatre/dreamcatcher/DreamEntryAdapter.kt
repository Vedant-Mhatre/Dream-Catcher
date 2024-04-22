package com.vmhatre.dreamcatcher

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.vmhatre.dreamcatcher.databinding.ListItemDreamEntryBinding


class DreamEntryHolder(private val binding: ListItemDreamEntryBinding) : RecyclerView.ViewHolder(binding.root) {

    lateinit var boundEntry: DreamEntry
        private set
    fun bind(entry: DreamEntry){
        boundEntry = entry
        binding.dreamEntryButton.configureForEntry(entry)
    }

    private fun Button.configureForEntry (entry: DreamEntry){
        text = entry.kind.toString()
        visibility = View.VISIBLE

        when(entry.kind){
            DreamEntryKind.REFLECTION ->{
                text = entry.text
                isAllCaps = false
                setBackgroundWithContrastingText("red")
            }
            DreamEntryKind.DEFERRED ->{
                setBackgroundWithContrastingText("black")
            }
            DreamEntryKind.FULFILLED ->{
                setBackgroundWithContrastingText("green")
            }
            DreamEntryKind.CONCEIVED ->{
                setBackgroundWithContrastingText("blue")
            }
        }
    }

}

class DreamEntryAdapter(private val entries: List<DreamEntry>) : RecyclerView.Adapter<DreamEntryHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DreamEntryHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemDreamEntryBinding.inflate(inflater, parent, false)
        return DreamEntryHolder(binding)
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    override fun onBindViewHolder(holder: DreamEntryHolder, position: Int) {
        holder.bind(entries[position])
    }
}