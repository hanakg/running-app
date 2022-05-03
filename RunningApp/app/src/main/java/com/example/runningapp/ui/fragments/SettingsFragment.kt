package com.example.runningapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.runningapp.R
import com.example.runningapp.other.Constants.KEY_MOVEMENT
import com.example.runningapp.other.Constants.KEY_NAME
import com.example.runningapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment:Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    lateinit var selectedMovement: String

    lateinit var movements: Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        movements=resources.getStringArray(R.array.movementType_array)
        val spinner: Spinner? =view.findViewById<Spinner>(R.id.movement_spinner)
        Timber.d("Spinner: ${spinner}")

        if (spinner!=null)
        {
            Timber.d("Van spinner")
            this.activity?.let {
                val adapter= ArrayAdapter(
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

        loadFieldFromSharedPref()

        btnApplyChanges.setOnClickListener {
            val success=applyChangesToSharedPref()

            if (success){
                Snackbar.make(view, "Változások elmentve!", Snackbar.LENGTH_LONG).show()
            }
            else{
                Snackbar.make(view, "Kérlek töltsd ki az összes mezőt", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun loadFieldFromSharedPref(){
        val name=sharedPreferences.getString(KEY_NAME, "")
        val weight=sharedPreferences.getFloat(KEY_WEIGHT, 80f)
        val movement=sharedPreferences.getString(KEY_MOVEMENT, "Futás")
        etName.setText(name)
        etWeight.setText(weight.toString())
        movement_spinner.setSelection(movements.indexOf(movement))
    }

    private fun applyChangesToSharedPref():Boolean{
        val nameText=etName.text.toString()
        val weightText=etWeight.text.toString()
        if (nameText.isEmpty() || weightText.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .putString(KEY_MOVEMENT, selectedMovement)
            .apply()

        val toolbarText="Gyerünk, $nameText!"
        requireActivity().tvToolbarTitle.text=toolbarText
        return true
    }
}