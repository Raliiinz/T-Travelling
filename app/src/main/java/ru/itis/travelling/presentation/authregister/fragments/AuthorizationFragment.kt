package ru.itis.travelling.presentation.authregister.fragments

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentAuthorizationBinding
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.authregister.util.hideKeyboard
import ru.itis.travelling.presentation.authregister.util.setupPasswordToggle
import ru.itis.travelling.presentation.authregister.util.setupValidationOfNull

@AndroidEntryPoint
class AuthorizationFragment : BaseFragment(R.layout.fragment_authorization) {
    private val viewBinding: FragmentAuthorizationBinding by viewBinding(FragmentAuthorizationBinding::bind)
    private val viewModel: AuthorizationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
        setupRealTimeValidation()
        setupPasswordToggle()
    }

    private fun setupObservers() {
        viewModel.events
            .onEach { event ->
                when (event) {
                    is AuthorizationViewModel.AuthorizationEvent.ShowError -> {
                        showToast(event.message)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupListeners() {
        with(viewBinding) {
            etPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin()
                    return@setOnEditorActionListener true
                }
                false
            }
            tvRegisterLink.setOnClickListener {
                hideKeyboard()
                viewModel.navigateToRegistration()
            }
            btnLogin.setOnClickListener {
                attemptLogin()
            }
        }
    }

    private fun attemptLogin() {
        val phone = viewBinding.etPhone.text.toString().trim()
        val password = viewBinding.etPassword.text.toString().trim()

        val isPhoneValid = validateField(
            value = phone,
            errorMessage = getString(R.string.error_phone_empty),
            errorTarget = viewBinding.textInputLayoutPhone
        )

        val isPasswordValid = validateField(
            value = password,
            errorMessage = getString(R.string.error_password_empty),
            errorTarget = viewBinding.textInputLayoutPassword
        )

        if (isPhoneValid && isPasswordValid) {
            viewModel.login(phone, password)
        }
    }

    private fun validateField(
        value: String,
        errorMessage: String,
        errorTarget: TextInputLayout?
    ): Boolean {
        return if (value.isEmpty()) {
            errorTarget?.error = errorMessage
            false
        } else {
            errorTarget?.error = null
            true
        }
    }

    private fun setupRealTimeValidation() {
        viewBinding.apply {
            etPhone.setupValidationOfNull(
                errorMessage = getString(R.string.error_phone_empty),
                errorTarget = textInputLayoutPhone
            )

            etPassword.setupValidationOfNull(
                errorMessage = getString(R.string.error_password_empty),
                errorTarget = textInputLayoutPassword
            )
        }
    }

    private fun setupPasswordToggle() {
        viewBinding.textInputLayoutPassword.setupPasswordToggle()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val AUTHORIZATION_TAG = "AUTHORIZATION_TAG"
    }
}
