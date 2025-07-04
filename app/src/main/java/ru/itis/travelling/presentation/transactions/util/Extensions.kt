package ru.itis.travelling.presentation.transactions.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.addSimpleTextWatcher(afterTextChanged: (String?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) = afterTextChanged(s?.toString())
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}
