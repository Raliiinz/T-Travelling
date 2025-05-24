package ru.itis.travelling.presentation.authregister.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentRegistrationBinding
import ru.itis.travelling.presentation.common.state.ErrorEvent
import ru.itis.travelling.presentation.authregister.state.RegistrationUiState
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.authregister.util.hideKeyboard
import ru.itis.travelling.presentation.authregister.util.setupPasswordToggle


@AndroidEntryPoint
class RegistrationFragment : BaseFragment(R.layout.fragment_registration) {
    private val viewBinding: FragmentRegistrationBinding by viewBinding(FragmentRegistrationBinding::bind)
    private val viewModel: RegistrationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPasswordToggle()
        setupListeners()
        setupObservers()
    }

    private fun setupPasswordToggle() {
        viewBinding.textInputLayoutPassword.setupPasswordToggle(viewBinding.etPassword)
        viewBinding.textInputLayoutPasswordRepeat.setupPasswordToggle(viewBinding.etPasswordRepeat)
    }

    private fun setupListeners() {
        viewBinding.etFirstName.doOnTextChanged { text, _, _, _ ->
            viewModel.onFirstNameChanged(text.toString())
        }

        viewBinding.etLastName.doOnTextChanged { text, _, _, _ ->
            viewModel.onLastNameChanged(text.toString())
        }

        viewBinding.etPhone.doOnTextChanged { text, _, _, _ ->
            viewModel.onPhoneChanged(text.toString())
        }

        viewBinding.etPassword.doOnTextChanged { text, _, _, _ ->
            viewModel.onPasswordChanged(text.toString())
        }

        viewBinding.etPasswordRepeat.doOnTextChanged { text, _, _, _ ->
            viewModel.onConfirmPasswordChanged(text.toString())
        }

        viewBinding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.register()
                return@setOnEditorActionListener true
            }
            false
        }

        viewBinding.ivBackIcon.setOnClickListener {
            hideKeyboard()
            viewModel.navigateToAuthorization()
        }

        viewBinding.btnLogin.setOnClickListener {
            viewModel.register()
        }
    }

    private fun setupObservers() {
        viewModel.uiState
            .onEach { state ->
                when (state) {
                    RegistrationUiState.Loading -> showProgress()
                    RegistrationUiState.Idle -> hideProgress()
                    RegistrationUiState.Success -> hideProgress()

                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.firstNameState
            .onEach { state ->
                viewBinding.textInputLayoutFirstName.error = when {
                    state.value.isBlank() && state.shouldShowError -> getString(R.string.error_first_name_empty)
                    !state.isValid && state.shouldShowError -> getString(R.string.error_first_name_invalid)
                    else -> null
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.lastNameState
            .onEach { state ->
                viewBinding.textInputLayoutLastName.error = when {
                    state.value.isBlank() && state.shouldShowError -> getString(R.string.error_last_name_empty)
                    !state.isValid && state.shouldShowError -> getString(R.string.error_last_name_invalid)
                    else -> null
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.phoneState
            .onEach { state ->
                if (viewBinding.etPhone.text.toString() != state.value) {
                    viewBinding.etPhone.setText(state.value)
                    viewBinding.etPhone.setSelection(state.value.length)
                }
                viewBinding.textInputLayoutPhone.error =
                    if (state.shouldShowError) getString(R.string.error_invalid_phone) else null
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.passwordState
            .onEach { state ->
                viewBinding.textInputLayoutPassword.error =
                    if (state.shouldShowError) getString(R.string.error_invalid_password) else null
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.confirmPasswordState
            .onEach { state ->
                viewBinding.textInputLayoutPasswordRepeat.error =
                    if (state.shouldShowError) getString(R.string.error_password_mismatch) else null
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.errorEvent
            .onEach { event ->
                when (event) {
                    is ErrorEvent.FullError -> {
                        showErrorDialog(event.titleRes, event.messageRes)
                    }
                    else -> {}
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showErrorDialog(@StringRes titleRes: Int, @StringRes messageRes: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    companion object {
        const val REGISTRATION_TAG = "REGISTRATION_TAG"
    }
}