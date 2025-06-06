package ru.itis.travelling.presentation.trips.list

import androidx.recyclerview.widget.DiffUtil
import ru.itis.travelling.domain.profile.model.ParticipantDto

class ParticipantDiffItemCallback : DiffUtil.ItemCallback<ParticipantDto>() {
    override fun areItemsTheSame(
        oldItem: ParticipantDto,
        newItem: ParticipantDto
    ): Boolean {
        return oldItem.phone == newItem.phone
    }

    override fun areContentsTheSame(
        oldItem: ParticipantDto,
        newItem: ParticipantDto
    ): Boolean {
        return oldItem == newItem
    }
}
