package ru.itis.travelling.presentation.transactions.fragments.details

import android.app.AlertDialog
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
import ru.itis.travelling.databinding.FragmentTransactionDetailsBinding
import ru.itis.travelling.domain.transactions.model.TransactionCategory
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import ru.itis.travelling.domain.transactions.model.getDisplayName
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.transactions.list.ParticipantDebtAdapter
import kotlin.getValue

@AndroidEntryPoint
class TransactionDetailsFragment: BaseFragment(R.layout.fragment_transaction_details) {
    private val viewBinding: FragmentTransactionDetailsBinding by viewBinding(FragmentTransactionDetailsBinding::bind)
    private val viewModel: TransactionDetailsViewModel by viewModels()
    private lateinit var participantDebtAdapter: ParticipantDebtAdapter

    private val tripId: String by lazy {
        arguments?.getString(TRIP_ID) ?: ""
    }
    private val transactionId: String by lazy {
        arguments?.getString(TRANSACTION_ID) ?: ""
    }
    private val phone: String by lazy {
        arguments?.getString(PHONE_TEXT) ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeViewModel()
        setupListeners()

        transactionId.let {
            viewModel.loadTransactionDetails(it)
        }
    }

    private fun setupAdapter() {
        participantDebtAdapter = ParticipantDebtAdapter()
        viewBinding.rvDebtParticipants.apply {
            adapter = participantDebtAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.transactionState.collect { state ->
                when (state) {
                    TransactionDetailsViewModel.TransactionDetailsState.Loading -> showProgress()
                    is TransactionDetailsViewModel.TransactionDetailsState.Success -> {
                        hideProgress()
                        updateUi(state.transaction)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is TransactionDetailsViewModel.TransactionDetailsEvent.Error -> {
                        hideProgress()
                        showError(event.message)
                    }
                    TransactionDetailsViewModel.TransactionDetailsEvent.NavigateToTransactions -> {
                        viewModel.navigateToTransactions(tripId, phone)
                    }

                    TransactionDetailsViewModel.TransactionDetailsEvent.ShowDeleteConfirmation -> {
                        hideProgress()
                        showDeleteConfirmationDialog()
                    }

                    TransactionDetailsViewModel.TransactionDetailsEvent.ShowDeleteNotAllowed -> {
                        hideProgress()
                        showAdminDeleteAlert()
                    }
                    TransactionDetailsViewModel.TransactionDetailsEvent.DebtPaid -> viewModel.loadTransactionDetails(transactionId)
                    TransactionDetailsViewModel.TransactionDetailsEvent.ShowEditNotAllowed -> {
                        hideProgress()
                        showAdminEditAlert()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvent.collect { event ->
                when (event) {
                    is ErrorEvent.MessageOnly -> showToast(event.messageRes)
                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        viewBinding.ivBackIcon.setOnClickListener {
            viewModel.navigateToTransactions(tripId, phone)
        }

        viewBinding.ivEditIcon.setOnClickListener {
            viewModel.onEditClicked(phone, tripId, transactionId)
        }

        viewBinding.ivDeleteIcon.setOnClickListener {
            viewModel.onDeleteClicked(phone)
        }

        viewBinding.btnPay.setOnClickListener {
            viewModel.payDebt(phone, transactionId)
        }
    }

    private fun updateUi(transactionDetails: TransactionDetails) {
        with(viewBinding) {
            tvCategory.text = TransactionCategory.valueOf(transactionDetails.category).getDisplayName(requireContext())
            tvDescription.text = transactionDetails.description
            tvAmount.text = getString(R.string.price, transactionDetails.totalCost)
            tvCreatorFirstName.text = transactionDetails.creator?.firstName
            tvCreatorLastName.text = transactionDetails.creator?.lastName
            tvCreatorPhone.text = transactionDetails.creator?.phone
            btnPay.text = getString(viewModel.getButtonText(phone))

            participantDebtAdapter.submitList(transactionDetails.participants)
        }
    }

    private fun showAdminDeleteAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_required))
            .setMessage(getString(R.string.message_delete_not_creator))
            .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showAdminEditAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_required))
            .setMessage(getString(R.string.message_edit_not_creator))
            .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_transaction))
            .setMessage(getString(R.string.message_delete_creator))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                viewModel.confirmDeleteTrip()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun showProgress() {
        with(viewBinding) {
            shimmerLayout.startShimmer()
            shimmerLayout.visibility = View.VISIBLE
            mainContentGroupTransaction.visibility = View.GONE
        }
    }

    override fun hideProgress() {
        with(viewBinding) {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            mainContentGroupTransaction.visibility = View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    companion object {
        const val TRANSACTION_DETAILS_TAG = "TRANSACTION_DETAILS_TAG"
        private const val TRIP_ID = "TRIP_ID"
        private const val TRANSACTION_ID = "TRANSACTION_ID"
        private const val PHONE_TEXT = "PHONE_TEXT"

        fun getInstance(tripId: String, transactionId: String, phone: String): TransactionDetailsFragment {
            return TransactionDetailsFragment().apply {
                arguments = bundleOf(TRIP_ID to tripId, PHONE_TEXT to phone, TRANSACTION_ID to transactionId)
            }
        }
    }
}