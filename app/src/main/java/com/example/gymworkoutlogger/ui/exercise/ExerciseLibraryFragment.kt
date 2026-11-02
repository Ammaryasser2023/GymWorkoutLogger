package com.example.gymworkoutlogger.ui.exercise

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gymworkoutlogger.R
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.databinding.FragmentExerciseLibraryBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ExerciseLibraryFragment : Fragment() {

    private var _binding: FragmentExerciseLibraryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupCategoryChips()
        setupSearch()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        exerciseAdapter = ExerciseAdapter(
            onEditClick = { exercise ->
                val dialog = AddEditExerciseDialogFragment.newInstance(
                    exercise.id, exercise.name, exercise.category, exercise.notes
                )
                dialog.show(childFragmentManager, "EDIT_EXERCISE_DIALOG")
            },
            onDeleteClick = { exercise ->
                confirmDeleteExercise(exercise)
            }
        )
        binding.rvExercises.adapter = exerciseAdapter
    }

    private fun setupCategoryChips() {
        val categories = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core")
        binding.chipGroupCategories.removeAllViews()

        categories.forEachIndexed { index, category ->
            val chip = layoutInflater.inflate(R.layout.item_chip, binding.chipGroupCategories, false) as Chip
            chip.apply {
                id = View.generateViewId()
                text = category
                isChecked = index == 0
                setOnClickListener {
                    viewModel.setSelectedCategory(if (category == "All") null else category)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun setupSearch() {
        binding.etSearchExercise.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupListeners() {
        binding.fabAddExercise.setOnClickListener {
            val dialog = AddEditExerciseDialogFragment.newInstance()
            dialog.show(childFragmentManager, "ADD_EXERCISE_DIALOG")
        }
    }

    private fun confirmDeleteExercise(exercise: Exercise) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Exercise")
            .setMessage("Are you sure you want to delete '${exercise.name}'? This will also remove sets associated with this exercise.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteExercise(exercise)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredExercises.collect { list ->
                    exerciseAdapter.submitList(list)
                    binding.layoutEmptyExercises.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvExercises.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
