package com.example.gymworkoutlogger.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.gymworkoutlogger.R
import com.example.gymworkoutlogger.databinding.FragmentProfileBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import com.example.gymworkoutlogger.ui.pr.PersonalRecordsViewModel
import com.example.gymworkoutlogger.ui.pr.ActivityStatsViewModel
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private val personalRecordsViewModel: PersonalRecordsViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private val activityStatsViewModel: ActivityStatsViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var prAdapter: ProfilePrAdapter

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {
                    // Ignore if permission not persistable
                }
                viewModel.updateProfileImageUri(it.toString())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        setupTabs()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        prAdapter = ProfilePrAdapter { pr ->
            val shareText = """
                🏆 New Personal Record Alert!
                🏋️ ${pr.exercise.name}: ${pr.personalRecord.maxWeightKg} kg (${pr.personalRecord.maxReps} reps)
                Logged with Gym Workout Logger App 💪
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "My Personal Record")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share PR Achievement"))
        }
        binding.rvProfilePrs.adapter = prAdapter
    }

    private fun setupListeners() {
        binding.btnPickPfp.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnEditName.setOnClickListener {
            showEditNameDialog(viewModel.userName.value)
        }
    }

    private fun setupTabs() {
        binding.tabLayoutProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.layoutPrTabContent.visibility = View.VISIBLE
                        binding.layoutStatsTabContent.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutPrTabContent.visibility = View.GONE
                        binding.layoutStatsTabContent.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun renderWeeklyActivity(
        weeklyActivity: List<com.example.gymworkoutlogger.ui.pr.DailyActivity>
    ) {
        val chart = binding.weeklyActivityChart

        // Remove previous chart views
        chart.removeAllViews()

        // Show empty message if there is no activity
        if (weeklyActivity.all { it.workoutCount == 0 }) {
            val emptyText = TextView(requireContext()).apply {
                text = getString(R.string.stats_no_activity)
                textSize = 14f
            }

            chart.addView(emptyText)
            return
        }

        val maxWorkouts = weeklyActivity.maxOf { it.workoutCount }

        weeklyActivity.forEach { activity ->

            // Create horizontal row
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            // Day name
            val dayText = TextView(requireContext()).apply {
                text = activity.day
                textSize = 12f
            }

            row.addView(
                dayText,
                LinearLayout.LayoutParams(
                    dp(40),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            // Progress bar
            val progressBar = ProgressBar(
                requireContext(),
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                max = maxWorkouts
                progress = activity.workoutCount
                isIndeterminate = false

                progressTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.purple_500
                    )
                )

                progressBackgroundTintList =
                    android.content.res.ColorStateList.valueOf(
                        Color.LTGRAY
                    )
            }

            val progressParams = LinearLayout.LayoutParams(
                0,
                dp(8),
                1f
            ).apply {
                marginStart = dp(8)
                marginEnd = dp(8)
            }

            row.addView(progressBar, progressParams)

            // Workout count
            val countText = TextView(requireContext()).apply {
                text = activity.workoutCount.toString()
                textSize = 12f
            }

            row.addView(
                countText,
                LinearLayout.LayoutParams(
                    dp(24),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            // Add row to chart
            val rowParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(32)
            )

            chart.addView(row, rowParams)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun showEditNameDialog(currentName: String) {
        val input = EditText(requireContext()).apply {
            setText(currentName)
            hint = "Enter profile name"
            setSelection(currentName.length)
        }
        val container = FrameLayout(requireContext()).apply {
            setPadding(48, 24, 48, 24)
            addView(input)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Profile Name")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateUserName(newName)
                    Toast.makeText(context, "Name updated", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Observe Profile Name
                launch {
                    viewModel.userName.collect { name ->
                        binding.tvUserName.text = name.ifEmpty { "Athlete" }
                    }
                }

                // Observe Profile Picture
                launch {
                    viewModel.profileImageUri.collect { uriString ->
                        loadProfilePicture(uriString)
                    }
                }

                // Observe Personal Records
                launch {
                    personalRecordsViewModel.personalRecords.collect { prs ->

                        prAdapter.submitList(prs)

                        binding.tvEmptyPrs.visibility =
                            if (prs.isEmpty()) View.VISIBLE else View.GONE

                        binding.rvProfilePrs.visibility =
                            if (prs.isEmpty()) View.GONE else View.VISIBLE
                    }
                }

                // Observe Activity Statistics
                launch {
                    activityStatsViewModel.stats.collect { stats ->

                        binding.tvTotalWorkouts.text =
                            stats.totalWorkouts.toString()

                        binding.tvTotalSets.text =
                            stats.totalSets.toString()

                        binding.tvTotalVolume.text = getString(
                            R.string.stats_volume_value,
                            stats.totalVolume
                        )

                        binding.tvActiveDays.text =
                            stats.activeDays.toString()

                        renderWeeklyActivity(stats.weeklyActivity)

                        binding.tvMostTrainedExercises.text =
                            if (stats.mostTrainedExercises.isEmpty()) {
                                getString(R.string.stats_no_exercises)
                            } else {
                                stats.mostTrainedExercises.joinToString("\n") {
                                    getString(
                                        R.string.stats_exercise_count,
                                        it.exerciseName,
                                        it.setCount
                                    )
                                }
                            }
                    }
                }
            }
        }
    }


    private fun loadProfilePicture(uriString: String?) {
        if (!uriString.isNullOrEmpty()) {
            try {
                binding.ivProfilePicture.imageTintList = null
                binding.ivProfilePicture.setImageURI(Uri.parse(uriString))
            } catch (e: Exception) {
                binding.ivProfilePicture.setImageResource(R.drawable.ic_person)
                binding.ivProfilePicture.imageTintList =
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
            }
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.ic_person)
            binding.ivProfilePicture.imageTintList =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
