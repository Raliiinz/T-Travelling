package ru.itis.travelling.presentation.contacts.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.itis.travelling.R
import ru.itis.travelling.databinding.DialogContactsPickerBinding
import ru.itis.travelling.domain.contacts.model.Contact
import ru.itis.travelling.presentation.contacts.list.ContactsAdapter
import ru.itis.travelling.presentation.contacts.list.ContactsDividerItemDecoration
import ru.itis.travelling.presentation.contacts.util.getParcelableArrayListSafe

class ContactsPickerDialog(
    private val onContactsSelected: (List<Contact>) -> Unit,
    private val initiallySelectedContacts: Set<String> = emptySet()
) : DialogFragment(R.layout.dialog_contacts_picker) {

    private val viewBinding: DialogContactsPickerBinding by viewBinding(DialogContactsPickerBinding::bind)
    private lateinit var adapter: ContactsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun setupUi() {
        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter(
            initiallySelectedContacts = initiallySelectedContacts,
            onContactSelected = { _, _ -> }
        )

        with(viewBinding.recyclerViewContacts) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ContactsPickerDialog.adapter
            addItemDecoration(
                ContactsDividerItemDecoration(
                    requireContext(),
                    marginStart = resources.getDimensionPixelSize(R.dimen.margin_16dp),
                    marginEnd = resources.getDimensionPixelSize(R.dimen.margin_16dp)
                )
            )
        }

        val contacts = arguments?.getParcelableArrayListSafe<Contact>(ARG_CONTACTS) ?: emptyList()
        adapter.submitList(contacts)
    }

    private fun setupButtons() {
        viewBinding.buttonCancel.setOnClickListener {
            dismiss()
        }

        viewBinding.buttonDone.setOnClickListener {
            val selected = adapter.getSelectedContacts()
            when {
                selected.isNotEmpty() || initiallySelectedContacts.isNotEmpty() -> {
                    onContactsSelected(selected)
                    dismiss()
                }
                else -> showToast(getString(R.string.select_at_least_one_contact))
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val ARG_CONTACTS = "contacts"

        fun newInstance(
            contacts: List<Contact>,
            initiallySelectedContacts: Set<String>,
            onContactsSelected: (List<Contact>) -> Unit
        ): ContactsPickerDialog {
            return ContactsPickerDialog(onContactsSelected, initiallySelectedContacts).apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_CONTACTS, ArrayList(contacts))
                }
            }
        }
    }
}