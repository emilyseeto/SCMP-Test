package com.example.scmptest.ui.stafflist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.scmptest.R
import com.example.scmptest.databinding.FragmentStaffListBinding
import com.example.scmptest.ext.showErrorDialog
import com.example.scmptest.ext.visibleElseGone

class StaffListFragment : Fragment() {

    private var _binding: FragmentStaffListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StaffListViewModel by viewModels()

    private val mAdapter = StaffListAdapter(
        onLoadMoreClick = {
            viewModel.retrieveStaffList()
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("token")?.let {
            binding.staffListTokenTxt.text = String.format(getString(R.string.staff_list_token), it)
        }

        binding.staffListContent.adapter = mAdapter

        setupObservers()

        viewModel.retrieveStaffList()
    }

    private fun setupObservers() {
        viewModel.displayList.observe(viewLifecycleOwner, Observer { list ->
            binding.staffListNoRecordTxt.visibleElseGone(list.isEmpty())
            binding.staffListContent.visibleElseGone(list.isNotEmpty())
            mAdapter.updateList(list)
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.staffListLoadingContainer.visibleElseGone(isLoading)
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            if (!error.isNullOrBlank()) {
                context?.showErrorDialog(error)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}