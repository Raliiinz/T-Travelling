package ru.itis.t_travelling.presentation.trips.fragments

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
                    is TripDetailsViewModel.TripDetailsState.Error -> {
                        hideProgress()
                        showError(state.message)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        viewBinding.ivBackIcon.setOnClickListener {
            viewModel.navigateToTrips(phoneNumber)
        }
    }

    private fun updateUi(trip: Trip) {
        with(viewBinding) {
            tvDestination.text = trip.destination
            tvDates.text = "${trip.startDate} - ${trip.endDate}"
            tvPrice.text = getString(R.string.price, FormatUtils.formatPriceWithThousands(trip.price))

            val allParticipants = mutableListOf<Participant?>()
            trip.admin?.let { admin ->
                allParticipants.add(admin)
                allParticipants.addAll(trip.participants.filter { p -> p?.id != admin.id })
            } ?: run {
                allParticipants.addAll(trip.participants)
            }

            participantAdapter.submitList(allParticipants)
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