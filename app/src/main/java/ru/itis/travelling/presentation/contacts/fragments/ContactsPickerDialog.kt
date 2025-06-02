package ru.itis.travelling.presentation.contacts.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.databinding.DialogContactsPickerBinding
import ru.itis.travelling.domain.contacts.model.Contact
import ru.itis.travelling.presentation.contacts.list.ContactsAdapter
import ru.itis.travelling.presentation.contacts.list.ContactsDividerItemDecoration
import ru.itis.travelling.presentation.contacts.util.getParcelableArrayListSafe

class ContactsPickerDialog(
    private val onContactsSelected: (List<Contact>) -> Unit,
) : DialogFragment(R.layout.dialog_contacts_picker) {

    private val viewBinding: DialogContactsPickerBinding by viewBinding(DialogContactsPickerBinding::bind)
    private val viewModel: ContactsViewModel by viewModels()
    private lateinit var adapter: ContactsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        observeViewModel()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    private fun setupUi() {
        setupRecyclerView()
        setupButtons()

        val contacts = arguments?.getParcelableArrayListSafe<Contact>(ARG_CONTACTS) ?: emptyList()
        val initiallySelected = arguments?.getStringArrayList(ARG_SELECTED_CONTACTS)?.toSet() ?: emptySet()
        viewModel.setContacts(contacts, initiallySelected)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contacts.collect { contacts ->
                adapter.submitList(contacts)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter { contactId ->
            viewModel.toggleContactSelection(contactId)
        }

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
    }

    private fun setupButtons() {
        viewBinding.buttonCancel.setOnClickListener {
            dismiss()
        }

        viewBinding.buttonDone.setOnClickListener {
            val selected = viewModel.contacts.value.filter { it.isSelected }
            onContactsSelected.invoke(selected)
            dismiss()
        }
    }

    companion object {
        const val CONTACTS_PICKER_DIALOG = "contact_picker_dialog"
        private const val ARG_CONTACTS = "contacts"
        private const val ARG_SELECTED_CONTACTS = "selected_contacts"

        fun newInstance(
            contacts: List<Contact>,
            initiallySelectedContacts: Set<String>,
            onContactsSelected: (List<Contact>) -> Unit
        ): ContactsPickerDialog {
            return ContactsPickerDialog(onContactsSelected).apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_CONTACTS, ArrayList(contacts))
                    putStringArrayList(ARG_SELECTED_CONTACTS, ArrayList(initiallySelectedContacts))
                }
            }
        }
    }
}