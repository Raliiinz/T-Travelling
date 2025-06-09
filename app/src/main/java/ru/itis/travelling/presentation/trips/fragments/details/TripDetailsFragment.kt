package ru.itis.travelling.presentation.trips.fragments.details

import android.app.AlertDialog
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentTripDetailsBinding
import ru.itis.travelling.domain.trips.model.TripDetails
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.trips.fragments.details.state.InvitationUiState
import ru.itis.travelling.presentation.trips.fragments.details.state.TripDetailsEvent
import ru.itis.travelling.presentation.trips.fragments.details.state.TripDetailsState
import ru.itis.travelling.presentation.trips.list.ParticipantAdapter
import ru.itis.travelling.presentation.trips.util.DateUtils
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

    private var isInvitation: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapter()
        observeViewModel()
        setupListeners()

        isInvitation = arguments?.getBoolean("IS_INVITATION", false) ?: false

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
                    is TripDetailsState.Loading -> showProgress()
                    is TripDetailsState.Success -> {
                        hideProgress()
                        updateUi(state.trip)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is TripDetailsEvent.Error -> {
                        hideProgress()
                        showError(event.message)
                    }
                    is TripDetailsEvent.ShowAdminAlert -> {
                        hideProgress()
                        showAdminAlert()
                    }
                    is TripDetailsEvent.ShowLeaveConfirmation -> {
                        hideProgress()
                        showLeaveConfirmationDialog()
                    }
                    is TripDetailsEvent.ShowDeleteConfirmation -> {
                        hideProgress()
                        showDeleteConfirmationDialog()
                    }
                    is TripDetailsEvent.NavigateToTrips -> {
                        viewModel.navigateToTrips(phoneNumber)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is InvitationUiState.Idle -> {}
                    is InvitationUiState.Loading -> {
                        showProgress()
                    }

                    is InvitationUiState.Success -> {
                        hideProgress()
                        showToast(state.message)
                        if (state.shouldHideActions) {
                            viewBinding.llInvitationActions.visibility = View.GONE
                        }
                    }

                    is InvitationUiState.Error -> {
                        hideProgress()
                        val message = if (state.message is Int) {
                            getString(state.message)
                        } else {
                            state.message as String
                        }
                        showError(message)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvent.collect { event ->
                when (event) {
                    is ErrorEvent.MessageOnly -> showToast(event.messageRes)
                    else -> {}
                }
            }
        }
    }

    private fun setupListeners() {
        viewBinding.ivBackIcon.setOnClickListener {
            viewModel.navigateToTrips(phoneNumber)
        }

        viewBinding.ivEditIcon.setOnClickListener {
            viewModel.onEditClicked(phoneNumber)
        }

        viewBinding.ivLeaveIcon.setOnClickListener {
            viewModel.onLeaveOrDeleteClicked(phoneNumber)
        }

        viewBinding.btnTransactions.setOnClickListener {
            viewModel.onTransactionsClicked(phoneNumber)
        }

        viewBinding.btnAcceptInvitation.setOnClickListener {
            viewModel.acceptInvitation(tripId)

            viewBinding.llInvitationActions.visibility = View.GONE
            viewBinding.dimView.visibility = View.GONE
            removeBlurEffect(viewBinding.contentLayout)
        }

        viewBinding.btnDeclineInvitation.setOnClickListener {
            viewModel.declineInvitation(tripId, phoneNumber)
        }
    }

    private fun updateUi(trip: TripDetails) {
        with(viewBinding) {
            tvDestination.text = trip.destination
            val startDate = DateUtils.formatDateForDisplay(trip.startDate)
            val endDate = DateUtils.formatDateForDisplay(trip.endDate)
            tvDates.text = getString(R.string.trip_dates, startDate, endDate)
            tvPrice.text = getString(R.string.price, trip.price)
            participantAdapter.submitList(trip.participants)

            if (isInvitation) {
                llInvitationActions.visibility = View.VISIBLE
                dimView.visibility = View.VISIBLE
                applyBlurEffect(contentLayout)
            } else {
                llInvitationActions.visibility = View.GONE
                dimView.visibility = View.GONE
                removeBlurEffect(contentLayout)
            }
            llInvitationActions.visibility = if (isInvitation) View.VISIBLE else View.GONE
        }
    }

    private fun applyBlurEffect(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val renderEffect = RenderEffect.createBlurEffect(
                20f, 20f, Shader.TileMode.CLAMP
            )
            view.setRenderEffect(renderEffect)
        }
    }

    private fun removeBlurEffect(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null)
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
                viewModel.confirmLeaveTrip()
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
                viewModel.confirmDeleteTrip()
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

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    companion object {
        const val TRIP_TAG = "TRIP_TAG"
        private const val TRIP_ID = "TRIP_ID"
        private const val PHONE_TEXT = "PHONE_TEXT"

        fun getInstance(tripId: String, phone: String, isInvitation: Boolean = false): TripDetailsFragment {
            return TripDetailsFragment().apply {
                arguments = bundleOf(TRIP_ID to tripId, PHONE_TEXT to phone, "IS_INVITATION" to isInvitation)
            }
        }
    }
}
