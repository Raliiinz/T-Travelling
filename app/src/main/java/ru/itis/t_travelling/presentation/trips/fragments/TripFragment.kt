package ru.itis.t_travelling.presentation.trips.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.FragmentTripBinding
import ru.itis.t_travelling.presentation.base.BaseFragment
import kotlin.getValue

@AndroidEntryPoint
class TripFragment: BaseFragment(R.layout.fragment_trips) {
    private val viewBinding: FragmentTripBinding by viewBinding(FragmentTripBinding::bind)
    private val viewModel: TripsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        const val TRIP_TAG = "TRIP_TAG"
        private const val TRIP_ID = "TRIP_ID"

        fun getInstance(param: String): TripFragment {
            return TripFragment().apply {
                arguments = bundleOf(TRIP_ID to param)
            }
        }
    }
}