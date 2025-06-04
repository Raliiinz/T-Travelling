package ru.itis.travelling.presentation.transactions.list

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.databinding.ItemParticipantTransactionBinding
import ru.itis.travelling.domain.profile.model.Participant

class ParticipantTransactionAdapter(
    private val onAmountChanged: (Participant, String) -> Unit
) : ListAdapter<Participant, ParticipantTransactionAdapter.ParticipantTransactionViewHolder>(ParticipantTransactionDiffItemCallback()) {

    private val textWatchers = mutableMapOf<Int, TextWatcher>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantTransactionViewHolder {
        val binding = ItemParticipantTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantTransactionViewHolder(binding, onAmountChanged)
    }

    override fun onBindViewHolder(holder: ParticipantTransactionViewHolder, position: Int) {
        holder.binding.etAmount.removeTextChangedListener(textWatchers[position])
        holder.bind(getItem(position))
        textWatchers[position] = holder.getTextWatcher()
    }

    inner class ParticipantTransactionViewHolder(
        val binding: ItemParticipantTransactionBinding,
        private val onAmountChanged: (Participant, String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private lateinit var textWatcher: TextWatcher

        fun bind(participant: Participant) = with(binding) {
            tvFirstName.text = participant.firstName ?: ""
            tvLastName.text = participant.lastName ?: ""
            tvPhone.text = participant.phone

            etAmount.setText(participant.shareAmount ?: "0")

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (etAmount.hasFocus()) {
                        val amount = s?.toString() ?: "0"
                        onAmountChanged(participant.copy(shareAmount = amount), amount)
                    }
                }
            }
            etAmount.addTextChangedListener(textWatcher)
        }

        fun getTextWatcher(): TextWatcher = textWatcher
    }
}
