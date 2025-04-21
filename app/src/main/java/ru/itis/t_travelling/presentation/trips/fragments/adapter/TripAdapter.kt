package ru.itis.t_travelling.presentation.trips.fragments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.itis.t_travelling.databinding.ItemTripBinding
import ru.itis.t_travelling.domain.trips.model.Trip

class TripAdapter(private val trips: List<Trip>) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TripViewHolder(binding)
    }

    override fun getItemCount(): Int = trips.size

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    inner class TripViewHolder(
        private val binding: ItemTripBinding
    ): RecyclerView.ViewHolder(binding.root) {
//        init {
//            binding.root.setOnLongClickListener {
//                onDeleteClick(adapterPosition)
//                true
//            }
//        }

        fun bind(trip: Trip) {
//            binding.textViewWishName.text = wish.wishName
//            binding.textViewPrice.text = binding.root.context.getString(R.string.price, wish.price.toString())
//
//            wish.photo?.let { byteArray ->
//                val bitmap = Converters().toBitmap(byteArray)
//                binding.imageViewWishPhoto.setImageBitmap(bitmap)
//            } ?: run {
//                binding.imageViewWishPhoto.setImageResource(R.drawable.ic_insert_photo)
//            }
        }
    }
}
