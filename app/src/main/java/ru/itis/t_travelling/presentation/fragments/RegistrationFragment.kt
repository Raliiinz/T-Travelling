package ru.itis.t_travelling.presentation.fragments

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.t_travelling.MainActivity
import ru.itis.t_travelling.R
import ru.itis.t_travelling.presentation.base.NavigationAction
import ru.itis.t_travelling.databinding.FragmentRegistrationBinding
import ru.itis.t_travelling.presentation.viewmodels.RegistrationViewModel
import ru.itis.t_travelling.util.ValidationUtils


@AndroidEntryPoint
class RegistrationFragment : Fragment(R.layout.fragment_registration) {
    private var viewBinding: FragmentRegistrationBinding? = null
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding = FragmentRegistrationBinding.bind(view)

        setupListeners()
        setupObservers()
        setupRealTimeValidation()
        setupPasswordToggle()
    }

    private fun setupPasswordToggle() {
        fun setupToggleForField(
            editText: TextInputEditText?,
            textInputLayout: TextInputLayout?
        ) {
            textInputLayout?.setEndIconOnClickListener {
                val isPasswordVisible = editText?.transformationMethod is HideReturnsTransformationMethod
                editText?.transformationMethod = if (isPasswordVisible) {
                    PasswordTransformationMethod.getInstance()
                } else {
                    HideReturnsTransformationMethod.getInstance()
                }
                textInputLayout.endIconDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    if (isPasswordVisible) R.drawable.ic_eye_closed else R.drawable.ic_eye_open
                )
                editText?.setSelection(editText.text?.length ?: 0)
            }
        }

        setupToggleForField(viewBinding?.etPassword, viewBinding?.textInputLayoutPassword)
        setupToggleForField(viewBinding?.etPasswordRepeat, viewBinding?.textInputLayoutPasswordRepeat)
    }

    private fun setupListeners() {
        listOf(
            viewBinding?.ivBackIcon,
            viewBinding?.tvBackText
        ).forEach { view ->
            view?.setOnClickListener { navigateBackToAuthorization() }
        }

        viewBinding?.btnLogin?.setOnClickListener {
            val phone = viewBinding?.etPhone?.text.toString().trim()
            val password = viewBinding?.etPassword?.text.toString().trim()
            val confirmPassword = viewBinding?.etPasswordRepeat?.text.toString().trim()

            val isPhoneValid = validateField(
                phone,
                ValidationUtils::isValidPhone,
                viewBinding?.textInputLayoutPhone,
                R.string.error_invalid_phone
            )

            val isPasswordValid = validateField(
                password,
                ValidationUtils::isValidPassword,
                viewBinding?.textInputLayoutPassword,
                R.string.error_invalid_password
            )

            val isPasswordMatch = if (password != confirmPassword) {
                viewBinding?.textInputLayoutPasswordRepeat?.error = getString(R.string.error_password_mismatch)
                false
            } else {
                viewBinding?.textInputLayoutPasswordRepeat?.error = null
                true
            }

            if (isPhoneValid && isPasswordValid && isPasswordMatch) {
                viewModel.register(phone, password, confirmPassword)
            }
        }
    }

    private fun validateField(
        value: String,
        validator: (String) -> Boolean,
        textInputLayout: TextInputLayout?,
        errorResId: Int
    ): Boolean {
        return if (!validator(value)) {
            textInputLayout?.error = getString(errorResId)
            false
        } else {
            textInputLayout?.error = null
            true
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.registrationState.collect { state ->
                when (state) {
                    is RegistrationViewModel.RegistrationState.Success -> {
                        navigateBackToAuthorization()
                    }
                    is RegistrationViewModel.RegistrationState.Error -> {
                        handleErrors(state.error)
                    }
                    RegistrationViewModel.RegistrationState.Idle -> {
                        // Do nothing
                    }
                }
            }
        }
    }


    private fun setupRealTimeValidation() {
        viewBinding?.etPhone?.setupValidation(
            ValidationUtils::isValidPhone,
            viewBinding?.textInputLayoutPhone,
            R.string.error_invalid_phone
        )

        viewBinding?.etPassword?.setupValidation(
            ValidationUtils::isValidPassword,
            viewBinding?.textInputLayoutPassword,
            R.string.error_invalid_password
        )

        viewBinding?.etPasswordRepeat?.doOnTextChanged { text, _, _, _ ->
            val password = viewBinding?.etPassword?.text.toString()
            if (text.toString() != password) {
                viewBinding?.textInputLayoutPasswordRepeat?.error = getString(R.string.error_password_mismatch)
            } else {
                viewBinding?.textInputLayoutPasswordRepeat?.error = null
            }
        }
    }

    private fun TextInputEditText.setupValidation(
        validator: (String) -> Boolean,
        textInputLayout: TextInputLayout?,
        errorResId: Int
    ) {
        doOnTextChanged { text, _, _, _ ->
            if (!validator(text.toString())) {
                textInputLayout?.error = getString(errorResId)
            } else {
                textInputLayout?.error = null
            }
        }
    }

    private fun handleErrors(error: RegistrationViewModel.RegistrationError) {
        val message = when (error) {
            RegistrationViewModel.RegistrationError.UserAlreadyExists -> R.string.error_user_already_exists
            RegistrationViewModel.RegistrationError.Unknown -> R.string.error_unknown
        }
        showToast(getString(message))
    }


    private fun navigateBackToAuthorization() {
        (requireActivity() as? MainActivity)?.navigate(
            destination = AuthorizationFragment(),
            destinationTag = AuthorizationFragment.AUTHORIZATION_TAG,
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
        const val REGISTRATION_TAG = "REGISTRATION_TAG"
    }
}