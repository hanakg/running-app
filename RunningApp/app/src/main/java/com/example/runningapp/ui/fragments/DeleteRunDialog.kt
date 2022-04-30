package com.example.runningapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.runningapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteRunDialog: DialogFragment() {

    private var yesListener: (() -> Unit)? = null

    fun setYesListener(listener: () -> Unit) {
        yesListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Törlöd a kijelölt futást?")
            .setMessage("Biztos, hogy ki akarod törölni a kiválasztott futást?")
            .setCancelable(false)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Igen"){ _, _ ->
                yesListener?.let { yes->
                    yes()
                }
            }
            .setNegativeButton("Nem"){dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
    }
}