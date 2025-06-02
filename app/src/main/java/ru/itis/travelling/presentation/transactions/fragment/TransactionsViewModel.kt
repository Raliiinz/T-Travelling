package ru.itis.travelling.presentation.transactions.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.data.network.model.ResultWrapper
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.domain.transactions.usecase.GetTransactionsUseCase
import ru.itis.travelling.domain.util.ErrorCodeMapper
import ru.itis.travelling.presentation.base.navigation.Navigator
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.trips.fragments.details.TripDetailsViewModel.TripDetailsState
import ru.itis.travelling.presentation.trips.util.DateUtils
import ru.itis.travelling.presentation.trips.util.FormatUtils
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val errorCodeMapper: ErrorCodeMapper,
    private val navigator: Navigator
) : ViewModel() {

    private val _transactionsState = MutableStateFlow<TransactionsState>(TransactionsState.Loading)
    val transactionsState: StateFlow<TransactionsState> = _transactionsState

    private val _errorEvent = MutableSharedFlow<ErrorEvent>()
    val errorEvent: SharedFlow<ErrorEvent> = _errorEvent

    fun loadTransactions(travelId: String) {
        viewModelScope.launch {
            _transactionsState.update { TransactionsState.Loading }
            delay(2000)
            when (val result = getTransactionsUseCase(travelId)) {
                is ResultWrapper.Success -> {
                    val transactions = result.value.map{ transaction ->
                        transaction.copy(
                           totalCost = FormatUtils.formatPriceWithThousands(transaction.totalCost),
                        )
                    }
                    _transactionsState.value = TransactionsState.Success(transactions)
                }

                is ResultWrapper.GenericError -> {
                    handleTripError(result.code)
                }
                is ResultWrapper.NetworkError -> {
                    _errorEvent.emit(ErrorEvent.MessageOnly(R.string.error_network))
                }
            }
            _transactionsState.update { TransactionsState.Idle }
        }
    }


    private suspend fun handleTripError(code: Int?) {
        val reason = errorCodeMapper.fromCode(code)
        val messageRes = when (reason) {
            ErrorEvent.FailureReason.Forbidden -> R.string.error_forbidden
            ErrorEvent.FailureReason.NotFound -> R.string.error_not_found_transactions
            ErrorEvent.FailureReason.Server -> R.string.error_server
            ErrorEvent.FailureReason.Network -> R.string.error_network
            else -> R.string.error_unknown
        }
        _errorEvent.emit(ErrorEvent.MessageOnly(messageRes))
    }

    fun navigateToTripDetail(tripId: String, phoneNumber: String) {
        navigator.navigateToTripDetailsFragment(tripId, phoneNumber)
    }

    sealed class TransactionsState {
        object Idle : TransactionsState()
        object Loading : TransactionsState()
        data class Success(val transactions: List<Transaction>) : TransactionsState()
    }
}