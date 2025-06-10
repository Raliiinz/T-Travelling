package ru.itis.travelling.presentation.transactions.fragments.add

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentAddTransactionBinding
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.transactions.model.TransactionCategory
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import ru.itis.travelling.domain.transactions.model.getDisplayName
import ru.itis.travelling.domain.transactions.model.mapCategoryToApiValue
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.transactions.fragments.add.state.AddTransactionUiState
import ru.itis.travelling.presentation.transactions.fragments.add.state.TransactionEvent
import ru.itis.travelling.presentation.transactions.fragments.add.state.ValidationFailure
import ru.itis.travelling.presentation.transactions.list.ParticipantTransactionAdapter
import ru.itis.travelling.presentation.transactions.util.SplitType
import ru.itis.travelling.presentation.transactions.util.addSimpleTextWatcher
import kotlin.getValue

@AndroidEntryPoint
class AddTransactionFragment : BaseFragment(R.layout.fragment_add_transaction) {
    private val viewBinding: FragmentAddTransactionBinding by viewBinding(FragmentAddTransactionBinding::bind)
    private val viewModel: AddTransactionViewModel by viewModels()

    private lateinit var participantsAdapter: ParticipantTransactionAdapter

    private val tripId: String by lazy {
        arguments?.getString(TRIP_ID) ?: ""
    }
    private val phone: String by lazy {
        arguments?.getString(PHONE_TEXT) ?: ""
    }
    private val transactionId: String by lazy {
        arguments?.getString(TRANSACTION_ID) ?: ""
    }

    private var selectedSplitType: SplitType = SplitType.ONE_PERSON
    private var totalAmount: String = "0.0"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (transactionId.isNotEmpty()) {
            viewModel.loadTransactionForEditing(tripId, transactionId)
        } else {
            viewModel.loadParticipants(tripId, phone)
        }

        setupUI()
        setupObservers()
        setupClickListeners()
        updateSplitTypeUI()
    }

    private fun setupUI() {
        participantsAdapter = ParticipantTransactionAdapter { participant, amount ->
            viewModel.updateParticipantAmount(participant.phone, amount)
        }
        viewBinding.rvParticipants.apply {
            adapter = participantsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.transaction_categories,
            R.layout.spinner_item
        ).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
            viewBinding.spinnerCategory.adapter = this
        }

        viewBinding.etTotalAmount.addSimpleTextWatcher {
            totalAmount = it.orEmpty()
            if (selectedSplitType != SplitType.MANUALLY) updateParticipantsList()
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { isEditMode() }
                launch { observeParticipants() }
                launch { observeUiState() }
                launch { observeErrors() }
                launch { observeEvents() }
                launch { observeFormState() }
            }
        }
    }

    private suspend fun observeParticipants() {
        viewModel.participants.collect { updateParticipantsList() }
    }

    private suspend fun observeUiState() {
        viewModel.uiState.collect { state ->
            when (state) {
                AddTransactionUiState.Loading -> showProgress()
                AddTransactionUiState.Success -> {
                    hideProgress()
                    viewModel.navigateToTransactions(tripId, phone)
                }
                AddTransactionUiState.Idle -> hideProgress()
            }
        }
    }

    private suspend fun observeErrors() {
        viewModel.errorEvent.collect { error ->
            hideProgress()
            if (error is ErrorEvent.MessageOnly) showToast(error.messageRes)
        }
    }

    private suspend fun observeEvents() {
        viewModel.events.collectLatest{ event ->
            if (event is TransactionEvent.ValidationError) handleValidationErrors(event.errors)
        }
    }

    private suspend fun observeFormState() {
        viewModel.formState.collect { formState ->
            selectedSplitType = formState.splitType
            updateSplitTypeUI()

            if (viewBinding.etTotalAmount.text?.toString() != formState.totalAmount) {
                viewBinding.etTotalAmount.setText(formState.totalAmount)
                totalAmount = formState.totalAmount
            }

            viewBinding.descriptionInputLayout.editText?.setText(formState.description)

            formState.category?.let { category ->
                val categoryDisplayName = category.getDisplayName(requireContext())
                val adapter = viewBinding.spinnerCategory.adapter as ArrayAdapter<String>
                val position = adapter.getPosition(categoryDisplayName)
                if (position >= 0) {
                    viewBinding.spinnerCategory.setSelection(position)
                }
            }
            updateParticipantsList()
        }
    }

    private suspend fun isEditMode() {
        viewModel.isEditMode.collect { isEditMode ->
            viewBinding.btnCreate.text = if (isEditMode) {
                getString(R.string.save_changes)
            } else {
                getString(R.string.create)
            }
        }
    }

    private fun handleValidationErrors(errors: Set<ValidationFailure>) {
        errors.forEach { error ->
            showToast(error.messageRes)
        }
    }

    private fun updateParticipantsList() {
        val participants = viewModel.participants.value
        val updatedList = viewModel.getParticipantsForSplitType(
            selectedSplitType,
            totalAmount,
            participants
        )
        participantsAdapter.submitList(updatedList)
    }

    private fun setupClickListeners() {
        viewBinding.apply {
            ivBackIcon.setOnClickListener { viewModel.navigateToTransactions(tripId, phone) }

            btnOnePerson.setOnClickListener {
                updateSplitType(SplitType.ONE_PERSON)
                viewBinding.etTotalAmount.text?.toString()?.takeIf { it.isNotEmpty() }?.let { amount ->
                    viewModel.participants.value.firstOrNull()?.let { participant ->
                        viewModel.updateParticipantAmount(participant.phone, amount)
                    }
                }
            }

            btnSplitEqually.setOnClickListener { updateSplitType(SplitType.EQUALLY) }
            btnAddParticipant.setOnClickListener { updateSplitType(SplitType.MANUALLY) }

            btnCreate.setOnClickListener { createTransaction() }
        }
    }

    private fun updateSplitType(type: SplitType) {
        selectedSplitType = type
        updateSplitTypeUI()
        updateParticipantsList()
    }

    private fun updateSplitTypeUI() {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.yellow)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.image_back)
        val selectedIconColor = ContextCompat.getColor(requireContext(), R.color.black)
        val defaultIconColor = ContextCompat.getColor(requireContext(), R.color.image_photo)

        viewBinding.apply {
            btnOnePerson.backgroundTintList = colorStateList(selectedSplitType == SplitType.ONE_PERSON, selectedColor, defaultColor)
            btnSplitEqually.backgroundTintList = colorStateList(selectedSplitType == SplitType.EQUALLY, selectedColor, defaultColor)
            btnAddParticipant.backgroundTintList = colorStateList(selectedSplitType == SplitType.MANUALLY, selectedColor, defaultColor)

            btnOnePerson.iconTint = colorStateList(selectedSplitType == SplitType.ONE_PERSON, selectedIconColor, defaultIconColor)
            btnSplitEqually.iconTint = colorStateList(selectedSplitType == SplitType.EQUALLY, selectedIconColor, defaultIconColor)
            btnAddParticipant.iconTint = colorStateList(selectedSplitType == SplitType.MANUALLY, selectedIconColor, defaultIconColor)
        }
    }

    private fun colorStateList(condition: Boolean, trueColor: Int, falseColor: Int) =
        ColorStateList.valueOf(if (condition) trueColor else falseColor)

    private fun createTransaction() {
        val description = viewBinding.descriptionInputLayout.editText?.text?.toString().orEmpty()
        val totalAmount = viewBinding.etTotalAmount.text?.toString().orEmpty()

        if (totalAmount.isBlank()) {
            showToast(R.string.error_invalid_amount)
            return
        }
        val categoryDisplayName = viewBinding.spinnerCategory.selectedItem?.toString().orEmpty()

        val category = TransactionCategory.entries.find { it.getDisplayName(requireContext()) == categoryDisplayName }
        val apiCategory = mapCategoryToApiValue(categoryDisplayName, requireContext())

        viewModel.updateFormState(
            description = description,
            totalAmount = totalAmount,
            category = category,
            splitType = selectedSplitType
        )

        val request = TransactionDetails(
            id = transactionId,
            category = apiCategory,
            totalCost = totalAmount,
            description = description,
            participants = viewModel.participants.value.map {
                Participant(
                    phone = it.phone,
                    shareAmount = it.shareAmount ?: "0"
                )
            }
        )
        viewModel.validateAndCreateTransaction(tripId, transactionId, request)
    }

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(
            requireContext(),
            getString(messageRes),
            Toast.LENGTH_LONG
        ).show()
    }

    companion object {
        const val ADD_TRANSACTION_TAG = "ADD_TRANSACTION_TAG"
        private const val TRIP_ID = "TRIP_ID"
        private const val PHONE_TEXT = "PHONE_TEXT"
        private const val TRANSACTION_ID = "TRANSACTION_ID"

        fun getInstance(tripId: String, phone: String): AddTransactionFragment {
            return getInstance(tripId, phone, "")
        }

        fun getInstanceForEditing(tripId: String, phone: String, transactionId: String): AddTransactionFragment {
            return getInstance(tripId, phone, transactionId)
        }

        private fun getInstance(tripId: String, phone: String, transactionId: String): AddTransactionFragment {
            return AddTransactionFragment().apply {
                arguments = bundleOf(
                    TRIP_ID to tripId,
                    PHONE_TEXT to phone,
                    TRANSACTION_ID to transactionId
                )
            }
        }
    }
}
