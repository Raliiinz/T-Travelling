package ru.itis.travelling.presentation.trips.list

import androidx.recyclerview.widget.DiffUtil
import ru.itis.travelling.domain.trips.model.Participant

class ParticipantDiffItemCallback : DiffUtil.ItemCallback<Participant>() {
    override fun areItemsTheSame(
        oldItem: Participant,
        newItem: Participant
    ): Boolean {
        return oldItem.phone == newItem.phone
    }

    override fun areContentsTheSame(
        oldItem: Participant,
        newItem: Participant
    ): Boolean {
        return oldItem == newItem
    }
}
