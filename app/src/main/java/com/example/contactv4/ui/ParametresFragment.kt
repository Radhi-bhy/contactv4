package com.example.contactv4.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.contactv4.MainActivity
import com.example.contactv4.R

/**
 * Fragment de préférences utilisant AndroidX Preference.
 * Charge [R.xml.preferences] et délègue tout l'affichage à [PreferenceFragmentCompat].
 */
class ParametresFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.setToolbarTitle(getString(R.string.titre_parametres))
        (activity as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}
