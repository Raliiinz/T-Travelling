package ru.itis.travelling.presentation.trips.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.databinding.ItemParticipantBinding
import ru.itis.travelling.domain.trips.model.Participant

class ParticipantsListAdapter(
    private val onParticipantSelected: (Participant) -> Unit
) : ListAdapter<Participant, ParticipantsListAdapter.ParticipantViewHolder>(ParticipantDiffCallback()) {

    private val selectedParticipantIds = mutableSetOf<String>()

    fun setSelectedParticipants(participants: Set<Participant>) {
        selectedParticipantIds.clear()
        selectedParticipantIds.addAll(participants.map { it.id })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val binding = ItemParticipantBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParticipantViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ParticipantViewHolder(
        private val binding: ItemParticipantBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: Participant) {
            binding.apply {
//                tvParticipantName.text = participant.name
                tvPhone.text = participant.phone

//                participant.photoUri?.let { uri ->
//                    Glide.with(root.context)
//                        .load(Uri.parse(uri))
//                        .circleCrop()
//                        .into(ivParticipantPhoto)
//                } ?: ivParticipantPhoto.setImageResource(R.drawable.ic_person)
//
//                cbSelected.isChecked = selectedParticipantIds.contains(participant.id)
//
//                root.setOnClickListener {
//                    cbSelected.toggle()
//                    onParticipantSelected(participant)
//                }
//
//                cbSelected.setOnClickListener {
//                    onParticipantSelected(participant)
//                }
            }
        }
    }

    class ParticipantDiffCallback : DiffUtil.ItemCallback<Participant>() {
        override fun areItemsTheSame(oldItem: Participant, newItem: Participant): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Participant, newItem: Participant): Boolean {
            return oldItem == newItem
        }
    }
}