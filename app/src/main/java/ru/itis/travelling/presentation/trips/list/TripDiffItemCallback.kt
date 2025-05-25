package ru.itis.travelling.presentation.trips.list

import androidx.recyclerview.widget.DiffUtil
import ru.itis.travelling.domain.trips.model.TripDetails

class TripDiffItemCallback : DiffUtil.ItemCallback<TripDetails>() {
    override fun areItemsTheSame(oldItem: TripDetails, newItem: TripDetails) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: TripDetails, newItem: TripDetails): Boolean = oldItem == newItem
}
