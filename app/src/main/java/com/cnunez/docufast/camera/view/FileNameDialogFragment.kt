package com.cnunez.docufast.camera.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment

class FileNameDialogFragment(
    private val defaultName: String,
    private val callback: (String) -> Unit
) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val input = EditText(requireContext()).apply { setText(defaultName) }
        return AlertDialog.Builder(requireContext())
            .setTitle("Save OCR Text")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                callback(input.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .create()
    }
}
