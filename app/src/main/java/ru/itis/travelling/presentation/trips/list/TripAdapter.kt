package ru.itis.travelling.presentation.trips.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.R
import ru.itis.travelling.databinding.ItemTripBinding
import ru.itis.travelling.domain.trips.model.TripDetails

class TripAdapter(
    private val onItemClick: (TripDetails) -> Unit
) : ListAdapter<TripDetails, TripAdapter.TripViewHolder>(TripDiffItemCallback()) {

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

        fun bind(trip: TripDetails) = with(binding) {
            tvDestination.text = trip.destination
            tvDates.text = binding.root.context.getString(R.string.trip_dates, trip.startDate, trip.endDate)
            tvPrice.text = binding.root.context.getString(R.string.price, trip.price)
        }
    }
}
