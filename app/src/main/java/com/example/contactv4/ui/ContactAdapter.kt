package com.example.contactv4.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactv4.data.Contact
import com.example.contactv4.databinding.ItemContactBinding
import java.io.File

/**
 * Adapter RecyclerView pour la liste des contacts.
 *
 * @param onItemClick    Appelé avec l'[Contact.id] (_id SQLite) quand l'utilisateur clique sur une ligne.
 * @param showCallButton Contrôle la visibilité du bouton d'appel rapide.
 * @param taillePolice   Taille de police appliquée au nom et au téléphone (en sp).
 * @param couleurTexte   Couleur ARGB appliquée au nom et au téléphone.
 */
class ContactAdapter(
    private val onItemClick: (Long) -> Unit,
    private var showCallButton: Boolean = false,
    private var taillePolice: Float = 16f,
    private var couleurTexte: Int = android.graphics.Color.BLACK
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    // ── Mise à jour des préférences ───────────────────────────────────────────

    /**
     * Met à jour les préférences visuelles et rafraîchit toutes les lignes visibles.
     * Appelé depuis [ListeContactsFragment.onResume].
     */
    fun applyPreferences(showCall: Boolean, taille: Float, couleur: Int) {
        showCallButton = showCall
        taillePolice   = taille
        couleurTexte   = couleur
        notifyDataSetChanged()
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────

    inner class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            // Textes
            binding.tvNomPrenom.text  = "${contact.prenom} ${contact.nom}"
            binding.tvTelephone.text  = contact.telephone

            // Préférences : taille de police et couleur
            binding.tvNomPrenom.textSize  = taillePolice
            binding.tvTelephone.textSize  = taillePolice
            binding.tvNomPrenom.setTextColor(couleurTexte)
            binding.tvTelephone.setTextColor(couleurTexte)

            // Photo locale depuis filesDir
            val photoFile = contact.photoUri?.let { File(it) }
            if (photoFile != null && photoFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                binding.ivPhoto.setImageBitmap(
                    bitmap ?: android.graphics.BitmapFactory.decodeResource(
                        binding.ivPhoto.resources,
                        android.R.drawable.ic_menu_gallery
                    )
                )
            } else {
                binding.ivPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Bouton d'appel rapide
            binding.btnAppelRapide.visibility = if (showCallButton) View.VISIBLE else View.GONE
            binding.btnAppelRapide.setOnClickListener {
                // ACTION_DIAL uniquement — ne déclenche PAS le clic sur la ligne
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.telephone}"))
                it.context.startActivity(intent)
            }

            // Clic sur la ligne → ouvre le détail via l'_id SQLite
            binding.root.setOnClickListener {
                onItemClick(contact.id)
            }
        }
    }

    // ── DiffUtil ─────────────────────────────────────────────────────────────

    private class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact) =
            oldItem == newItem
    }
}
