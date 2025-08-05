package com.cnunez.docufast.common.utils

import android.content.Context
import android.util.Patterns
import com.cnunez.docufast.R

class EmailValidator(private val context: Context) {
    fun validate(email: String?): ValidationResult {
        return when {
            email.isNullOrBlank() -> ValidationResult.Error(
                context.getString(R.string.error_email_empty)
            )
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> ValidationResult.Error(
                context.getString(R.string.error_email_invalid)
            )
            else -> ValidationResult.Success
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}