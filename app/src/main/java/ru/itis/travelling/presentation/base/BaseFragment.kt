package ru.itis.travelling.presentation.base

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import ru.itis.travelling.R

abstract class BaseFragment(layoutId: Int) : Fragment(layoutId) {
    private var progressDialog: Dialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWindowInsets()
        initProgressDialog()
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

    private fun initProgressDialog() {
        progressDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.progress_dialog)
            setCancelable(false)
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    protected open fun showProgress() {
        progressDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
    }

    protected open fun hideProgress() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog?.dismiss()
        progressDialog = null
    }
}
