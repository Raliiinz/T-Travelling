package ru.itis.t_travelling.presentation.trips.list

import androidx.recyclerview.widget.DiffUtil
import ru.itis.t_travelling.domain.trips.model.Participant

class ParticipantDiffItemCallback : DiffUtil.ItemCallback<Participant>() {
    override fun areItemsTheSame(
        oldItem: Participant,
        newItem: Participant
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: Participant,
        newItem: Participant
    ): Boolean {
        return oldItem == newItem
    }
}