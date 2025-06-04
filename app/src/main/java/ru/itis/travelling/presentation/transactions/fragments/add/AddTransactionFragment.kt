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
import ru.itis.travelling.presentation.transactions.fragments.add.AddTransactionViewModel.AddTransactionUiState
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

    private var selectedSplitType: SplitType = SplitType.ONE_PERSON
    private var totalAmount: String = "0.0"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadParticipants(tripId)
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
//            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeParticipants() }
                launch { observeUiState() }
                launch { observeErrors() }
                launch { observeEvents() }
                launch {
                    viewModel.formState.collect { formState ->
                        formState.splitType.let {
                            selectedSplitType = it
                            updateSplitTypeUI()
                        }
                    }
//                }
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

    private fun handleValidationErrors(errors: Set<ValidationFailure>) {
        errors.forEach { error ->
            showToast(error.messageRes)
        }
    }

    private fun updateParticipantsList() {
        val participants = viewModel.participants.value
        participantsAdapter.submitList(when (selectedSplitType) {
            SplitType.ONE_PERSON -> participants.firstOrNull()?.let {
                listOf(it.copy(shareAmount = totalAmount))
            } ?: emptyList()

            SplitType.EQUALLY -> {
                val equalAmount =
                    if (participants.isNotEmpty()) totalAmount.toDouble() / participants.size else 0.0
                participants.map { it.copy(shareAmount = equalAmount.toString()) }
            }

            SplitType.MANUALLY -> {
                val currentList = participantsAdapter.currentList
                if (currentList.size != participants.size) {
                    participants.map { it.copy(shareAmount = it.shareAmount ?: "0") }
                } else {
                    participants.mapIndexed { index, p ->
                        p.copy(shareAmount = currentList.getOrNull(index)?.shareAmount ?: "0")
                    }
                }
            }
        })
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
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.hint_color)

        viewBinding.apply {
            btnOnePerson.backgroundTintList = colorStateList(selectedSplitType == SplitType.ONE_PERSON, selectedColor, defaultColor)
            btnSplitEqually.backgroundTintList = colorStateList(selectedSplitType == SplitType.EQUALLY, selectedColor, defaultColor)
            btnAddParticipant.backgroundTintList = colorStateList(selectedSplitType == SplitType.MANUALLY, selectedColor, defaultColor)
        }
    }

//    private fun updateSplitTypeUI() {
//        val selectedColor = ContextCompat.getColor(requireContext(), R.color.yellow)
//        val defaultColor = ContextCompat.getColor(requireContext(), R.color.hint_color)
//        val selectedIconTint = ContextCompat.getColor(requireContext(), R.color.black)
//        val defaultIconTint = ContextCompat.getColor(requireContext(), R.color.black)
//
//        viewBinding.apply {
//            btnOnePerson.apply {
//                backgroundTintList = colorStateList(selectedSplitType == SplitType.ONE_PERSON, selectedColor, defaultColor)
//                iconTint = ColorStateList.valueOf(if (selectedSplitType == SplitType.ONE_PERSON) selectedIconTint else defaultIconTint)
//            }
//
//            btnSplitEqually.apply {
//                backgroundTintList = colorStateList(selectedSplitType == SplitType.EQUALLY, selectedColor, defaultColor)
//                iconTint = ColorStateList.valueOf(if (selectedSplitType == SplitType.EQUALLY) selectedIconTint else defaultIconTint)
//            }
//
//            btnAddParticipant.apply {
//                backgroundTintList = colorStateList(selectedSplitType == SplitType.MANUALLY, selectedColor, defaultColor)
//                iconTint = ColorStateList.valueOf(if (selectedSplitType == SplitType.MANUALLY) selectedIconTint else defaultIconTint)
//            }
//        }
//    }

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

        val category = TransactionCategory.entries.find { it.getDisplayName() == categoryDisplayName }
        val apiCategory = mapCategoryToApiValue(categoryDisplayName)

        viewModel.updateFormState(
            description = description,
            totalAmount = totalAmount,
            category = category,
            splitType = selectedSplitType
        )

        val request = TransactionDetails(
            category = apiCategory,
            totalCost = totalAmount,
            description = description,
            participants = when (selectedSplitType) {
                SplitType.ONE_PERSON -> viewModel.participants.value.take(1).map {
                    it.copy(shareAmount = it.shareAmount?.toString())
                }
                else -> viewModel.participants.value.map {
                    Participant(phone = it.phone, shareAmount = it.shareAmount?.toString())
                }
            }
        )
        viewModel.validateAndCreateTransaction(tripId, request)
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

        fun getInstance(tripId: String, phone: String): AddTransactionFragment {
            return AddTransactionFragment().apply {
                arguments = bundleOf(TRIP_ID to tripId, PHONE_TEXT to phone)
            }
        }
    }
}
