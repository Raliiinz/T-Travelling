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

//@HiltViewModel
//class AddTransactionViewModel @Inject constructor(
//    private val createTransactionsUseCase: CreateTransactionUseCase,
//    private val getTripDetailsUseCase: GetTripDetailsUseCase,
//    private val getTransactionDetailsUseCase: GetTransactionDetailsUseCase,
//    private val updateTransactionUseCase: UpdateTransactionUseCase,
//    private val errorCodeMapper: ErrorCodeMapper,
//    private val navigator: Navigator,
//) : ViewModel() {
//
//    // Состояния UI
//    private val _uiState = MutableStateFlow<AddTransactionUiState>(AddTransactionUiState.Idle)
//    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()
//
//    // Участники транзакции
//    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
//    val participants: StateFlow<List<Participant>> = _participants.asStateFlow()
//
//    // Состояние формы
//    private val _formState = MutableStateFlow(TransactionFormState())
//    val formState: StateFlow<TransactionFormState> = _formState.asStateFlow()
//
//    // Флаги и события
//    private val _isEditMode = MutableStateFlow(false)
//    val isEditMode: StateFlow<Boolean> = _isEditMode
//
//    private val _events = MutableSharedFlow<TransactionEvent>()
//    val events: SharedFlow<TransactionEvent> = _events
//
//    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
//    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent.asSharedFlow()
//
//    // Загрузка данных
//    // ============================================================
//
//    fun loadTransactionForEditing(tripId: String, transactionId: String) {
//        viewModelScope.launch {
//            safeLoad {
//                _isEditMode.value = true
//
//                val transaction = getTransactionDetailsUseCase(transactionId).getOrThrow()
//                val trip = getTripDetailsUseCase(tripId).getOrThrow()
//
//                updateFormForEditing(transaction, trip)
//            }
//        }
//    }
//
//    private suspend fun updateFormForEditing(transaction: TransactionDetails, trip: TripDetails) {
//        val splitType = determineSplitType(transaction.participants)
//        val formattedParticipants = formatParticipantsForEditing(transaction, trip)
//
//        _formState.update {
//            it.copy(
//                description = transaction.description,
//                totalAmount = transaction.totalCost,
//                category = TransactionCategory.valueOf(transaction.category),
//                splitType = splitType
//            )
//        }
//
//        _participants.value = formattedParticipants
//    }
//
//    private fun determineSplitType(participants: List<Participant>): SplitType {
//        return when {
//            participants.size == 1 -> SplitType.ONE_PERSON
//            participants.all { it.shareAmount == participants.first().shareAmount } -> SplitType.EQUALLY
//            else -> SplitType.MANUALLY
//        }
//    }
//
//    private fun formatParticipantsForEditing(
//        transaction: TransactionDetails,
//        trip: TripDetails
//    ): List<Participant> {
//        val creator = transaction.creator?.toParticipant()?.formatPhone()
//        val transactionParticipants = transaction.participants.map { it.formatPhone() }
//
//        return (listOf(creator) + transactionParticipants)
//            .distinctBy { it?.phone }
//            .mapNotNull { participant ->
//                participant?.copy(
//                    shareAmount = transaction.participants
//                        .find { it.phone.normalize() == participant.phone?.normalize() }
//                        ?.shareAmount ?: DEFAULT_SHARE_AMOUNT
//                )
//            }
//    }
//
//    fun loadParticipants(tripId: String, userPhone: String) {
//        viewModelScope.launch {
//            safeLoad {
//                val trip = getTripDetailsUseCase(tripId).getOrThrow()
//                _participants.value = formatParticipants(trip, userPhone)
//            }
//        }
//    }
//
//    private fun formatParticipants(trip: TripDetails, userPhone: String): List<Participant> {
//        val normalizedUserPhone = userPhone.format()
//        val admin = trip.admin.toParticipant().formatPhone()
//        val others = trip.participants.map { it.toParticipant().formatPhone() }
//
//        val allParticipants = (listOf(admin) + others).distinctBy { it.phone }
//        val currentUser = allParticipants.find { it.phone == normalizedUserPhone } ?: admin
//
//        return listOf(currentUser) + allParticipants.filterNot { it.phone == normalizedUserPhone }
//    }
//
//    // Обновление состояния
//    // ============================================================
//
//    fun updateSplitType(splitType: SplitType) {
//        _formState.update { it.copy(splitType = splitType) }
//        updateParticipantsForSplitType()
//    }
//
//    private fun updateParticipantsForSplitType() {
//        val state = formState.value
//        val updatedParticipants = when (state.splitType) {
//            SplitType.ONE_PERSON -> calculateOnePersonSplit(state.totalAmount)
//            SplitType.EQUALLY -> calculateEqualSplit(state.totalAmount)
//            SplitType.MANUALLY -> _participants.value // Оставляем текущие значения
//        }
//        _participants.value = updatedParticipants
//    }
//
//    private fun calculateOnePersonSplit(totalAmount: String): List<Participant> {
//        return _participants.value.firstOrNull()?.let { first ->
//            listOf(first.copy(shareAmount = totalAmount.takeIf { it.isNotBlank() } ?: DEFAULT_SHARE_AMOUNT))
//        } ?: emptyList()
//    }
//
//    private fun calculateEqualSplit(totalAmount: String): List<Participant> {
//        val amount = totalAmount.toDoubleOrNull() ?: 0.0
//        val count = _participants.value.size
//        val share = if (count > 0) amount / count else 0.0
//
//        return _participants.value.map {
//            it.copy(shareAmount = share.toString())
//        }
//    }
//
//    fun updateTotalAmount(amount: String) {
//        _formState.update { it.copy(totalAmount = amount) }
//        updateParticipantsForSplitType()
//    }
//
//    fun updateParticipantAmount(phone: String, amount: String) {
//        _participants.update { currentList ->
//            currentList.map { participant ->
//                if (participant.phone == phone) {
//                    participant.copy(shareAmount = amount.ifBlank { DEFAULT_SHARE_AMOUNT })
//                } else {
//                    participant
//                }
//            }
//        }
//    }
//
//    // Создание/обновление транзакции
//    // ============================================================
//
//    fun createTransaction(
//        tripId: String,
//        transactionId: String,
//        description: String,
//        categoryDisplayName: String
//    ) {
//        viewModelScope.launch {
//            val request = buildTransactionRequest(
//                tripId = tripId,
//                transactionId = transactionId,
//                description = description,
//                categoryDisplayName = categoryDisplayName
//            )
//
//            validateAndProcessTransaction(tripId, transactionId, request)
//        }
//    }
//
//    private suspend fun buildTransactionRequest(
//        tripId: String,
//        transactionId: String,
//        description: String,
//        categoryDisplayName: String
//    ): TransactionDetails {
//        val category = TransactionCategory.fromDisplayName(requireContext(), categoryDisplayName)
//
//        return TransactionDetails(
//            tripId = tripId,
//            transactionId = if (_isEditMode.value) transactionId else null,
//            description = description,
//            category = category.name,
//            totalCost = formState.value.totalAmount,
//            participants = _participants.value.map { participant ->
//                ParticipantDto(
//                    phone = participant.phone.normalize(),
//                    shareAmount = participant.shareAmount
//                )
//            },
//            creator = _participants.value.firstOrNull()?.let {
//                ParticipantDto(
//                    phone = it.phone.normalize(),
//                    shareAmount = it.shareAmount
//                )
//            }
//        )
//    }
//
//    private suspend fun validateAndProcessTransaction(
//        tripId: String,
//        transactionId: String,
//        request: TransactionDetails
//    ) {
//        validateTransaction(request).takeIf { it.isNotEmpty() }?.let { errors ->
//            _events.emit(TransactionEvent.ValidationError(errors))
//            return
//        }
//
//        safeLoad {
//            if (_isEditMode.value) {
//                updateTransactionUseCase(normalizePhoneNumbers(request), transactionId).getOrThrow()
//            } else {
//                createTransactionsUseCase(tripId, normalizePhoneNumbers(request)).getOrThrow()
//            }
//            _uiState.value = AddTransactionUiState.Success
//        }
//    }
//
//    private fun validateTransaction(request: TransactionDetails): Set<ValidationFailure> {
//        val errors = mutableSetOf<ValidationFailure>()
//
//        if (request.category.isBlank()) errors.add(ValidationFailure.EmptyCategory)
//        if (request.description.isBlank()) errors.add(ValidationFailure.EmptyDescription)
//        if (request.participants.isEmpty()) errors.add(ValidationFailure.NoParticipants)
//
//        val totalAmount = request.totalCost.toDoubleOrNull()
//        when {
//            request.totalCost.isBlank() -> errors.add(ValidationFailure.InvalidAmount)
//            totalAmount == null -> errors.add(ValidationFailure.InvalidAmount)
//            totalAmount <= 0 -> errors.add(ValidationFailure.InvalidAmount)
//        }
//
//        if (totalAmount != null) {
//            val totalShares = request.participants.sumOf { it.shareAmount?.toDoubleOrNull() ?: 0.0 }
//            if (abs(totalAmount - totalShares) > 0.1) {
//                errors.add(ValidationFailure.SharesNotMatchTotal)
//            }
//        }
//
//        return errors
//    }
//
//    private fun normalizePhoneNumbers(details: TransactionDetails): TransactionDetails {
//        return details.copy(
//            participants = details.participants.map { it.copy(phone = it.phone.normalize()) },
//            creator = details.creator?.copy(phone = details.creator.phone.normalize())
//        )
//    }
//
//    // Вспомогательные функции
//    // ============================================================
//
//    private suspend fun safeLoad(block: suspend () -> Unit) {
//        _uiState.value = AddTransactionUiState.Loading
//        try {
//            block()
//        } catch (e: ResultWrapper.GenericError) {
//            handleError(e.code)
//        } catch (e: ResultWrapper.NetworkError) {
//            _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
//        } catch (e: Exception) {
//            _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_unknown))
//        } finally {
//            _uiState.value = AddTransactionUiState.Idle
//        }
//    }
//
//    private suspend fun handleError(code: Int?) {
//        val reason = errorCodeMapper.fromCode(code)
//        val messageRes = when (reason) {
//            ErrorEvent.FailureReason.BadRequest -> R.string.error_bad_request_add
//            ErrorEvent.FailureReason.Unauthorized -> R.string.error_unauthorized_trip
//            ErrorEvent.FailureReason.Forbidden -> R.string.error_forbidden
//            ErrorEvent.FailureReason.NotFound -> R.string.error_not_found_trip
//            ErrorEvent.FailureReason.Server -> R.string.error_server
//            ErrorEvent.FailureReason.Network -> R.string.error_network
//            else -> R.string.error_unknown
//        }
//        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
//    }
//
//    fun navigateToTransactions(tripId: String, phone: String) {
//        viewModelScope.launch {
//            navigator.navigateToTransactionsFragment(tripId, phone)
//        }
//    }
//
//    companion object {
//        const val DEFAULT_SHARE_AMOUNT = "0"
//    }
//}
//
//// Расширения для упрощения кода
//private fun String.format(): String = PhoneNumberUtils.formatPhoneNumber(this)
//private fun String.normalize(): String = PhoneNumberUtils.normalizePhoneNumber(this)
//private fun Participant.formatPhone(): Participant = phone?.format()?.let { copy(phone = it) }!!
//private fun ParticipantDto.formatPhone(): ParticipantDto = copy(phone = phone.format())
//
//private suspend fun <T> ResultWrapper<T>.getOrThrow(): T {
//    return when (this) {
//        is ResultWrapper.Success -> value
//        is ResultWrapper.GenericError -> throw this
//        is ResultWrapper.NetworkError -> throw this
//    }
//}

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
