package ru.itis.travelling.presentation.transactions.fragments.add.state

sealed class AddTransactionUiState {
    data object Idle : AddTransactionUiState()
    data object Loading : AddTransactionUiState()
    data object Success : AddTransactionUiState()
}