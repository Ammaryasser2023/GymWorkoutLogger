// Member 3: Active Workout Screen
package com.example.gymworkoutlogger.ui.workout

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.gymworkoutlogger.databinding.FragmentActiveWorkoutBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.util.Locale

class ActiveWorkoutFragment : Fragment() {

    private var _binding: FragmentActiveWorkoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkoutLoggerViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var groupAdapter: ActiveExerciseGroupAdapter
    private var passedSessionId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        passedSessionId = arguments?.getLong("sessionId", -1L) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadOrStartSession(passedSessionId)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        groupAdapter = ActiveExerciseGroupAdapter(
            onAddSetClick = { exerciseId ->
                viewModel.addSetToExercise(exerciseId)
            },
            onSetUpdated = { set ->
                viewModel.updateSet(set)
            },
            onSetDeleted = { set ->
                viewModel.deleteSet(set)
            }
        )
        binding.rvActiveExercises.adapter = groupAdapter
    }

    private fun setupListeners() {
        binding.btnAddExerciseToWorkout.setOnClickListener {
            hideKeyboard()
            val dialog = SelectExerciseDialogFragment().apply {
                onExercisePicked = { exercise ->
                    viewModel.addExerciseToWorkout(exercise)
                }
            }
            dialog.show(childFragmentManager, "SELECT_EXERCISE_DIALOG")
        }

        binding.btnFinishWorkout.setOnClickListener {
            hideKeyboard()
            val sessionWithSets = viewModel.currentSessionWithSets.value
            val sets = sessionWithSets?.sets ?: emptyList()
            val completedCount = sets.count { it.isCompleted }

            if (sets.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Empty Workout")
                    .setMessage("You haven't added any exercises to this workout. Would you like to discard it?")
                    .setPositiveButton("Discard") { _, _ ->
                        viewModel.cancelSession {
                            findNavController().navigateUp()
                        }
                    }
                    .setNegativeButton("Keep Editing", null)
                    .show()
            } else {
                viewModel.finishSession {
                    Toast.makeText(context, "Workout Finished! $completedCount sets logged 💪", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
            }
        }

        binding.btnCancelWorkout.setOnClickListener {
            hideKeyboard()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Discard Workout")
                .setMessage("Are you sure you want to discard this active workout session? All logged sets will be removed.")
                .setPositiveButton("Discard") { _, _ ->
                    viewModel.cancelSession {
                        findNavController().navigateUp()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val view = activity?.currentFocus ?: binding.root
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.currentSessionWithSets.collect { sessionWithSets ->
                        if (sessionWithSets != null) {
                            binding.tvActiveTitle.text = sessionWithSets.session.title
                        }
                    }
                }

                launch {
                    viewModel.activeExercisesGrouped.collect { list ->
                        groupAdapter.submitList(list)
                        binding.tvEmptyActiveExercises.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvActiveExercises.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.elapsedSeconds.collect { seconds ->
                        val hours = seconds / 3600
                        val minutes = (seconds % 3600) / 60
                        val secs = seconds % 60
                        binding.tvTimerDisplay.text = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, secs)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
