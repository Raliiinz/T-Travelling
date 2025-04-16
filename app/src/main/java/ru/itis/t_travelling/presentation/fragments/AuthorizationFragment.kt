package ru.itis.t_travelling.presentation.fragments

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.t_travelling.MainActivity
import ru.itis.t_travelling.R
import ru.itis.t_travelling.databinding.FragmentAuthorizationBinding
import ru.itis.t_travelling.presentation.base.NavigationAction
import ru.itis.t_travelling.presentation.util.hideKeyboard
import ru.itis.t_travelling.presentation.util.setupValidationOfNull
import ru.itis.t_travelling.presentation.viewmodels.AuthorizationViewModel

@AndroidEntryPoint
class AuthorizationFragment : Fragment(R.layout.fragment_authorization) {
    private var viewBinding: FragmentAuthorizationBinding? = null
    private val viewModel: AuthorizationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = FragmentAuthorizationBinding.bind(view)

        setupWindowInsets()
        setupObservers()
        setupListeners()
        setupRealTimeValidation()
        setupPasswordToggle()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.updatePadding(
                bottom = systemBars.bottom + if (ime.bottom > 0) ime.bottom else 0
            )

            insets
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is AuthorizationViewModel.LoginState.Success -> {
                        navigateToTravelling(state.phone)
                    }
                    is AuthorizationViewModel.LoginState.Error -> {
                        showToast(state.message)
                    }
                    AuthorizationViewModel.LoginState.Idle -> {
                        // Do nothing
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        viewBinding?.etPassword?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                return@setOnEditorActionListener true
            }
            false
        }

        viewBinding?.tvRegisterLink?.setOnClickListener {
            navigateToRegistration()
        }

        viewBinding?.btnLogin?.setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {
        val phone = viewBinding?.etPhone?.text.toString().trim()
        val password = viewBinding?.etPassword?.text.toString().trim()

        val isPhoneValid = validateField(
            value = phone,
            errorMessage = getString(R.string.error_phone_empty),
            errorTarget = viewBinding?.textInputLayoutPhone
        )

        val isPasswordValid = validateField(
            value = password,
            errorMessage = getString(R.string.error_password_empty),
            errorTarget = viewBinding?.textInputLayoutPassword
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
        viewBinding?.apply {
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
        viewBinding?.textInputLayoutPassword?.setEndIconOnClickListener {
            val isPasswordVisible =
                viewBinding?.etPassword?.transformationMethod is HideReturnsTransformationMethod
            if (isPasswordVisible) {
                viewBinding?.etPassword?.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                viewBinding?.textInputLayoutPassword?.endIconDrawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_eye_closed)
            } else {
                viewBinding?.etPassword?.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                viewBinding?.textInputLayoutPassword?.endIconDrawable =
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_eye_open)
            }
            viewBinding?.etPassword?.setSelection(viewBinding?.etPassword?.text?.length ?: 0)
        }
    }

    private fun navigateToTravelling(phone: String) {
        hideKeyboard()
        (requireActivity() as? MainActivity)?.apply {
            setupBottomNavigation()
            showBottomNavigation()
            navigate(
                destination = TravellingFragment.getInstance(param = phone),
                destinationTag = TravellingFragment.TRAVELLING_TAG,
                action = NavigationAction.REPLACE,
                isAddToBackStack = false
            )
        }
    }

    private fun navigateToRegistration() {
        hideKeyboard()
        (requireActivity() as? MainActivity)?.navigate(
            destination = RegistrationFragment(),
            destinationTag = RegistrationFragment.REGISTRATION_TAG,
            action = NavigationAction.REPLACE,
            isAddToBackStack = true
        )
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewBinding = null
    }

    companion object {
        const val AUTHORIZATION_TAG = "AUTHORIZATION_TAG"
    }
}