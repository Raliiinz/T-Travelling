package ru.itis.travelling.presentation.trips.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.R
import ru.itis.travelling.databinding.ItemParticipantBinding
import ru.itis.travelling.domain.trips.model.Participant

class ParticipantAdapter() : ListAdapter<Participant, ParticipantAdapter.ParticipantViewHolder>(ParticipantDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(getItem(position), position == 0)
    }

    inner class ParticipantViewHolder(
        private val binding: ItemParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: Participant, isFirstItem: Boolean) = with(binding) {
            tvPhone.text = participant.phone

            val cardView = root
            if (isFirstItem) {
                cardView.strokeWidth = ADMIN_STROKE_WIDTH
                cardView.strokeColor = ContextCompat.getColor(root.context, ADMIN_STROKE_COLOR)
            } else {
                cardView.strokeWidth = REGULAR_STROKE_WIDTH
            }
        }
    }

    companion object {
        const val ADMIN_STROKE_WIDTH = 4
        const val REGULAR_STROKE_WIDTH = 0
        @ColorRes
        val ADMIN_STROKE_COLOR = R.color.blue
    }
}
