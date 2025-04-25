package ru.itis.t_travelling.presentation.trips.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.FragmentTripDetailsBinding
import ru.itis.t_travelling.domain.trips.model.Participant
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.presentation.base.BaseFragment
import ru.itis.t_travelling.presentation.trips.list.ParticipantAdapter
import ru.itis.t_travelling.presentation.trips.util.FormatUtils
import kotlin.getValue

@AndroidEntryPoint
class TripDetailsFragment: BaseFragment(R.layout.fragment_trip_details) {
    private val viewBinding: FragmentTripDetailsBinding by viewBinding(FragmentTripDetailsBinding::bind)
    private val viewModel: TripDetailsViewModel by viewModels()
    private lateinit var participantAdapter: ParticipantAdapter
    private val phoneNumber: String by lazy {
        arguments?.getString(PHONE_TEXT) ?: ""
    }
    private val tripId : String by lazy {
        arguments?.getString(TRIP_ID) ?: ""
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeViewModel()
        setupListeners()

        tripId.let {
            viewModel.loadTripDetails(it)
        }
    }

    private fun setupAdapter() {
        participantAdapter = ParticipantAdapter()
        viewBinding.rvParticipants.adapter = participantAdapter
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tripState.collect { state ->
                when (state) {
                    is TripDetailsViewModel.TripDetailsState.Loading -> showProgress()
                    is TripDetailsViewModel.TripDetailsState.Success -> {
                        hideProgress()
                        updateUi(state.trip)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is TripDetailsViewModel.TripDetailsEvent.Error -> {
                        hideProgress()
                        showError(event.message)
                    }
                    is TripDetailsViewModel.TripDetailsEvent.ShowAdminAlert -> {
                        hideProgress()
                        showAdminAlert()
                    }
                    is TripDetailsViewModel.TripDetailsEvent.ShowLeaveConfirmation -> {
                        hideProgress()
                        showLeaveConfirmationDialog()
                    }
                    is TripDetailsViewModel.TripDetailsEvent.ShowDeleteConfirmation -> {
                        hideProgress()
                        showDeleteConfirmationDialog()
                    }
                    is TripDetailsViewModel.TripDetailsEvent.NavigateToTrips -> {
                        viewModel.navigateToTrips(phoneNumber)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        viewBinding.ivBackIcon.setOnClickListener {
            viewModel.navigateToTrips(phoneNumber)
        }

        viewBinding.ivEditIcon.setOnClickListener {
            (viewModel.tripState.value as? TripDetailsViewModel.TripDetailsState.Success)?.trip?.let { trip ->
                viewModel.onEditClicked(phoneNumber, trip)
            }
        }

        viewBinding.ivLeaveIcon.setOnClickListener {
            (viewModel.tripState.value as? TripDetailsViewModel.TripDetailsState.Success)?.trip?.let { trip ->
                viewModel.onLeaveOrDeleteClicked(phoneNumber, trip)
            }
        }
    }

    private fun updateUi(trip: Trip) {
        with(viewBinding) {
            tvDestination.text = trip.destination
            tvDates.text = requireContext().getString(R.string.trip_dates, trip.startDate, trip.endDate)
            tvPrice.text = getString(R.string.price, FormatUtils.formatPriceWithThousands(trip.price))

            val allParticipants = mutableListOf<Participant>()
            allParticipants.add(trip.admin)
            allParticipants.addAll(trip.participants.filter { p -> p.id != trip.admin.id })

            participantAdapter.submitList(allParticipants)
        }
    }

    private fun showAdminAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.admin_required))
            .setMessage(getString(R.string.only_admin_can_edit))
            .setPositiveButton(getString(R.string.close)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showLeaveConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.leave_trip_title))
            .setMessage(getString(R.string.leave_trip_message))
            .setPositiveButton(getString(R.string.leave)) { dialog, _ ->
                (viewModel.tripState.value as? TripDetailsViewModel.TripDetailsState.Success)?.trip?.let { trip ->
                    viewModel.confirmLeaveTrip(trip.id, phoneNumber)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_trip_title))
            .setMessage(getString(R.string.delete_trip_message))
            .setPositiveButton(getString(R.string.delete)) { dialog, _ ->
                (viewModel.tripState.value as? TripDetailsViewModel.TripDetailsState.Success)?.trip?.let { trip ->
                    viewModel.confirmDeleteTrip(trip.id)
                }
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun showProgress() {
        with(viewBinding) {
            shimmerLayout.startShimmer()
            shimmerLayout.visibility = View.VISIBLE
            mainContentGroup.visibility = View.GONE
        }
    }

    override fun hideProgress() {
        with(viewBinding) {
            shimmerLayout.stopShimmer()
            shimmerLayout.visibility = View.GONE
            mainContentGroup.visibility = View.VISIBLE
        }
    }


    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TRIP_TAG = "TRIP_TAG"
        private const val TRIP_ID = "TRIP_ID"
        private const val PHONE_TEXT = "PHONE_TEXT"

        fun getInstance(tripId: String, phone: String): TripDetailsFragment {
            return TripDetailsFragment().apply {
                arguments = bundleOf(TRIP_ID to tripId, PHONE_TEXT to phone)
            }
        }
    }
}