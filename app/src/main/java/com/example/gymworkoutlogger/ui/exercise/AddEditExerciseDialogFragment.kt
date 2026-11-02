package com.example.gymworkoutlogger.ui.exercise

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.gymworkoutlogger.databinding.DialogAddEditExerciseBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddEditExerciseDialogFragment : DialogFragment() {

    private var _binding: DialogAddEditExerciseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseViewModel by activityViewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private var exerciseId: Long = 0L
    private var existingName: String = ""
    private var existingCategory: String = ""
    private var existingNotes: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exerciseId = arguments?.getLong(ARG_ID, 0L) ?: 0L
        existingName = arguments?.getString(ARG_NAME, "") ?: ""
        existingCategory = arguments?.getString(ARG_CATEGORY, "") ?: ""
        existingNotes = arguments?.getString(ARG_NOTES, "") ?: ""
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddEditExerciseBinding.inflate(layoutInflater)

        val categories = listOf("Chest", "Back", "Legs", "Shoulders", "Arms", "Core", "Cardio")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)

        if (exerciseId > 0) {
            binding.tvDialogTitle.text = "Edit Exercise"
            binding.etExerciseName.setText(existingName)
            binding.actvCategory.setText(existingCategory, false)
            binding.etExerciseNotes.setText(existingNotes)
        } else {
            binding.tvDialogTitle.text = "Add New Exercise"
            binding.actvCategory.setText("Chest", false)
        }

        binding.btnCancelDialog.setOnClickListener { dismiss() }

        binding.btnSaveDialog.setOnClickListener {
            val name = binding.etExerciseName.text.toString().trim()
            val category = binding.actvCategory.text.toString().trim()
            val notes = binding.etExerciseNotes.text.toString().trim()

            if (name.isEmpty()) {
                binding.inputLayoutName.error = "Exercise name required"
                return@setOnClickListener
            }

            viewModel.saveExercise(exerciseId, name, category.ifEmpty { "General" }, notes)
            Toast.makeText(context, if (exerciseId > 0) "Exercise Updated" else "Exercise Created", Toast.LENGTH_SHORT).show()
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ID = "arg_id"
        private const val ARG_NAME = "arg_name"
        private const val ARG_CATEGORY = "arg_category"
        private const val ARG_NOTES = "arg_notes"

        fun newInstance(id: Long = 0, name: String = "", category: String = "", notes: String = ""): AddEditExerciseDialogFragment {
            return AddEditExerciseDialogFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, id)
                    putString(ARG_NAME, name)
                    putString(ARG_CATEGORY, category)
                    putString(ARG_NOTES, notes)
                }
            }
        }
    }
}
