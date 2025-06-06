package ru.itis.travelling.presentation.transactions.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.R
import ru.itis.travelling.databinding.ItemTransactionBinding
import ru.itis.travelling.domain.transactions.model.Transaction
import ru.itis.travelling.domain.transactions.model.getDisplayName
import ru.itis.travelling.domain.transactions.model.getIconResId

class TransactionAdapter(
    private val onItemClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(
        private val binding: ItemTransactionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(transaction: Transaction) = with(binding) {
            val context = binding.root.context

            tvCategory.text = transaction.category.getDisplayName(context)
            tvDescription.text = transaction.description
            tvPrice.text = binding.root.context.getString(R.string.price, transaction.totalCost)
            ivImage.setImageResource(transaction.category.getIconResId())
        }
    }
}
