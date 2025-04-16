package ru.itis.t_travelling.presentation.util

import android.content.Context
import android.provider.Settings.Global.getString
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun Fragment.hideKeyboard() {
    val activity = requireActivity()
    val view = activity.currentFocus ?: View(activity)
    val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun TextInputEditText.setupValidation(
    validator: (String) -> Boolean,
    textInputLayout: TextInputLayout?,
    errorResId: Int
) {
    doOnTextChanged { text, _, _, _ ->
        if (!validator(text.toString())) {
            textInputLayout?.error = context.getString(errorResId)
        } else {
            textInputLayout?.error = null
        }
    }
}

fun TextInputEditText.setupValidationOfNull(
    errorMessage: String,
    errorTarget: TextInputLayout?
) {
    doOnTextChanged { text, _, _, _ ->
        errorTarget?.error = if (text.isNullOrEmpty()) errorMessage else null
    }
}