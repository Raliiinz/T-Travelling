package ru.itis.travelling.presentation.addtrip.fragments

import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentAddTripBinding
import ru.itis.travelling.presentation.base.BaseFragment

@AndroidEntryPoint
class AddTripFragment : BaseFragment(R.layout.fragment_add_trip) {
    private val viewBinding: FragmentAddTripBinding by viewBinding(FragmentAddTripBinding::bind)

    companion object {
        const val ADD_TRIP_TAG = "ADD_TRIP_TAG"
        private const val PHONE_TEXT = "PHONE_TEXT"

        fun getInstance(param: String): AddTripFragment {
            return AddTripFragment().apply {
                arguments = bundleOf(PHONE_TEXT to param)
            }
        }
    }
}