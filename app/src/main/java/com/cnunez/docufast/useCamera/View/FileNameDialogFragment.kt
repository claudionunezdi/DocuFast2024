package com.cnunez.docufast.useCamera.View
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.cnunez.docufast.R

class FileNameDialogFragment(private val defaultName: String, private val callback: (String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_file_name, null)
            val editText = view.findViewById<EditText>(R.id.EditFileName)

            editText.setText(defaultName)

            builder.setView(view)
                .setPositiveButton("Save") { _, _ ->
                    val fileName = editText.text.toString()
                    callback(fileName)
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}