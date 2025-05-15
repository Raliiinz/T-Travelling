package ru.itis.travelling.presentation.trips.fragments.overview

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentTripsBinding
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.trips.list.TripAdapter

@AndroidEntryPoint
class TripsFragment: BaseFragment(R.layout.fragment_trips) {
    private val viewBinding: FragmentTripsBinding by viewBinding(FragmentTripsBinding::bind)
    private val viewModel: TripsViewModel by viewModels()
    private var rvAdapter: TripAdapter? = null

    private val phoneNumber: String by lazy {
        arguments?.getString(PHONE_TEXT) ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
        viewModel.loadTrips(phoneNumber)
    }

    private fun setupRecyclerView() {
        rvAdapter = TripAdapter { trip ->
            viewModel.onTripClicked(trip.id, phoneNumber)
        }

        viewBinding.rvTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tripsState.collect { state ->
                when (state) {
                    is TripsViewModel.TripsState.Loading -> showShimmer()
                    is TripsViewModel.TripsState.Idle -> hideShimmer()
                    is TripsViewModel.TripsState.Success -> {
                        hideShimmer()
                        rvAdapter?.submitList(state.trips)
                    }
                }
            }
            viewModel.events
                .onEach { event ->
                    when (event) {
                        is TripsViewModel.TripsEvent.Error -> {
                            showError(event.message)
                        }
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)
        }
    }

    private fun showShimmer() {
        viewBinding.rvTrips.visibility = View.GONE
        viewBinding.shimmerContainer.visibility = View.VISIBLE
        viewBinding.shimmerContainer.startShimmer()
    }

    private fun hideShimmer() {
        viewBinding.shimmerContainer.stopShimmer()
        viewBinding.shimmerContainer.visibility = View.GONE
        viewBinding.rvTrips.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TRIPS_TAG = "TRIPS_TAG"
        private const val PHONE_TEXT = "PHONE_TEXT"

        fun getInstance(param: String): TripsFragment {
            return TripsFragment().apply {
                arguments = bundleOf(PHONE_TEXT to param)
            }
        }
    }
}
