package ru.itis.t_travelling.presentation.authregister.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import by.kirich1409.viewbindingdelegate.viewBinding
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.FragmentTravellingBinding
import ru.itis.t_travelling.presentation.base.BaseFragment

class TravellingFragment: BaseFragment(R.layout.fragment_travelling) {
    private val viewBinding: FragmentTravellingBinding by viewBinding(FragmentTravellingBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        const val TRAVELLING_TAG = "TRAVELLING_TAG"
        private const val PHONE_TEXT = "PHONE_TEXT"

        fun getInstance(param: String): TravellingFragment {
            return TravellingFragment().apply {
                arguments = bundleOf(PHONE_TEXT to param)
            }
        }
    }
}