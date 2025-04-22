package ru.itis.t_travelling.presentation.trips.list

import androidx.recyclerview.widget.DiffUtil
import ru.itis.t_travelling.domain.trips.model.Trip

class TripDiffItemCallback : DiffUtil.ItemCallback<Trip>() {
    override fun areItemsTheSame(oldItem: Trip, newItem: Trip) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean = oldItem == newItem
}
