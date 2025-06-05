package ru.itis.travelling.presentation.transactions.list

import androidx.recyclerview.widget.DiffUtil
import ru.itis.travelling.domain.profile.model.Participant

class ParticipantDebtDiffItemCallback : DiffUtil.ItemCallback<Participant>() {
    override fun areItemsTheSame(oldItem: Participant, newItem: Participant) = oldItem.phone == newItem.phone

    override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean = oldItem == newItem
}