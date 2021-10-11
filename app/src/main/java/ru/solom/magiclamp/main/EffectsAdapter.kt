package ru.solom.magiclamp.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.solom.magiclamp.R
import ru.solom.magiclamp.data.EffectDto
import ru.solom.magiclamp.databinding.ItemEffectBinding

class EffectsAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<EffectsAdapter.EffectViewHolder>() {
    private var currentId: Int = 0
    private var data: List<EffectDto> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EffectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_effect, parent, false)
        return EffectViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: EffectViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    fun updateEffects(newList: List<EffectDto>) {
        val diff = DiffUtil.calculateDiff(EffectsDiff(data, newList))
        data = newList
        diff.dispatchUpdatesTo(this)
    }

    fun updateCurrent(id: Int) {
        val oldIdx = data.indexOfFirst { it.id == currentId }
        val newIdx = data.indexOfFirst { it.id == id }
        currentId = id
        notifyItemChanged(oldIdx)
        notifyItemChanged(newIdx)
    }

    inner class EffectViewHolder(itemView: View, private val onItemClick: (Int) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val binding = ItemEffectBinding.bind(itemView)

        fun bind(effect: EffectDto) {
            binding.imgCurrent.isInvisible = effect.id != currentId
            binding.name.text = effect.name
            binding.root.setOnClickListener {
                onItemClick(effect.id)
            }
        }
    }
}

private class EffectsDiff(
    private val oldList: List<EffectDto>,
    private val newList: List<EffectDto>
) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size
    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].name == newList[newItemPosition].name
    }
}
