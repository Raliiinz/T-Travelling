package ru.itis.travelling.presentation.transactions.fragments.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import ru.itis.travelling.domain.transactions.usecase.DeleteTransactionUseCase
import ru.itis.travelling.domain.transactions.usecase.GetTransactionDetailsUseCase
import ru.itis.travelling.domain.transactions.usecase.RemindTransactionUseCase
import ru.itis.travelling.domain.transactions.usecase.UpdateTransactionUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.trips.util.FormatUtils.formatPriceWithThousands
import ru.itis.travelling.presentation.trips.util.FormatUtils.formatPriceWithoutSeparators
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import javax.inject.Inject


@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    private val getTransactionDetailsUseCase: GetTransactionDetailsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val remindTransactionUseCase: RemindTransactionUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator
) : ViewModel() {

    private val _transactionState = MutableStateFlow<TransactionDetailsState>(TransactionDetailsState.Loading)
    val transactionState: StateFlow<TransactionDetailsState> = _transactionState

    private val _events = MutableSharedFlow<TransactionDetailsEvent>()
    val events: SharedFlow<TransactionDetailsEvent> = _events

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent

    fun loadTransactionDetails(transactionId: String) {
        viewModelScope.launch {
            _transactionState.update { TransactionDetailsState.Loading }

            when (val result = getTransactionDetailsUseCase(transactionId)) {
                is ResultWrapper.Success -> {
                    result.value.let { transaction ->
                        val formattedCreator = transaction.creator?.copy(
                            phone = PhoneNumberUtils.formatPhoneNumber(transaction.creator.phone)
                        )
                        val filteredParticipants = prepareParticipantsList(
                            transaction.participants.filterNot { it.phone == transaction.creator?.phone }
                        )

                        val formattedTransaction = transaction.copy(
                            creator = formattedCreator,
                            totalCost = formatPriceWithThousands(transaction.totalCost),
                            participants = filteredParticipants,
                            category = transaction.category
                        )

                        _transactionState.update {
                            TransactionDetailsState.Success(
                                formattedTransaction
                            )
                        }
                    }
                }
                is ResultWrapper.GenericError -> {
                    handleTransactionError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
        }
    }

    fun getButtonText(currentUserPhone: String): Int {
        val normalizedCurrentPhone = PhoneNumberUtils.normalizePhoneNumber(currentUserPhone)
        val normalizedCreatorPhone = (_transactionState.value as? TransactionDetailsState.Success)
            ?.transaction?.creator?.phone?.let { PhoneNumberUtils.normalizePhoneNumber(it) }

        return if (normalizedCurrentPhone == normalizedCreatorPhone) {
            R.string.remind
        } else {
            R.string.pay_the_debt
        }
    }

    fun doAction(participantPhone: String, transactionId: String) {
        when (val currentState = _transactionState.value) {
            TransactionDetailsState.Loading -> {}
            is TransactionDetailsState.Success -> {
                val isAdmin = checkAdminStatus(participantPhone, currentState.transaction)
                if (isAdmin) {
                    remindDebtors(transactionId)
                } else {
                    payDebt(participantPhone, transactionId)
                }
            }
        }

    }

    private fun payDebt(participantPhone: String, transactionId: String) {
        viewModelScope.launch {
            val currentState = _transactionState.value as? TransactionDetailsState.Success ?: return@launch
            val currentTransaction = currentState.transaction

            val updatedParticipants = currentTransaction.participants.map { participant ->
                if (PhoneNumberUtils.normalizePhoneNumber(participant.phone) ==
                    PhoneNumberUtils.normalizePhoneNumber(participantPhone)) {
                    participant.copy(shareAmount = "0.0", phone = PhoneNumberUtils.normalizePhoneNumber(participant.phone))
                } else {
                    participant
                }
            }

            val plainAmount = formatPriceWithoutSeparators(currentTransaction.totalCost)

            val updatedTransaction = currentTransaction.copy(
                totalCost = plainAmount,
                participants = updatedParticipants
            )

            when (val result = updateTransactionUseCase(updatedTransaction, transactionId)) {
                is ResultWrapper.Success -> {
                    val successTransaction = result.value.copy(
                        totalCost = formatPriceWithThousands(result.value.totalCost)
                    )
                    _transactionState.value = TransactionDetailsState.Success(successTransaction)
                    _events.emit(TransactionDetailsEvent.DebtPaid)
                }
                is ResultWrapper.GenericError -> handleTransactionError(result.code)
                is ResultWrapper.NetworkError -> _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
            }
        }
    }

    private fun remindDebtors(transactionId: String) {
        viewModelScope.launch {
            when (val result = remindTransactionUseCase(transactionId)) {
                is ResultWrapper.Success -> {
                    _events.emit(TransactionDetailsEvent.ReminderSent)
                }
                is ResultWrapper.GenericError -> handleTransactionError(result.code)
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
        }
    }

    fun onEditClicked(currentUserPhone: String, tripId: String, transactionId: String) {
        viewModelScope.launch {
            when (val currentState = _transactionState.value) {
                is TransactionDetailsState.Success -> {
                    val isAdmin = checkAdminStatus(currentUserPhone, currentState.transaction)
                    if (isAdmin) {
                        navigator.navigateToEditTransactionFragment(
                            tripId = tripId,
                            transactionId = transactionId,
                            phone = currentUserPhone
                        )
                    } else {
                        _events.emit(TransactionDetailsEvent.ShowEditNotAllowed)
                    }
                }
                else -> _events.emit(TransactionDetailsEvent.Error("Transaction data not loaded"))
            }
        }
    }


    fun onDeleteClicked(currentUserPhone: String) {
        viewModelScope.launch {
            when (val currentState = _transactionState.value) {
                is TransactionDetailsState.Success -> {
                    val isAdmin = checkAdminStatus(currentUserPhone, currentState.transaction)
                    if (isAdmin) {
                        _events.emit(TransactionDetailsEvent.ShowDeleteConfirmation)
                    } else {
                        _events.emit(TransactionDetailsEvent.ShowDeleteNotAllowed)
                    }
                }
                else -> _events.emit(TransactionDetailsEvent.Error("Transaction not loaded"))
            }
        }
    }

    fun confirmDeleteTrip(transactionId: String) {
        viewModelScope.launch {
            _transactionState.update { TransactionDetailsState.Loading }
            when (val result = deleteTransactionUseCase(transactionId)) {
                is ResultWrapper.Success -> {
                    _events.emit(TransactionDetailsEvent.NavigateToTransactions)
                }
                is ResultWrapper.GenericError -> {
                    handleTransactionError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
        }
    }

    fun confirmDeleteTrip() {
        viewModelScope.launch {
            when (val currentState = _transactionState.value) {
                is TransactionDetailsState.Success -> {
                    currentState.transaction.id?.let { confirmDeleteTrip(it) }
                }
                else -> _events.emit(TransactionDetailsEvent.Error("Transaction not loaded"))
            }
        }
    }

    private fun checkAdminStatus(userPhone: String, transactionDetails: TransactionDetails): Boolean {
        return transactionDetails.creator?.phone?.let { PhoneNumberUtils.normalizePhoneNumber(it) } == userPhone
    }

    private fun prepareParticipantsList(participants: List<Participant>): List<Participant> {
        return participants.map { participant ->
            participant.copy(
                phone = PhoneNumberUtils.formatPhoneNumber(participant.phone)
            )
        }
    }

    fun navigateToTransactions(tripId: String, phone: String) {
        navigator.navigateToTransactionsFragment(tripId, phone)
    }

    private suspend fun handleTransactionError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.NotFound -> R.string.error_not_found_transaction
            ErrorEvent.FailureReason.Forbidden -> R.string.error_forbidden
            ErrorEvent.FailureReason.Conflict -> R.string.error_conflict
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }

    sealed class TransactionDetailsState {
        object Loading : TransactionDetailsState()
        data class Success(val transaction: TransactionDetails) : TransactionDetailsState()
    }

    sealed class TransactionDetailsEvent {
        data class Error(val message: String) : TransactionDetailsEvent()
        object NavigateToTransactions : TransactionDetailsEvent()
        object DebtPaid : TransactionDetailsEvent()
        object ShowEditNotAllowed : TransactionDetailsEvent()
        object ShowDeleteNotAllowed : TransactionDetailsEvent()
        object ShowDeleteConfirmation : TransactionDetailsEvent()
        object ReminderSent : TransactionDetailsEvent()
    }
}