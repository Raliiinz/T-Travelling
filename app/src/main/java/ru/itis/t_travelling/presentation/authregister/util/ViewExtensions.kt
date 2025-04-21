package ru.itis.t_travelling.presentation.authregister.util

import android.content.Context
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import ru.itis.t_travelling.R

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


fun TextInputLayout.setupPasswordToggle(editText: TextInputEditText?) {
    this.setEndIconOnClickListener {
        val isPasswordVisible = editText?.transformationMethod is HideReturnsTransformationMethod
        editText?.transformationMethod = if (isPasswordVisible) {
            PasswordTransformationMethod.getInstance()
        } else {
            HideReturnsTransformationMethod.getInstance()
        }
        this.endIconDrawable = ContextCompat.getDrawable(
            this.context,
            if (isPasswordVisible) R.drawable.ic_eye_closed else R.drawable.ic_eye_open
        )
        editText?.setSelection(editText.text?.length ?: 0)
    }
}

fun TextInputLayout.setupPasswordToggle() {
    val editText = this.editText as? TextInputEditText
    setupPasswordToggle(editText)
}