package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningapp.other.Constants.KEY_MOVEMENT
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment:Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen=true
    
    lateinit var selectedMovement: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstAppOpen){
            val navOptions= NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setupFragment_to_runFragment,
                savedInstanceState,
                navOptions
            )
        }

        val spinner: Spinner? =view.findViewById<Spinner>(R.id.movement_spinner)
        Timber.d("Spinner: ${spinner}")
        val movements=resources.getStringArray(R.array.movementType_array)
        if (spinner!=null)
        {
            Timber.d("Van spinner")
            this.activity?.let {
                val adapter=ArrayAdapter(
                    it,
                    android.R.layout.simple_spinner_item,
                    movements
                )
                spinner.adapter=adapter
            }

            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener{
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedMovement=movements[position]
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }


        //Tovább léphetünk-e, a következő fragmentre(run fragment). Akkor léphetünk tovább, ha ki van töltve  az összes mező(név, súly)
        btnContinue.setOnClickListener{
            val success=writePersonalDataToSharedPref()
            if (success){
                findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
            else{
                Snackbar.make(requireView(), "Kérlek töltsd ki az összes mezőt!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    // A felhasználó nevének és súlyának, illetve a mozgásformának a beállítása
    private fun writePersonalDataToSharedPref():Boolean{
        val name=etName.text.toString()
        val weight=etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty() || selectedMovement.isEmpty()){
            return false
        }

        Timber.d("${selectedMovement}")

        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putString(KEY_MOVEMENT, selectedMovement)
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()

        /*val toolbarText="Hello, $name!"
        requireActivity().tvToolbarTitle.text=toolbarText*/
        return true
    }
}
