package ru.itis.travelling.presentation.contacts.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.databinding.ItemContactBinding
import ru.itis.travelling.domain.contacts.model.Contact


class ContactsAdapter(
    private val onContactSelected: (String) -> Unit
) : ListAdapter<Contact, ContactsAdapter.ViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onContactSelected
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemContactBinding,
        private val onContactSelected: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            with(binding) {
                tvName.text = contact.name ?: contact.phoneNumber
                tvPhone.text = contact.phoneNumber
                checkbox.isChecked = contact.isSelected

                root.setOnClickListener {
                    onContactSelected(contact.phoneNumber)
                }

                checkbox.setOnClickListener {
                    onContactSelected(contact.phoneNumber)
                }
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.phoneNumber == newItem.phoneNumber
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}

