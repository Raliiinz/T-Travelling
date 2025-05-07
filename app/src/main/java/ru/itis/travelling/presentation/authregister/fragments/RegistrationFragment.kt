package ru.itis.travelling.presentation.authregister.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentRegistrationBinding
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.authregister.util.hideKeyboard
import ru.itis.travelling.presentation.authregister.util.setupPasswordToggle
import ru.itis.travelling.presentation.authregister.util.setupValidation
import ru.itis.travelling.presentation.authregister.util.ValidationUtils
import ru.itis.travelling.presentation.utils.PhoneNumberUtils


@AndroidEntryPoint
class RegistrationFragment : BaseFragment(R.layout.fragment_registration) {
    private val viewBinding: FragmentRegistrationBinding by viewBinding(FragmentRegistrationBinding::bind)
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPhoneNumberInput()
        setupPasswordToggle()
        setupRealTimeValidation()
        setupListeners()
        setupObservers()
    }

    private var isFormatting = false

    private fun setupPhoneNumberInput() {
        viewBinding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                s?.let {
                    isFormatting = true

                    val input = s.toString()
                    val formatted = PhoneNumberUtils.formatPhoneNumber(input)

                    if (formatted != input) {
                        viewBinding.etPhone.setText(formatted)
                        viewBinding.etPhone.setSelection(formatted.length)
                    }

                    isFormatting = false
                }
            }
        })
    }

    private fun setupPasswordToggle() {
        viewBinding.textInputLayoutPassword.setupPasswordToggle(viewBinding.etPassword)
        viewBinding.textInputLayoutPasswordRepeat.setupPasswordToggle(viewBinding.etPasswordRepeat)
    }

    private fun setupRealTimeValidation() {
        viewBinding.etPhone.setupValidation(
            ValidationUtils::isValidPhone,
            viewBinding.textInputLayoutPhone,
            R.string.error_invalid_phone
        )

        viewBinding.etPassword.setupValidation(
            ValidationUtils::isValidPassword,
            viewBinding.textInputLayoutPassword,
            R.string.error_invalid_password
        )

        viewBinding.etPasswordRepeat.doOnTextChanged { text, _, _, _ ->
            val password = viewBinding.etPassword.text.toString()
            if (text.toString() != password) {
                viewBinding.textInputLayoutPasswordRepeat.error = getString(R.string.error_password_mismatch)
            } else {
                viewBinding.textInputLayoutPasswordRepeat.error = null
            }
        }
    }

    private fun setupListeners() {
        viewBinding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister()
                return@setOnEditorActionListener true
            }
            false
        }

        viewBinding.ivBackIcon.setOnClickListener {
            hideKeyboard()
            viewModel.navigateToAuthorization()
        }

        viewBinding.btnLogin.setOnClickListener {
            attemptRegister()
        }
    }

    private fun attemptRegister() {
        val phone = PhoneNumberUtils.normalizePhoneNumber(viewBinding.etPhone.text.toString())
        val password = viewBinding.etPassword.text.toString().trim()
        val confirmPassword = viewBinding.etPasswordRepeat.text.toString().trim()

        val isPhoneValid = validateField(
            phone,
            ValidationUtils::isValidPhone,
            viewBinding.textInputLayoutPhone,
            R.string.error_invalid_phone
        )

        val isPasswordValid = validateField(
            password,
            ValidationUtils::isValidPassword,
            viewBinding.textInputLayoutPassword,
            R.string.error_invalid_password
        )

        val isPasswordMatch = if (password != confirmPassword) {
            viewBinding.textInputLayoutPasswordRepeat.error = getString(R.string.error_password_mismatch)
            false
        } else {
            viewBinding.textInputLayoutPasswordRepeat.error = null
            true
        }

        if (isPhoneValid && isPasswordValid && isPasswordMatch) {
            viewModel.register(phone, password)
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
        viewModel.uiState
            .onEach { state ->
                when (state) {
                    RegistrationViewModel.RegistrationUiState.Loading -> showProgress()
                    RegistrationViewModel.RegistrationUiState.Idle -> hideProgress()
                    RegistrationViewModel.RegistrationUiState.Success -> hideProgress()
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.events
            .onEach { event ->
                when (event) {
                    is RegistrationViewModel.RegistrationEvent.ShowError -> {
                        handleErrors(event.error)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleErrors(error: RegistrationViewModel.RegistrationError) {
        val message = when (error) {
            RegistrationViewModel.RegistrationError.UserAlreadyExists -> R.string.error_user_already_exists
            RegistrationViewModel.RegistrationError.Unknown -> R.string.error_unknown
        }
        showToast(getString(message))
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    companion object {
        const val REGISTRATION_TAG = "REGISTRATION_TAG"
    }
}
