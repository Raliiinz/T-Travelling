package ru.itis.travelling.presentation.trips.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.R
import ru.itis.travelling.databinding.ItemTripBinding
import ru.itis.travelling.domain.trips.model.Trip
import ru.itis.travelling.presentation.trips.util.DateUtils

class TripAdapter(
    private val onItemClick: (Trip) -> Unit
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffItemCallback()) {

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

        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(trip: Trip) = with(binding) {
            tvDestination.text = trip.destination
            val startDate = DateUtils.formatDateForDisplay(trip.startDate)
            val endDate = DateUtils.formatDateForDisplay(trip.endDate)
            tvDates.text = binding.root.context.getString(R.string.trip_dates, startDate, endDate)
            tvPrice.text = binding.root.context.getString(R.string.price, trip.price)
        }
    }
}
