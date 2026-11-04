package com.example.gymworkoutlogger.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.gymworkoutlogger.databinding.FragmentWorkoutDetailBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutDetailFragment : Fragment() {

    private var _binding: FragmentWorkoutDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var detailAdapter: WorkoutDetailAdapter
    private var currentSessionId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentSessionId = arguments?.getLong("sessionId", -1L) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeSession()
    }

    private fun setupRecyclerView() {
        detailAdapter = WorkoutDetailAdapter()
        binding.rvDetailSets.adapter = detailAdapter
    }

    private fun observeSession() {
        if (currentSessionId == -1L) return

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getSessionWithSets(currentSessionId).collect { sessionWithSets ->
                    if (sessionWithSets != null) {
                        val session = sessionWithSets.session
                        val sets = sessionWithSets.sets

                        binding.tvDetailTitle.text = session.title
                        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
                        binding.tvDetailDate.text = dateFormat.format(Date(session.startTime))

                        binding.tvTotalSetsCount.text = sets.size.toString()
                        val totalVolume = sets.sumOf { it.weightKg * it.reps }.toInt()
                        binding.tvTotalVolume.text = "$totalVolume kg"

                        detailAdapter.submitList(sets)

                        setupActions(sessionWithSets)
                    }
                }
            }
        }
    }

    private fun setupActions(sessionWithSets: com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets) {
        binding.btnShareWorkout.setOnClickListener {
            val session = sessionWithSets.session
            val sets = sessionWithSets.sets
            val totalVolume = sets.sumOf { it.weightKg * it.reps }.toInt()

            val shareText = """
                💪 Gym Workout Summary: ${session.title}
                📅 Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(session.startTime))}
                🏋️ Total Volume: $totalVolume kg across ${sets.size} sets!
                Logged with Gym Workout Logger App 🚀
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "My Workout Summary")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Workout Summary"))
        }

        binding.btnDeleteWorkout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Workout")
                .setMessage("Are you sure you want to delete '${sessionWithSets.session.title}'?")
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteSession(sessionWithSets.session)
                    findNavController().navigateUp()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
