// ContactsPickerFragment.kt
package ru.itis.travelling.presentation.trips.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import ru.itis.travelling.databinding.FragmentContactsPickerBinding
import ru.itis.travelling.domain.trips.model.Contact
import ru.itis.travelling.presentation.trips.list.ContactsAdapter

class ContactsPickerFragment : Fragment() {

    private var _binding: FragmentContactsPickerBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter(
            initialSelectedContacts = args.selectedContacts.toSet(),
            onContactSelected = { _, _ -> }
        )

        binding.recyclerViewContacts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ContactsPickerFragment.adapter
        }

        adapter.submitList(args.contacts)

        binding.buttonDone.setOnClickListener {
            val selected = adapter.getSelectedContacts()
            if (selected.isNotEmpty()) {
                val action = ContactsPickerFragmentDirections.actionContactsPickerFragmentToAddTripFragment(
                    selectedContacts = selected.toTypedArray(),
                    contacts = args.contacts.toTypedArray()
                )
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Выберите хотя бы один контакт", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(contacts: List<Contact>, selectedContacts: List<Contact>): ContactsPickerFragment {
            return ContactsPickerFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList("contacts", ArrayList(contacts))
                    putParcelableArrayList("selectedContacts", ArrayList(selectedContacts))
                }
            }
        }
    }
}