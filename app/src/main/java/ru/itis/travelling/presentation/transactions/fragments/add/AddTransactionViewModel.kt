package ru.itis.travelling.presentation.transactions.fragments.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.profile.model.ParticipantDto
import ru.itis.travelling.domain.profile.model.toParticipant
import ru.itis.travelling.domain.transactions.model.TransactionCategory
import ru.itis.travelling.domain.transactions.model.TransactionDetails
import ru.itis.travelling.domain.transactions.usecase.CreateTransactionUseCase
import ru.itis.travelling.domain.transactions.usecase.GetTransactionDetailsUseCase
import ru.itis.travelling.domain.transactions.usecase.UpdateTransactionUseCase
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.domain.trips.usecase.GetTripDetailsUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.transactions.fragments.add.state.AddTransactionUiState
import ru.itis.travelling.presentation.transactions.fragments.add.state.TransactionEvent
import ru.itis.travelling.presentation.transactions.fragments.add.state.TransactionFormState
import ru.itis.travelling.presentation.transactions.fragments.add.state.ValidationFailure
import ru.itis.travelling.presentation.transactions.util.SplitType
import ru.itis.travelling.presentation.utils.PhoneNumberUtils
import javax.inject.Inject
import kotlin.String
import kotlin.math.abs

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val createTransactionsUseCase: CreateTransactionUseCase,
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val getTransactionDetailsUseCase: GetTransactionDetailsUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator,
) : ViewModel() {

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    private val _events = MutableSharedFlow<TransactionEvent>()
    val events: SharedFlow<TransactionEvent> = _events

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val _uiState = MutableStateFlow< AddTransactionUiState>(AddTransactionUiState.Idle)
    val uiState: StateFlow<AddTransactionUiState> = _uiState

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent.asSharedFlow()

    private val _formState = MutableStateFlow<TransactionFormState>(TransactionFormState())
    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()


    fun getParticipantsForSplitType(
        splitType: SplitType,
        totalAmount: String,
        participants: List<Participant>
    ): List<Participant> {
        return when (splitType) {
            SplitType.ONE_PERSON -> participants.firstOrNull()?.let {
                listOf(it.copy(shareAmount = totalAmount))
            } ?: emptyList()

            SplitType.EQUALLY -> {
                val amount = try {
                    totalAmount.toDouble()
                } catch (e: NumberFormatException) {
                    0.0
                }
                val equalAmount = if (participants.isNotEmpty()) {
                    amount / participants.size
                } else {
                    0.0
                }
                participants.map { participant ->
                    updateParticipantAmount(participant.phone, equalAmount.toString())
                    participant.copy(shareAmount = equalAmount.toString())
                }
            }

            SplitType.MANUALLY -> {
                participants.map { participant ->
                    participant.copy(shareAmount = participant.shareAmount)
                }
            }
        }
    }

    fun loadTransactionForEditing(tripId: String, transactionId: String) {
        viewModelScope.launch {
            _uiState.value = AddTransactionUiState.Loading
            _isEditMode.value = true

            val transactionResult = getTransactionDetailsUseCase(transactionId)
            val tripResult = getTripDetailsUseCase(tripId)

            when {
                transactionResult is ResultWrapper.Success && tripResult is ResultWrapper.Success -> {
                    val transaction = transactionResult.value

                    val splitType = when {
                        transaction.participants.size == 1 -> SplitType.ONE_PERSON
                        transaction.participants.all { it.shareAmount == transaction.participants.first().shareAmount } ->
                            SplitType.EQUALLY
                        else -> SplitType.MANUALLY
                    }

                    _formState.update {
                        it.copy(
                            description = transaction.description,
                            totalAmount = transaction.totalCost,
                            category = TransactionCategory.valueOf(transaction.category.uppercase()),
                            splitType = splitType
                        )
                    }

                    val creatorParticipant = transaction.creator?.toParticipant()?.copy(
                        phone = PhoneNumberUtils.formatPhoneNumber(transaction.creator.phone)
                    )
                    val allParticipants = transaction.participants.map { participant ->
                        participant.copy(
                            phone = PhoneNumberUtils.formatPhoneNumber(participant.phone)
                        )
                    }

                    val combinedParticipants = listOf(creatorParticipant) + allParticipants
                    val uniqueParticipants = combinedParticipants.distinctBy { it?.phone }

                    val updatedParticipants = uniqueParticipants.map { participant ->
                        val share = transaction.participants.find {
                            PhoneNumberUtils.normalizePhoneNumber(it.phone) ==
                                    participant?.phone?.let { it1 -> PhoneNumberUtils.normalizePhoneNumber(it1) }
                        }?.shareAmount ?: DEFAULT_SHARE_AMOUNT
                        participant?.copy(shareAmount = share)
                    }

                    _participants.value = updatedParticipants as List<Participant>
                }
                transactionResult is ResultWrapper.GenericError -> handleTripError(transactionResult.code)
                tripResult is ResultWrapper.GenericError -> handleTripError(tripResult.code)
                transactionResult is ResultWrapper.NetworkError || tripResult is ResultWrapper.NetworkError ->
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
            }
            _uiState.value = AddTransactionUiState.Idle
        }
    }

    fun loadParticipants(tripId: String, userPhone: String) {
        viewModelScope.launch {
            _uiState.value = AddTransactionUiState.Loading
            val result = getTripDetailsUseCase(tripId)
            when (result) {
                is ResultWrapper.Success -> {
                    val normalizedUserPhone = PhoneNumberUtils.formatPhoneNumber(userPhone)
                    val adminParticipant = result.value.admin.toParticipant().copy(
                        phone = PhoneNumberUtils.formatPhoneNumber(result.value.admin.phone)
                    )
                    val allParticipants = result.value.participants.map { participant ->
                        participant.toParticipant().copy(
                            phone = PhoneNumberUtils.formatPhoneNumber(participant.phone)
                        )
                    }
                    val combinedParticipants = listOf(adminParticipant) + allParticipants
                    val uniqueParticipants = combinedParticipants.distinctBy { it.phone }
                    val currentUser = uniqueParticipants.find {
                        it.phone == normalizedUserPhone
                    } ?: adminParticipant
                    val otherParticipants = uniqueParticipants.filterNot {
                        it.phone == normalizedUserPhone
                    }
                    _participants.value = listOf(currentUser) + otherParticipants
                }
                is ResultWrapper.GenericError -> handleTripError(result.code)
                is ResultWrapper.NetworkError -> _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
            }
            _uiState.value = AddTransactionUiState.Idle
        }
    }

    fun updateParticipantAmount(phone: String, amount: String) {
        _participants.update { currentList ->
            currentList.map { participant ->
                if (participant.phone == phone) {
                    participant.copy(shareAmount = amount.takeIf { it.isNotBlank() } ?: DEFAULT_SHARE_AMOUNT)
                } else {
                    participant
                }
            }
        }
    }

    fun updateFormState(
        description: String = formState.value.description,
        totalAmount: String = formState.value.totalAmount,
        category: TransactionCategory? = formState.value.category,
        splitType: SplitType = formState.value.splitType
    ) {
        _formState.update {
            it.copy(
                description = description,
                totalAmount = totalAmount,
                category = category,
                splitType = splitType
            )
        }
    }

    fun validateAndCreateTransaction(tripId: String, transactionId: String, request: TransactionDetails) {
        viewModelScope.launch {
            validateTransaction(request).takeIf { it.isNotEmpty() }?.let { errors ->
                _events.emit(TransactionEvent.ValidationError(errors))
                return@launch
            }
            if (_isEditMode.value) {
                updateTransaction(transactionId, request)
            } else {
                createTransaction(tripId, request)
            }
        }
    }

    private fun validateTransaction(request: TransactionDetails): Set<ValidationFailure> {
        val errors = mutableSetOf<ValidationFailure>().apply {
            if (request.category.isBlank()) add(ValidationFailure.EmptyCategory)

            val totalAmount = request.totalCost.toDoubleOrNull()
            when {
                request.totalCost.isBlank() -> add(ValidationFailure.InvalidAmount)
                totalAmount == null -> add(ValidationFailure.InvalidAmount)
                totalAmount <= 0 -> add(ValidationFailure.InvalidAmount)
            }

            if (request.description.isBlank()) add(ValidationFailure.EmptyDescription)
            if (request.participants.isEmpty()) add(ValidationFailure.NoParticipants)

            if (totalAmount != null) {
                val totalShares = request.participants.sumOf { participant ->
                    println(participant.shareAmount)
                    participant.shareAmount?.toDoubleOrNull() ?: 0.0
                }

                if (abs(totalAmount - totalShares) > 0.1) {
                    add(ValidationFailure.SharesNotMatchTotal)
                }
            }
        }
        return errors
    }

    private fun updateTransaction(transactionId: String, transactionDetails: TransactionDetails) {
        viewModelScope.launch {
            _uiState.update { AddTransactionUiState.Loading }

            val normalizedDetails = normalizeTransactionDetails(transactionDetails)

            when (val result = updateTransactionUseCase(normalizedDetails, transactionId)) {
                is ResultWrapper.Success -> {
                    _uiState.update { AddTransactionUiState.Success }
                }
                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
            _uiState.update { AddTransactionUiState.Idle }
        }
    }

    private fun normalizeTransactionDetails(details: TransactionDetails): TransactionDetails {
        return details.copy(
            participants = details.participants.map { participant ->
                participant.copy(
                    phone = PhoneNumberUtils.normalizePhoneNumber(participant.phone)
                )
            },
            creator = details.creator?.let { creator ->
                creator.copy(
                    phone = PhoneNumberUtils.normalizePhoneNumber(creator.phone)
                )
            }
        )
    }

    fun createTransaction(tripId: String, transactionDetails: TransactionDetails) {
        viewModelScope.launch {
            _uiState.update { AddTransactionUiState.Loading }
            val normalizedParticipants = transactionDetails.participants.map { participant ->
                participant.copy(
                    phone = PhoneNumberUtils.normalizePhoneNumber(participant.phone)
                )
            }

            val normalizedCreator = transactionDetails.creator?.let { creator ->
                creator.copy(
                    phone = PhoneNumberUtils.normalizePhoneNumber(creator.phone)
                )
            }

            val normalizedDetails = transactionDetails.copy(
                participants = normalizedParticipants,
                creator = normalizedCreator
            )

            when (val result = createTransactionsUseCase(tripId, normalizedDetails)) {
                is ResultWrapper.Success -> {
                    _uiState.update { AddTransactionUiState.Success }
                }
                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
            _uiState.update { AddTransactionUiState.Idle }
        }
    }

    fun navigateToTransactions(tripId: String, phone: String) {
        viewModelScope.launch {
            navigator.navigateToTransactionsFragment(tripId, phone)
        }
    }

    private suspend fun handleTripError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.BadRequest -> R.string.error_bad_request_add
            ErrorEvent.FailureReason.Unauthorized -> R.string.error_unauthorized_trip
            ErrorEvent.FailureReason.Forbidden -> R.string.error_forbidden
            ErrorEvent.FailureReason.NotFound -> R.string.error_not_found_trip
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }

    companion object {
        const val ZERO_AMOUNT = "0.0"
        const val DEFAULT_SHARE_AMOUNT = "0"
    }
}
