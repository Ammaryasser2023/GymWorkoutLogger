// Member 3: Select Exercise Adapter
package com.example.gymworkoutlogger.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.databinding.ItemExerciseBinding

class SelectExerciseAdapter(
    private val onExerciseSelected: (Exercise) -> Unit
) : ListAdapter<Exercise, SelectExerciseAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemExerciseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(exercise: Exercise) {
            binding.tvExerciseName.text = exercise.name
            binding.tvExerciseCategory.text = exercise.category
            binding.tvExerciseNotes.text = exercise.notes.ifEmpty { "Tap to add to workout" }

            binding.root.setOnClickListener { onExerciseSelected(exercise) }
            binding.btnEditExercise.visibility = View.GONE
            binding.btnDeleteExercise.visibility = View.GONE
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean =
            oldItem == newItem
    }
}
