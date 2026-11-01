package com.example.gymworkoutlogger.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets
import com.example.gymworkoutlogger.databinding.ItemRecentWorkoutBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentWorkoutAdapter(
    private val onItemClick: (WorkoutSessionWithSets) -> Unit
) : ListAdapter<WorkoutSessionWithSets, RecentWorkoutAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecentWorkoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRecentWorkoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: WorkoutSessionWithSets) {
            binding.tvWorkoutTitle.text = item.session.title
            val totalSets = item.sets.size
            val totalVolume = item.sets.sumOf { it.weightKg * it.reps }
            binding.tvWorkoutSubtitle.text = "$totalSets Sets • ${totalVolume.toInt()} kg Total Volume"

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvWorkoutDate.text = dateFormat.format(Date(item.session.startTime))

            binding.root.setOnClickListener { onItemClick(item) }
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
