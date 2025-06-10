package ru.itis.travelling.presentation.transactions.fragments.add.state

import androidx.annotation.StringRes
import ru.itis.travelling.R

sealed class ValidationFailure(@StringRes val messageRes: Int) {
    object EmptyCategory : ValidationFailure(R.string.error_category_empty)
    object InvalidAmount : ValidationFailure(R.string.error_invalid_amount)
    object EmptyDescription : ValidationFailure(R.string.error_description_empty)
    object NoParticipants : ValidationFailure(R.string.error_no_participants)
    object SharesNotMatchTotal : ValidationFailure(R.string.error_shares_not_match_total)
}