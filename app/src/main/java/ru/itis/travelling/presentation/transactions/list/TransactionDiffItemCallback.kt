package ru.itis.travelling.presentation.transactions.list

import androidx.recyclerview.widget.DiffUtil
import ru.itis.travelling.domain.transactions.model.Transaction

class TransactionDiffItemCallback : DiffUtil.ItemCallback<Transaction>() {
    override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem == newItem
}
