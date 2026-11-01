package com.example.gymworkoutlogger.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.gymworkoutlogger.R
import com.example.gymworkoutlogger.databinding.FragmentDashboardBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var recentWorkoutAdapter: RecentWorkoutAdapter
    private lateinit var prHighlightAdapter: PrHighlightAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        recentWorkoutAdapter = RecentWorkoutAdapter { session ->
            val args = bundleOf("sessionId" to session.session.id)
            findNavController().navigate(R.id.action_dashboard_to_detail, args)
        }
        binding.rvRecentWorkouts.adapter = recentWorkoutAdapter

        prHighlightAdapter = PrHighlightAdapter { pr ->
            Toast.makeText(context, "PR: ${pr.exercise.name} - ${pr.personalRecord.maxWeightKg}kg", Toast.LENGTH_SHORT).show()
        }
        binding.rvPrHighlights.adapter = prHighlightAdapter
    }

    private fun setupListeners() {
        binding.btnStartWorkout.setOnClickListener {
            val activeSession = viewModel.activeSession.value
            if (activeSession != null) {
                val args = bundleOf("sessionId" to activeSession.id)
                findNavController().navigate(R.id.action_dashboard_to_activeWorkout, args)
            } else {
                viewModel.startNewSession("Workout Session") { newId ->
                    val args = bundleOf("sessionId" to newId)
                    findNavController().navigate(R.id.action_dashboard_to_activeWorkout, args)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.recentSessions.collect { sessions ->
                        recentWorkoutAdapter.submitList(sessions)
                        binding.tvStatWorkoutsCount.text = sessions.size.toString()
                        binding.tvEmptyHistory.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvRecentWorkouts.visibility = if (sessions.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.personalRecords.collect { prs ->
                        prHighlightAdapter.submitList(prs)
                        binding.tvStatPrsCount.text = prs.size.toString()
                        binding.tvPrSectionTitle.visibility = if (prs.isEmpty()) View.GONE else View.VISIBLE
                        binding.rvPrHighlights.visibility = if (prs.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.activeSession.collect { active ->
                        if (active != null) {
                            binding.tvCardTitle.text = "Session In Progress ⏱️"
                            binding.tvCardSubtitle.text = active.title
                            binding.btnStartWorkout.text = "Resume Workout"
                        } else {
                            binding.tvCardTitle.text = "Ready for Today's Session?"
                            binding.tvCardSubtitle.text = "Log sets, reps, and weights in real-time."
                            binding.btnStartWorkout.text = "Start New Workout"
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkActiveSession()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
