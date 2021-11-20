package com.example.runningapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentOnAttachListener
import com.example.runningapp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CancelTrackingDialog:DialogFragment() {

    private var yesListener: (()->Unit)?=null

    fun setYesListener(listener: ()->Unit){
        yesListener=listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Befejezed a futást?")
            .setMessage("Biztos vagy benne, hogy befejezed a futást és törlöd a futásod adatait?")
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