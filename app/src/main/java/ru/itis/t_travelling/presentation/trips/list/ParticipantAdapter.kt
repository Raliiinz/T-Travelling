package ru.itis.t_travelling.presentation.trips.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.ItemParticipantBinding
import ru.itis.t_travelling.domain.trips.model.Participant

class ParticipantAdapter () : ListAdapter<Participant, ParticipantAdapter.ParticipantViewHolder>(ParticipantDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val isAdmin = position == 0
        holder.bind(getItem(position), isAdmin)
    }

    inner class ParticipantViewHolder(
        private val binding: ItemParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: Participant, isAdmin: Boolean) = with(binding) {
            tvPhone.text = participant.phone

            val cardView = root
            if (isAdmin) {
                cardView.strokeWidth = 4
                cardView.strokeColor = ContextCompat.getColor(root.context, R.color.blue)
            } else {
                cardView.strokeWidth = 0
            }
        }
    }
}


