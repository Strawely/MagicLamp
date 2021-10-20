package ru.solom.magiclamp.alarms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.solom.magiclamp.R

class AlarmsAdapter : RecyclerView.Adapter<AlarmsAdapter.AlarmViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = WEEK_LENGTH

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
            /* no-op */
        }
    }
}

private const val WEEK_LENGTH = 7
