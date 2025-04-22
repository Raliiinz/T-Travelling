package ru.itis.t_travelling.presentation.trips.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.FragmentTripsBinding
import ru.itis.t_travelling.domain.trips.model.Trip
import ru.itis.t_travelling.presentation.base.BaseFragment
import ru.itis.t_travelling.presentation.trips.list.TripAdapter

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
        rvAdapter = TripAdapter()
        viewBinding.rvTrips.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = rvAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tripsState.collect { state ->
                when (state) {
                    is TripsViewModel.TripsState.Loading -> showProgress()
                    is TripsViewModel.TripsState.Success -> {
                        hideProgress()
                        rvAdapter?.submitList(
                            listOf(
                                Trip(
                                    id = "1",
                                    destination = "Sochi",
                                    startDate = "12.12.2025",
                                    endDate = "24.12.2025",
                                    price = 150000,
                                    userId = "1"
                                )
                            )
                        )
//                        TODO
//                        rvAdapter?.submitList(state.trips)

                    }
                    is TripsViewModel.TripsState.Error -> {
                        hideProgress()
                        showError(state.message)
                    }
                }
            }
        }
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