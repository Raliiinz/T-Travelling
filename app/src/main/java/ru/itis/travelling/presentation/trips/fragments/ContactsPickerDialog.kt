package ru.itis.travelling.presentation.trips.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import ru.itis.travelling.databinding.DialogContactsPickerBinding
import ru.itis.travelling.domain.trips.model.Contact
import ru.itis.travelling.presentation.trips.list.ContactsAdapter

class ContactsPickerDialog(
    private val onContactsSelected: (List<Contact>) -> Unit
) : DialogFragment() {

    private lateinit var binding: DialogContactsPickerBinding
    private lateinit var adapter: ContactsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DialogContactsPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter { _, _ ->
            // We don't need immediate feedback on selection
        }

        binding.recyclerViewContacts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ContactsPickerDialog.adapter
        }

        // Load contacts here - you might want to pass them as arguments or load from ViewModel
        val contacts = arguments?.getParcelableArrayList<Contact>(ARG_CONTACTS) ?: emptyList()
        adapter.submitList(contacts)
    }

    private fun setupButtons() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonDone.setOnClickListener {
            val selected = adapter.getSelectedContacts()
            println("Selected contacts: $selected")  // Логирование
            if (selected.isNotEmpty()) {
                onContactsSelected(selected)
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Select at least one contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_CONTACTS = "contacts"

        fun newInstance(
            contacts: List<Contact>,
            onContactsSelected: (List<Contact>) -> Unit
        ): ContactsPickerDialog {
            return ContactsPickerDialog(onContactsSelected).apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_CONTACTS, ArrayList(contacts))
                }
            }
        }
    }
}