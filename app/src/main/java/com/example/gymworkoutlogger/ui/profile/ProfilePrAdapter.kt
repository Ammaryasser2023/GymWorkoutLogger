package com.example.gymworkoutlogger.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.data.relation.PersonalRecordWithExercise
import com.example.gymworkoutlogger.databinding.ItemPrFullCardBinding

class ProfilePrAdapter(
    private val onShareClick: (PersonalRecordWithExercise) -> Unit
) : ListAdapter<PersonalRecordWithExercise, ProfilePrAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPrFullCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPrFullCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PersonalRecordWithExercise) {
            binding.tvPrExerciseName.text = item.exercise.name
            binding.tvPrCategoryReps.text = "${item.exercise.category} • Max ${item.personalRecord.maxReps} Reps"
            binding.tvPrWeight.text = "${item.personalRecord.maxWeightKg} kg"

            binding.btnSharePr.setOnClickListener { onShareClick(item) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PersonalRecordWithExercise>() {
        override fun areItemsTheSame(
            oldItem: PersonalRecordWithExercise,
            newItem: PersonalRecordWithExercise
        ): Boolean = oldItem.personalRecord.id == newItem.personalRecord.id

        override fun areContentsTheSame(
            oldItem: PersonalRecordWithExercise,
            newItem: PersonalRecordWithExercise
        ): Boolean = oldItem == newItem
    }
}
