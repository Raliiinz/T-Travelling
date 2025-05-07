package ru.itis.travelling.presentation.trips.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.databinding.DialogAddTripBinding
import ru.itis.travelling.domain.trips.model.Contact
import ru.itis.travelling.presentation.trips.list.ParticipantAdapter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.getValue

@AndroidEntryPoint
class AddTripBottomSheet : BottomSheetDialogFragment(R.layout.dialog_add_trip) {
    private val viewBinding: DialogAddTripBinding by viewBinding(DialogAddTripBinding::bind)
    private val viewModel: AddTripViewModel by viewModels()
    private lateinit var participantsAdapter: ParticipantAdapter
    private val phoneNumber: String by lazy {
        arguments?.getString(PHONE_NUMBER) ?: ""
    }

    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.loadContacts()
            observeContactsAndShowPicker()
        } else {
            showPermissionDeniedDialog()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.initializeWithAdmin(phoneNumber)
        setupParticipantsAdapter()
        initViews()
        setupObservers()
    }

    private fun setupParticipantsAdapter() {
        participantsAdapter = ParticipantAdapter()
        viewBinding.participantsRecyclerView.apply {
            adapter = participantsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun initViews() {
        viewBinding.apply {
            updateDateViews(viewModel.datesState.value)

            ivBackArrow.setOnClickListener { dismissWithNavigation() }
            selectStartDate.setOnClickListener { showDatePicker(isStartDate = true) }
            selectEndDate.setOnClickListener { showDatePicker(isStartDate = false) }
            tvAddContactsLink.setOnClickListener {
                checkContactsPermission()
            }

            createButton.setOnClickListener {
                if (validateInput()) {
                    viewModel.createTrip(
                        etTitleTrip.text.toString(),
                        etCostTrip.text.toString(),
                        phoneNumber
                    )
                }
            }
        }
    }

    private fun updateDateViews(dates: Pair<LocalDate, LocalDate>) {
        with(viewBinding) {
            startDateValue.text = dates.first.toBeautifulString()
            endDateValue.text = dates.second.toBeautifulString()
        }
    }

    private fun checkContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.loadContacts()
            observeContactsAndShowPicker()
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun observeContactsAndShowPicker() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.contactsState.collect { contacts ->
                    if (contacts.isNotEmpty()) {
                        showContactsPicker(contacts)
                    }
                }
            }
        }
    }


    private fun showContactsPicker(contacts: List<Contact>) {
        val selectedIds = viewModel.fullParticipants.value
            .filterNot { it.phone == phoneNumber }
            .map { it.id }
            .toSet()

        ContactsPickerDialog(
            onContactsSelected = { viewModel.addParticipants(it, phoneNumber) },
            initiallySelectedContacts = selectedIds
        ).apply {
            arguments = Bundle().apply {
                putParcelableArrayList("contacts", ArrayList(contacts))
            }
        }.show(parentFragmentManager, "ContactsPickerDialog")
    }

    private fun showPermissionDeniedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_required))
            .setMessage(getString(R.string.permission_denied))
            .setPositiveButton(getString(R.string.open_settings)) { _, _ ->
                openAppSettings()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }.also { startActivity(it) }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { observeParticipants() }
                launch { observeUiState() }
                launch { observeDates() }
            }
        }
    }

    private suspend fun observeParticipants() {
        viewModel.fullParticipants.collect {
            participantsAdapter.submitList(it)
        }
    }

    private suspend fun observeUiState() {
        viewModel.uiState.collect { state ->
            when (state) {
                is AddTripViewModel.AddTripUiState.Loading -> showProgress(true)
                is AddTripViewModel.AddTripUiState.Success -> {
                    dismissAllowingStateLoss()
                }
                AddTripViewModel.AddTripUiState.Idle -> Unit
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is AddTripViewModel.AddTripEvent.Error -> {
                        showToast(event.message)
                    }
                }
            }
        }
    }

    private suspend fun observeDates() {
        viewModel.datesState.collect { updateDateViews(it) }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val currentDates = viewModel.datesState.value
        val constraints = CalendarConstraints.Builder().apply {
            setStart(if (isStartDate) MaterialDatePicker.todayInUtcMilliseconds()
            else currentDates.first.toEpochMilli())
        }

        MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(if (isStartDate) R.string.select_start_date else R.string.select_end_date))
            .setSelection(currentDates.let { if (isStartDate) it.first else it.second }.toEpochMilli())
            .setCalendarConstraints(constraints.build())
            .build()
            .apply {
                addOnPositiveButtonClickListener { millis ->
                    viewModel.updateDates(calculateNewDates(
                        isStartDate,
                        millis.toLocalDate()
                    ))
                }
            }
            .show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun calculateNewDates(isStartDate: Boolean, selectedDate: LocalDate): Pair<LocalDate, LocalDate> {
        val current = viewModel.datesState.value
        return if (isStartDate) {
            selectedDate to if (selectedDate > current.second) selectedDate else current.second
        } else {
            if (selectedDate < current.first) selectedDate to selectedDate else current.first to selectedDate
        }
    }

    private fun Long.toLocalDate(): LocalDate {
        return Instant.ofEpochMilli(this)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    private fun LocalDate.toEpochMilli(): Long {
        return atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun LocalDate.toBeautifulString(): String {
        val month = resources.getStringArray(R.array.months_genitive)[monthValue - 1]
        return "$dayOfMonth $month $year"
    }

    private fun validateInput(): Boolean {
        with(viewBinding) {
            return when {
                etTitleTrip.text.isNullOrBlank() -> {
                    showToast(getString(R.string.enter_the_name_of_the_trip))
                    false
                }
                etCostTrip.text.isNullOrBlank() -> {
                    showToast(getString(R.string.enter_the_cost))
                    false
                }
                else -> true
            }
        }
    }

    private fun dismissWithNavigation() {
        dismissAllowingStateLoss()
        viewModel.navigateToTrips(phoneNumber)
    }

    private fun showProgress(show: Boolean) {
        // Реализация отображения прогресса
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): BottomSheetDialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet).apply {
                    addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                        override fun onStateChanged(bottomSheet: View, newState: Int) {
                            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                                dismissWithNavigation()
                            }
                        }
                        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                    })

                    val maxHeight = (resources.displayMetrics.heightPixels * 0.75).toInt()
                    sheet.layoutParams.height = maxHeight
                    peekHeight = maxHeight
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        return dialog
    }

    companion object {
        const val TAG = "AddTripBottomSheet"
        private const val PHONE_NUMBER = "phone_number"
        private const val DATE_PICKER_TAG = "DATE_PICKER_TAG"

        fun newInstance(phoneNumber: String): AddTripBottomSheet {
            return AddTripBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(PHONE_NUMBER, phoneNumber)
                }
            }
        }
    }
}