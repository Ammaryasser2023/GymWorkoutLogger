package com.example.gymworkoutlogger.ui.history

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.gymworkoutlogger.R
import com.example.gymworkoutlogger.databinding.FragmentWorkoutHistoryBinding
import com.example.gymworkoutlogger.ui.ViewModelFactory
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutHistoryFragment : Fragment() {

    private var _binding: FragmentWorkoutHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory.getInstance(requireContext())
    }

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchAndFilter()
        setupSwipeToDelete()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onItemClick = { sessionWithSets ->
                val args = bundleOf("sessionId" to sessionWithSets.session.id)
                findNavController().navigate(R.id.workoutDetailFragment, args)
            },
            onDeleteClick = { sessionWithSets ->
                confirmDeleteSession(sessionWithSets)
            }
        )
        binding.rvHistorySessions.adapter = historyAdapter
    }

    private fun setupSearchAndFilter() {
        binding.etSearchHistory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnFilterDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Filter History From Date")
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                viewModel.setDateFilter(selection)
                val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.chipDateFilter.text = "From: ${format.format(Date(selection))}"
                binding.chipDateFilter.visibility = View.VISIBLE
            }

            datePicker.show(childFragmentManager, "DATE_PICKER")
        }

        binding.chipDateFilter.setOnCloseIconClickListener {
            viewModel.setDateFilter(null)
            binding.chipDateFilter.visibility = View.GONE
        }
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = historyAdapter.currentList.getOrNull(position)
                if (item != null) {
                    viewModel.deleteSession(item.session)
                    Snackbar.make(binding.root, "Session deleted", Snackbar.LENGTH_LONG)
                        .setAction("Dismiss") {}
                        .show()
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvHistorySessions)
    }

    private fun confirmDeleteSession(sessionWithSets: com.example.gymworkoutlogger.data.relation.WorkoutSessionWithSets) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Session")
            .setMessage("Are you sure you want to delete '${sessionWithSets.session.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteSession(sessionWithSets.session)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredSessions.collect { list ->
                    historyAdapter.submitList(list)
                    binding.layoutEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                    binding.rvHistorySessions.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
