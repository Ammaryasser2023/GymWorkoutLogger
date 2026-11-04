package com.example.gymworkoutlogger.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import com.example.gymworkoutlogger.databinding.ItemHistorySessionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (WorkoutSessionWithSets) -> Unit,
    private val onDeleteClick: (WorkoutSessionWithSets) -> Unit
) : ListAdapter<WorkoutSessionWithSets, HistoryAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistorySessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistorySessionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkoutSessionWithSets) {
            binding.tvHistoryTitle.text = item.session.title
            val totalSets = item.sets.size
            val totalVolume = item.sets.sumOf { it.weightKg * it.reps }
            binding.tvHistoryDetails.text = "$totalSets Sets • ${totalVolume.toInt()} kg Volume"

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvHistoryDate.text = dateFormat.format(Date(item.session.startTime))

            binding.root.setOnClickListener { onItemClick(item) }
            binding.btnDeleteHistory.setOnClickListener { onDeleteClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WorkoutSessionWithSets>() {
        override fun areItemsTheSame(
            oldItem: WorkoutSessionWithSets,
            newItem: WorkoutSessionWithSets
        ): Boolean = oldItem.session.id == newItem.session.id

        override fun areContentsTheSame(
            oldItem: WorkoutSessionWithSets,
            newItem: WorkoutSessionWithSets
        ): Boolean = oldItem == newItem
    }
}
