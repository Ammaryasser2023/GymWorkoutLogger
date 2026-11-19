// Member 4: Workout Detail Sets Adapter
package com.example.gymworkoutlogger.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import com.example.gymworkoutlogger.databinding.ItemDetailExerciseSetBinding

data class DetailSetItem(
    val set: WorkoutSet,
    val exerciseName: String,
    val category: String
)

class WorkoutDetailAdapter :
    ListAdapter<DetailSetItem, WorkoutDetailAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetailExerciseSetBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemDetailExerciseSetBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DetailSetItem) {
            val set = item.set
            binding.tvDetailExerciseName.text = if (item.category.isNotEmpty()) "${item.exerciseName} (${item.category})" else item.exerciseName
            binding.tvSetNumber.text = set.setNumber.toString()
            binding.tvSetWeightReps.text = "${set.weightKg} kg  ×  ${set.reps} reps"
            val volume = (set.weightKg * set.reps).toInt()
            binding.tvSetVolume.text = "Volume: $volume kg"
            binding.tvSetStatus.text = if (set.isCompleted) "Completed ✓" else "Incomplete"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DetailSetItem>() {
        override fun areItemsTheSame(oldItem: DetailSetItem, newItem: DetailSetItem): Boolean =
            oldItem.set.id == newItem.set.id

        override fun areContentsTheSame(oldItem: DetailSetItem, newItem: DetailSetItem): Boolean =
            oldItem == newItem
    }
}
