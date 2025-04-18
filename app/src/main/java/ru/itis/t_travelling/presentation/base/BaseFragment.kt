package ru.itis.t_travelling.presentation.base

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment

abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
    }

    protected fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.updatePadding(
                bottom = systemBars.bottom + if (ime.bottom > 0) ime.bottom else 0
            )

            insets
        }
    }
}