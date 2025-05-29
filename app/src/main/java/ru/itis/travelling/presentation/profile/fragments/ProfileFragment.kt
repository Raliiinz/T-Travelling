package ru.itis.travelling.presentation.profile.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.itis.travelling.R
import ru.itis.travelling.databinding.FragmentProfileBinding
import ru.itis.travelling.domain.profile.model.Participant
import ru.itis.travelling.presentation.MainActivity
import ru.itis.travelling.presentation.base.BaseFragment
import ru.itis.travelling.presentation.common.state.ErrorEvent

@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile) {
    private val viewModel: ProfileViewModel by viewModels()
    private val viewBinding: FragmentProfileBinding by viewBinding(FragmentProfileBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
        viewModel.loadProfile()
    }

    private fun setupListeners() {
        viewBinding.logoutItem.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        viewBinding.tvLanguageItem.setOnClickListener {
            showLanguageSelectionDialog()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.profileState.collect { state ->
                when (state) {
                    ProfileViewModel.ProfileState.Loading -> showProgress()
                    is ProfileViewModel.ProfileState.Success -> {
                        hideProgress()
                        updateUi(state.participant)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorEvent.collect { event ->
                when (event) {
                    is ErrorEvent.MessageOnly -> showToast(event.messageRes)
                    else -> {}
                }
            }
        }
    }

    private fun updateUi(participant: Participant) {
        with(viewBinding) {
            tvUserFirstName.text = participant.firstName
            tvUserLastName.text = participant.lastName
            tvUserPhone.text = participant.phone
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_confirmation))
            .setPositiveButton(getString(R.string.logout)) { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.language_russian),
            getString(R.string.language_english)
        )

        val currentLanguage = viewModel.getCurrentLanguage()
        val checkedItem = when (currentLanguage) {
            LANGUAGE_RUSSIAN -> 0
            else -> 1
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.change_language_title))
            .setSingleChoiceItems(languages, checkedItem) { dialog, which ->
                val language = when (which) {
                    0 -> LANGUAGE_RUSSIAN
                    else -> LANGUAGE_ENGLISH
                }
                viewModel.changeLanguage(language)
                (activity as? MainActivity)?.changeLanguage()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showToast(@StringRes messageRes: Int) {
        Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_LONG).show()
    }

    companion object {
        const val PROFILE_TAG = "PROFILE_TAG"
        private const val LANGUAGE_RUSSIAN = "ru"
        private const val LANGUAGE_ENGLISH = "en"
    }
}