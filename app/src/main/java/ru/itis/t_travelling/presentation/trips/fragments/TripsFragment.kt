package ru.itis.t_travelling.presentation.trips.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.FragmentTripsBinding
import ru.itis.t_travelling.presentation.base.BaseFragment
import ru.itis.t_travelling.presentation.trips.fragments.adapter.Trip
import ru.itis.t_travelling.presentation.trips.fragments.adapter.TripAdapter

class TripsFragment: BaseFragment(R.layout.fragment_trips) {
    private val viewBinding: FragmentTripsBinding by viewBinding(FragmentTripsBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val trips = listOf(
            Trip("Сочи", "23.04.25", "12.05.25", "100.000 $"),
            // Добавьте другие путешествия
        )

        val adapter = TripAdapter(trips)
        viewBinding.rvTrips.layoutManager = LinearLayoutManager(requireContext())
        viewBinding.rvTrips.adapter = adapter
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