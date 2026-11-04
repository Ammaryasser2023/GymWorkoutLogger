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

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels {
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
                launch {
                    viewModel.userName.collect { name ->
                        binding.tvUserName.text = name.ifEmpty { "Athlete" }
                    }
                }

                launch {
                    viewModel.profileImageUri.collect { uriString ->
                        loadProfilePicture(uriString)
                    }
                }

                launch {
                    viewModel.personalRecords.collect { prs ->
                        prAdapter.submitList(prs)
                        binding.tvEmptyPrs.visibility = if (prs.isEmpty()) View.VISIBLE else View.GONE
                        binding.rvProfilePrs.visibility = if (prs.isEmpty()) View.GONE else View.VISIBLE
                        binding.tvProfileStatsSummary.text = "You have achieved ${prs.size} Personal Records across your training!"
                    }
                }
            }
        }
    }

    private fun loadProfilePicture(uriString: String?) {
        if (!uriString.isNullOrEmpty()) {
            try {
                binding.ivProfilePicture.clearColorFilter()
                binding.ivProfilePicture.setImageURI(Uri.parse(uriString))
            } catch (e: Exception) {
                binding.ivProfilePicture.setImageResource(R.drawable.ic_person)
            }
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.ic_person)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
