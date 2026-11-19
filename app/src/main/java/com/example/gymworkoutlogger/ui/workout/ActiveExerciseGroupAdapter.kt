// Member 3: Active Exercise Group List Adapter
package com.example.gymworkoutlogger.ui.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import com.example.gymworkoutlogger.databinding.ItemActiveExerciseGroupBinding

class ActiveExerciseGroupAdapter(
    private val onAddSetClick: (Long) -> Unit,
    private val onSetUpdated: (WorkoutSet) -> Unit,
    private val onSetDeleted: (WorkoutSet) -> Unit
) : ListAdapter<ExerciseWithLoggedSets, ActiveExerciseGroupAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveExerciseGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemActiveExerciseGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val setRowAdapter = ActiveSetRowAdapter(onSetUpdated, onSetDeleted)

        init {
            binding.rvGroupSetRows.adapter = setRowAdapter
        }

        fun bind(group: ExerciseWithLoggedSets) {
            binding.tvGroupExerciseName.text = group.exercise.name
            binding.tvGroupCategory.text = group.exercise.category
            setRowAdapter.submitList(group.sets)

            binding.btnAddSet.setOnClickListener {
                onAddSetClick(group.exercise.id)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ExerciseWithLoggedSets>() {
        override fun areItemsTheSame(
            oldItem: ExerciseWithLoggedSets,
            newItem: ExerciseWithLoggedSets
        ): Boolean = oldItem.exercise.id == newItem.exercise.id

        override fun areContentsTheSame(
            oldItem: ExerciseWithLoggedSets,
            newItem: ExerciseWithLoggedSets
        ): Boolean = oldItem == newItem
    }
}
