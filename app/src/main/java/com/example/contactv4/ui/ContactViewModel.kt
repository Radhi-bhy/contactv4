package com.example.contactv4.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactv4.data.Contact
import com.example.contactv4.data.ContactDao
import com.example.contactv4.data.ContactDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel partagé pour la gestion des contacts.
 *
 * - Hérite de [AndroidViewModel] pour accéder à l'[Application] sans retenir une Activity.
 * - Expose la liste des contacts via [contacts] (LiveData immuable).
 * - Tous les accès à la base de données s'exécutent sur [Dispatchers.IO] via [viewModelScope].
 * - La liste est rafraîchie automatiquement après chaque INSERT, UPDATE ou DELETE.
 */
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    // ── DAO ──────────────────────────────────────────────────────────────────

    private val dao: ContactDao = ContactDao(
        ContactDbHelper.getInstance(application.applicationContext)
    )

    // ── État exposé ──────────────────────────────────────────────────────────

    private val _contacts = MutableLiveData<List<Contact>>(emptyList())

    /** Liste des contacts, triée par nom/prénom. Observez ce flux depuis vos Fragments/Activity. */
    val contacts: LiveData<List<Contact>> get() = _contacts

    /** Résultat de la dernière opération d'écriture : `null` = succès, sinon message d'erreur. */
    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> get() = _error

    // ── Initialisation ───────────────────────────────────────────────────────

    init {
        loadAll()
    }

    // ── Lecture ──────────────────────────────────────────────────────────────

    /** Charge (ou recharge) tous les contacts depuis la base de données. */
    fun loadAll() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { dao.getAll() }
            _contacts.value = list
        }
    }

    /**
     * Filtre les contacts dont le nom, prénom, téléphone ou e-mail contient [texte].
     * Passe une chaîne vide pour revenir à la liste complète.
     */
    fun search(texte: String) {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                if (texte.isBlank()) dao.getAll() else dao.search(texte)
            }
            _contacts.value = list
        }
    }

    // ── Écriture ─────────────────────────────────────────────────────────────

    /**
     * Insère [contact] et rafraîchit la liste.
     * @param onResult Callback invoqué sur le thread principal avec l'id généré (-1 = erreur).
     */
    fun insert(contact: Contact, onResult: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) { dao.insert(contact) }
            if (id == -1L) {
                _error.value = "Erreur lors de l'insertion du contact."
            } else {
                _error.value = null
                loadAll()
            }
            onResult?.invoke(id)
        }
    }

    /**
     * Met à jour [contact] et rafraîchit la liste.
     * @param onResult Callback invoqué sur le thread principal avec le nombre de lignes modifiées.
     */
    fun update(contact: Contact, onResult: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) { dao.update(contact) }
            if (rows == 0) {
                _error.value = "Aucun contact mis à jour (id=${contact.id})."
            } else {
                _error.value = null
                loadAll()
            }
            onResult?.invoke(rows)
        }
    }

    /**
     * Supprime le contact identifié par [id] et rafraîchit la liste.
     * @param onResult Callback invoqué sur le thread principal avec le nombre de lignes supprimées.
     */
    fun delete(id: Long, onResult: ((Int) -> Unit)? = null) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) { dao.delete(id) }
            if (rows == 0) {
                _error.value = "Aucun contact supprimé (id=$id)."
            } else {
                _error.value = null
                loadAll()
            }
            onResult?.invoke(rows)
        }
    }

    /**
     * Récupère un contact par son [id] sans mettre à jour [contacts].
     * Utile pour pré-remplir un formulaire d'édition.
     * @param onResult Callback sur le thread principal avec le contact trouvé ou `null`.
     */
    fun getById(id: Long, onResult: (Contact?) -> Unit) {
        viewModelScope.launch {
            val contact = withContext(Dispatchers.IO) { dao.getById(id) }
            onResult(contact)
        }
    }
}
