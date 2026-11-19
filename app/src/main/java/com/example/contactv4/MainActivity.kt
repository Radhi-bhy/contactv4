package com.example.contactv4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.contactv4.databinding.ActivityMainBinding
import com.example.contactv4.ui.ListeContactsFragment
import com.example.contactv4.ui.ParametresFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure la Toolbar comme ActionBar
        setSupportActionBar(binding.toolbar)

        // Charge le fragment initial uniquement au premier démarrage (pas à la rotation)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListeContactsFragment())
                .commit()
        }
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_parametres -> {
                navigateTo(ParametresFragment(), "parametres")
                true
            }
            android.R.id.home -> {
                // Bouton retour de la Toolbar → dépile le back stack
                supportFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ── Navigation helpers ────────────────────────────────────────────────────

    /**
     * Affiche [fragment] dans le conteneur principal en l'empilant sur la back stack.
     * Le tag fourni est utilisé pour identifier le fragment existant.
     */
    fun navigateTo(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    /** Met à jour le titre de la Toolbar. */
    fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }
}
