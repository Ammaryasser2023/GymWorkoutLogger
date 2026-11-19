// Member 3: Active Set Row List Adapter
package com.example.gymworkoutlogger.ui.workout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.data.entity.WorkoutSet
import com.example.gymworkoutlogger.databinding.ItemActiveSetRowBinding

class ActiveSetRowAdapter(
    private val onSetUpdated: (WorkoutSet) -> Unit,
    private val onSetDeleted: (WorkoutSet) -> Unit
) : ListAdapter<WorkoutSet, ActiveSetRowAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemActiveSetRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemActiveSetRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(set: WorkoutSet) {
            binding.tvSetNumber.text = set.setNumber.toString()

            // Update text only if view is not currently focused to prevent overwriting user input while typing
            if (!binding.etSetWeight.hasFocus()) {
                val weightStr = if (set.weightKg > 0) set.weightKg.toString() else ""
                if (binding.etSetWeight.text.toString() != weightStr) {
                    binding.etSetWeight.setText(weightStr)
                }
            }

            if (!binding.etSetReps.hasFocus()) {
                val repsStr = if (set.reps > 0) set.reps.toString() else ""
                if (binding.etSetReps.text.toString() != repsStr) {
                    binding.etSetReps.setText(repsStr)
                }
            }

            binding.cbSetCompleted.setOnCheckedChangeListener(null)
            binding.cbSetCompleted.isChecked = set.isCompleted

            // Save weight when user finishes editing (loses focus)
            binding.etSetWeight.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val newWeight = binding.etSetWeight.text.toString().toDoubleOrNull() ?: 0.0
                    val currentReps = binding.etSetReps.text.toString().toIntOrNull() ?: set.reps
                    if (newWeight != set.weightKg || currentReps != set.reps) {
                        onSetUpdated(set.copy(weightKg = newWeight, reps = currentReps))
                    }
                }
            }

            // Save reps when user finishes editing (loses focus)
            binding.etSetReps.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val currentWeight = binding.etSetWeight.text.toString().toDoubleOrNull() ?: set.weightKg
                    val newReps = binding.etSetReps.text.toString().toIntOrNull() ?: 0
                    if (currentWeight != set.weightKg || newReps != set.reps) {
                        onSetUpdated(set.copy(weightKg = currentWeight, reps = newReps))
                    }
                }
            }

            // Save state immediately when completion checkbox is clicked
            binding.cbSetCompleted.setOnClickListener {
                val weight = binding.etSetWeight.text.toString().toDoubleOrNull() ?: set.weightKg
                val reps = binding.etSetReps.text.toString().toIntOrNull() ?: set.reps
                val isChecked = binding.cbSetCompleted.isChecked
                onSetUpdated(set.copy(weightKg = weight, reps = reps, isCompleted = isChecked))
            }

            binding.btnDeleteSet.setOnClickListener { onSetDeleted(set) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WorkoutSet>() {
        override fun areItemsTheSame(oldItem: WorkoutSet, newItem: WorkoutSet): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WorkoutSet, newItem: WorkoutSet): Boolean =
            oldItem == newItem
    }
}
