// Member 3: Select Exercise Bottom Sheet Dialog Fragment
package com.example.gymworkoutlogger.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gymworkoutlogger.data.entity.Exercise
import com.example.gymworkoutlogger.databinding.DialogSelectExerciseBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import com.example.gymworkoutlogger.ui.exercise.ExerciseViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class SelectExerciseDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogSelectExerciseBinding? = null
    private val binding get() = _binding!!

    private val exerciseViewModel: ExerciseViewModel by activityViewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    var onExercisePicked: ((Exercise) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSelectExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = SelectExerciseAdapter { exercise ->
            onExercisePicked?.invoke(exercise)
            dismiss()
        }
        binding.rvSelectExercises.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                exerciseViewModel.filteredExercises.collect { list ->
                    adapter.submitList(list)
                }
            }
        }

        binding.btnCloseSelectDialog.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
