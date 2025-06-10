package ru.itis.travelling.presentation.transactions.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.R
import ru.itis.travelling.databinding.ItemDebtParticipantBinding
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.domain.profile.model.isPaid


class ParticipantDebtAdapter() : ListAdapter<Participant, ParticipantDebtAdapter.ParticipantDebtViewHolder>(ParticipantDebtDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantDebtViewHolder {
        val binding = ItemDebtParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantDebtViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantDebtViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ParticipantDebtViewHolder(
        private val binding: ItemDebtParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: Participant) {
            with(binding) {
                tvFirstName.text = participant.firstName ?: ""
                tvLastName.text = participant.lastName ?: ""
                tvPhone.text = participant.phone
                tvAmount.text = binding.root.context.getString(R.string.price, participant.shareAmount)

                val dotColorRes = if (participant.isPaid()) {
                    R.drawable.green_dot_circle
                } else {
                    R.drawable.red_dot_circle
                }
                dotView.background = ContextCompat.getDrawable(binding.root.context, dotColorRes)
            }
        }
    }
}
