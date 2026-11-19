package com.example.contactv4.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.contactv4.MainActivity
import com.example.contactv4.R
import com.example.contactv4.data.ContactContract.COLUMN_ID
import com.example.contactv4.databinding.FragmentListeContactsBinding

class ListeContactsFragment : Fragment() {

    private var _binding: FragmentListeContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    // ── Cycle de vie ──────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setToolbarTitle(getString(R.string.app_name))
        (activity as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Crée l'adapter avec les préférences actuelles
        val ctx = requireContext()
        adapter = ContactAdapter(
            onItemClick    = { contactId -> openDetail(contactId) },
            showCallButton = PreferencesManager.isAppelDepuisListeActif(ctx),
            taillePolice   = PreferencesManager.getTaillePolice(ctx),
            couleurTexte   = PreferencesManager.getCouleur(ctx)
        )
        binding.recyclerContacts.adapter = adapter

        // Observer LiveData
        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            adapter.submitList(contacts)
            binding.tvListeVide.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
        }

        // Chargement initial
        viewModel.loadAll()

        // Recherche en temps réel (insensible à la casse via le DAO SQLite)
        binding.etRecherche.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString()?.trim() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        // FAB → mode création (_id = 0)
        binding.fabAjouter.setOnClickListener {
            openDetail(0L)
        }
    }

    /**
     * Appelé à chaque fois que le fragment redevient visible (retour depuis Détail ou Paramètres).
     * Applique immédiatement les nouvelles préférences sans redémarrer l'app.
     */
    override fun onResume() {
        super.onResume()
        val ctx = requireContext()
        adapter.applyPreferences(
            showCall = PreferencesManager.isAppelDepuisListeActif(ctx),
            taille   = PreferencesManager.getTaillePolice(ctx),
            couleur  = PreferencesManager.getCouleur(ctx)
        )
        // Recharge aussi la liste (modifications éventuelles depuis le détail)
        viewModel.loadAll()
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private fun openDetail(contactId: Long) {
        val fragment = DetailContactFragment().apply {
            arguments = Bundle().apply { putLong(COLUMN_ID, contactId) }
        }
        (activity as? MainActivity)?.navigateTo(fragment, "detail_$contactId")
    }

    // ── Nettoyage ─────────────────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
