package ru.itis.travelling.presentation.transactions.fragments.overview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentTransactionsBinding
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.transactions.list.TransactionAdapter

@AndroidEntryPoint
class TransactionsFragment : BaseFragment(R.layout.fragment_transactions) {
    private val viewBinding: FragmentTransactionsBinding by viewBinding(FragmentTransactionsBinding::bind)
    private val viewModel: TransactionsViewModel by viewModels()
    private val tripId: String by lazy {
        arguments?.getString(TRIP_ID) ?: ""
    }
    private val phone: String by lazy {
        arguments?.getString(PHONE_TEXT) ?: ""
    }
    private var rvAdapter: TransactionAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()

        viewModel.loadTransactions(tripId)
    }

    private fun setupRecyclerView() {
        rvAdapter = TransactionAdapter { transaction ->
            viewModel.onTripClicked(tripId, transaction.id, phone)
        }

        viewBinding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    private fun setupListeners() {
        viewBinding.fabAdd.setOnClickListener {
            viewModel.navigateToAddTransaction(tripId, phone)
        }

        viewBinding.ivBackIcon.setOnClickListener {
            viewModel.navigateToTripDetail(tripId, phone)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactionsState.collect { state ->
                when (state) {
                    is TransactionsViewModel.TransactionsState.Loading -> showLoading()
                    is TransactionsViewModel.TransactionsState.Success -> {
                        hideLoading()
                        if (state.transactions.isEmpty()) {
                            showEmptyState()
                        } else {
                            showTransactions(state.transactions)
                        }
                    }
                    is TransactionsViewModel.TransactionsState.Idle -> hideLoading()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvent.collect { event ->
                when (event) {
                    is ErrorEvent.MessageOnly -> {
                        hideLoading()
                        showError(event.messageRes)
                    }
                    else -> {}
                }
            }
        }
    }

    private fun showLoading() {
        viewBinding.shimmerContainer.visibility = View.VISIBLE
        viewBinding.shimmerContainer.startShimmer()
        viewBinding.rvTransactions.visibility = View.GONE
    }

    private fun hideLoading() {
        viewBinding.shimmerContainer.visibility = View.GONE
        viewBinding.shimmerContainer.stopShimmer()
        viewBinding.rvTransactions.visibility = View.VISIBLE
    }

    private fun showEmptyState() {
        viewBinding.rvTransactions.visibility = View.GONE
        viewBinding.emptyStateView.visibility = View.VISIBLE
        viewBinding.emptyStateText.text = getString(R.string.no_transactions_message)
    }

    private fun showTransactions(transactions: List<Transaction>) {
        viewBinding.emptyStateView.visibility = View.GONE
        viewBinding.rvTransactions.visibility = View.VISIBLE
        rvAdapter?.submitList(transactions)
    }

    private fun showError(@StringRes messageRes: Int) {
        Toast.makeText(
            requireContext(),
            getString(messageRes),
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        const val TRANSACTIONS_TAG = "TRANSACTIONS_TAG"
        private const val TRIP_ID = "TRIP_ID"
        private const val PHONE_TEXT = "PHONE_TEXT"

        fun getInstance(tripId: String, phone: String): TransactionsFragment {
            return TransactionsFragment().apply {
                arguments = bundleOf(TRIP_ID to tripId, PHONE_TEXT to phone)
            }
        }
    }
}