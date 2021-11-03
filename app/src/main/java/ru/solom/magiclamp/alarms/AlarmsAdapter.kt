package ru.solom.magiclamp.alarms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.solom.magiclamp.R
import ru.solom.magiclamp.databinding.LayoutItemAlarmBinding

class AlarmsAdapter(
    private var data: List<AlarmState> = emptyList(),
    private val onTimeClick: (AlarmState) -> Unit,
    private val onEnabledSwitch: (AlarmState, Boolean) -> Unit
) : RecyclerView.Adapter<AlarmsAdapter.AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount() = data.size

    fun update(newData: List<AlarmState>) {
        val diff = AlarmsDiffUtil(data, newData)
        data = newData
        DiffUtil.calculateDiff(diff).dispatchUpdatesTo(this)
    }

    inner class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = LayoutItemAlarmBinding.bind(itemView)
        private val weekDays = itemView.context.resources.getStringArray(R.array.week_days)
        private var data: AlarmState? = null
        private val switchCheckedListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            data?.let { onEnabledSwitch(it, isChecked) }
        }

        init {
            binding.time.setOnClickListener {
                data?.let(onTimeClick)
            }
        }

        fun bind(state: AlarmState) {
            data = state
            binding.switchStatus.text = weekDays[state.dayNum]
            binding.switchStatus.silentSwitchCheck(state.isOn)
            binding.time.text = state.time.toString()
        }

        private fun SwitchCompat.silentSwitchCheck(isChecked: Boolean) {
            setOnCheckedChangeListener(null)
            this.isChecked = isChecked
            setOnCheckedChangeListener(switchCheckedListener)
        }
    }

    class AlarmsDiffUtil(
        private val oldList: List<AlarmState>,
        private val newList: List<AlarmState>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return areContentsTheSame(oldItemPosition, newItemPosition)
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].dayNum == newList[newItemPosition].dayNum &&
                    oldList[oldItemPosition].isOn == newList[newItemPosition].isOn &&
                    oldList[oldItemPosition].time == newList[newItemPosition].time
        }
    }
}
