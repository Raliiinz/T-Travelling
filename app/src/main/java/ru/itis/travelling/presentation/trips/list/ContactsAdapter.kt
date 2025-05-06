package ru.itis.travelling.presentation.trips.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.itis.travelling.databinding.ItemContactBinding
import ru.itis.travelling.domain.trips.model.Contact


class ContactsAdapter(
    initiallySelectedContacts: Set<String> = emptySet(),
    private val onContactSelected: (Contact, Boolean) -> Unit
) : ListAdapter<Contact, ContactsAdapter.ViewHolder>(ContactDiffCallback()) {

    private val selectedContacts = initiallySelectedContacts.toMutableSet()

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
        val contact = getItem(position)
        holder.bind(contact)
    }

    fun getSelectedContacts(): List<Contact> {
        return currentList.filter { selectedContacts.contains(it.id) }
    }

    inner class ViewHolder(
        private val binding: ItemContactBinding,
        private val onContactSelected: (Contact, Boolean) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            with(binding) {
                tvName.text = contact.name ?: contact.phoneNumber
                tvPhone.text = contact.phoneNumber
                checkbox.isChecked = selectedContacts.contains(contact.id)

                root.setOnClickListener {
                    val newCheckedState = !checkbox.isChecked
                    checkbox.isChecked = newCheckedState
                    onCheckChanged(contact, newCheckedState)
                }

                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    onCheckChanged(contact, isChecked)
                }
            }
        }

        private fun onCheckChanged(contact: Contact, isChecked: Boolean) {
            if (isChecked) {
                selectedContacts.add(contact.id)
            } else {
                selectedContacts.remove(contact.id)
            }
            onContactSelected(contact, isChecked)
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}

