package ru.itis.travelling.presentation.authregister.fragments

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentAuthorizationBinding
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.authregister.util.hideKeyboard
import ru.itis.travelling.presentation.authregister.util.setupPasswordToggle

@AndroidEntryPoint
class AuthorizationFragment : BaseFragment(R.layout.fragment_authorization) {
    private val viewBinding: FragmentAuthorizationBinding by viewBinding(FragmentAuthorizationBinding::bind)
    private val viewModel: AuthorizationViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()
        setupPasswordToggle()
    }

    private fun setupObservers() {
        viewModel.uiState
            .onEach { state ->
                when (state) {
                    AuthorizationViewModel.AuthorizationUiState.Loading -> showProgress()
                    AuthorizationViewModel.AuthorizationUiState.Idle -> hideProgress()
                    AuthorizationViewModel.AuthorizationUiState.Success -> hideProgress()
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
                    if (state.shouldShowError) getString(R.string.error_phone_empty) else null
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.passwordState
            .onEach { state ->
                viewBinding.textInputLayoutPassword.error =
                    if (state.shouldShowError) getString(R.string.error_password_empty) else null
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

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
            viewBinding.etPhone.doOnTextChanged { text, _, _, _ ->
                viewModel.onPhoneChanged(text.toString())
            }

            viewBinding.etPassword.doOnTextChanged { text, _, _, _ ->
                viewModel.onPasswordChanged(text.toString())
            }

            etPassword.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    viewModel.login()
                    return@setOnEditorActionListener true
                }
                false
            }
            tvRegisterLink.setOnClickListener {
                hideKeyboard()
                viewModel.navigateToRegistration()
            }
            btnLogin.setOnClickListener {
                viewModel.login()
            }
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