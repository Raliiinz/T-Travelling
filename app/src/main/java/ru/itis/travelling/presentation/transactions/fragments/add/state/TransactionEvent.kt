package ru.itis.travelling.presentation.transactions.fragments.add.state

sealed class TransactionEvent {
    data class ValidationError(val errors: Set<ValidationFailure>) : TransactionEvent()
}