package ru.itis.t_travelling.presentation.trips.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.ItemTripBinding
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.presentation.trips.util.FormatUtils

class TripAdapter : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TripViewHolder(
        private val binding: ItemTripBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(trip: Trip) = with(binding) {
            tvDestination.text = trip.destination
            tvDates.text = binding.root.context.getString(R.string.trip_dates, trip.startDate, trip.endDate)
            tvPrice.text = binding.root.context.getString(
                R.string.price,
                FormatUtils.formatPriceWithThousands(trip.price)
            )
        }
    }
}