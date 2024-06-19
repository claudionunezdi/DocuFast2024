package com.cnunez.docufast.useCamera.View
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.cnunez.docufast.R
import com.google.android.material.textfield.TextInputEditText

class EditFileNameDialogFragment(
    private val fileId: Int,
    private val callback: (String) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_edit_file_name, null)

            builder.setView(view)
                .setPositiveButton("Confirm") { _, _ ->
                    val editText = view.findViewById<TextInputEditText>(R.id.editTextFileName)
                    val newFileName = editText.text.toString()
                    callback(newFileName)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}