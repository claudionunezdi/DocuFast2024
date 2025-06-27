package com.cnunez.docufast.camera.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.cnunez.docufast.R
import com.google.android.material.textfield.TextInputEditText

class EditFileNameDialogFragment(
    private val fileId: String,
    private val callback: (String) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle("Edit File Name")
            .setView(TextInputEditText(requireContext()).apply { hint = "New name" })
            .setPositiveButton("OK") { _, _ ->
                // retrieve input
                callback((dialog as AlertDialog).findViewById<TextInputEditText>(android.R.id.edit)?.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
