package com.csiq6823.knowb4

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.csiq6823.knowb4.ui.ScanAdapter
import com.csiq6823.knowb4.ui.ThreatViewModel
import com.csiq6823.knowb4.ui.ThreatViewModel.ThreatViewModelFactory
import kotlinx.coroutines.launch

class ScanListFragment : Fragment(R.layout.fragment_scan_list) {

    private val viewModel: ThreatViewModel by activityViewModels {
        ThreatViewModelFactory((requireActivity().application as SecurityApplication).repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.scan_recycler_view)
        val blankState = view.findViewById<TextView>(R.id.blank_state)

        val adapter = ScanAdapter()
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allScans.collect { scans ->
                    adapter.submitList(scans)
                    blankState.isVisible = scans.isEmpty()
                }
            }
        }
    }
}
