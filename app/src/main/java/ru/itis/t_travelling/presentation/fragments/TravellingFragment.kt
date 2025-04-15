package ru.itis.t_travelling.presentation.fragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.FragmentTravellingBinding

class TravellingFragment: Fragment(R.layout.fragment_travelling) {
    private var viewBinding: FragmentTravellingBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = FragmentTravellingBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
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