package com.example.contactv4.data

import android.content.ContentValues
import android.database.Cursor
import com.example.contactv4.data.ContactContract.COLUMN_EMAIL
import com.example.contactv4.data.ContactContract.COLUMN_ID
import com.example.contactv4.data.ContactContract.COLUMN_LATITUDE
import com.example.contactv4.data.ContactContract.COLUMN_LONGITUDE
import com.example.contactv4.data.ContactContract.COLUMN_NOM
import com.example.contactv4.data.ContactContract.COLUMN_PHOTO_URI
import com.example.contactv4.data.ContactContract.COLUMN_PRENOM
import com.example.contactv4.data.ContactContract.COLUMN_TELEPHONE
import com.example.contactv4.data.ContactContract.TABLE_NAME

/**
 * Couche d'accès aux données (DAO) pour la table [ContactContract.TABLE_NAME].
 *
 * Règles appliquées :
 * - Toutes les requêtes utilisent des paramètres `?` (selectionArgs) — jamais de concaténation.
 * - Chaque [Cursor] est fermé via `.use { }`.
 * - Les index de colonnes sont récupérés avec [Cursor.getColumnIndexOrThrow].
 * - Les résultats sont triés par `nom` puis `prenom` COLLATE NOCASE.
 */
class ContactDao(private val dbHelper: ContactDbHelper) {

    // ── Écriture ─────────────────────────────────────────────────────────────

    /**
     * Insère un nouveau contact.
     * @return L'identifiant généré, ou -1 en cas d'erreur.
     */
    fun insert(contact: Contact): Long {
        val db = dbHelper.writableDatabase
        return db.insert(TABLE_NAME, null, contact.toContentValues())
    }

    /**
     * Met à jour un contact existant identifié par son [Contact.id].
     * @return Le nombre de lignes modifiées (1 si succès, 0 sinon).
     */
    fun update(contact: Contact): Int {
        val db = dbHelper.writableDatabase
        return db.update(
            TABLE_NAME,
            contact.toContentValues(),
            "$COLUMN_ID = ?",
            arrayOf(contact.id.toString())
        )
    }

    /**
     * Supprime le contact dont l'identifiant est [id].
     * @return Le nombre de lignes supprimées (1 si succès, 0 sinon).
     */
    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            TABLE_NAME,
            "$COLUMN_ID = ?",
            arrayOf(id.toString())
        )
    }

    // ── Lecture ──────────────────────────────────────────────────────────────

    /**
     * Retourne tous les contacts triés par nom puis prénom (insensible à la casse).
     */
    fun getAll(): List<Contact> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            /* table        */ TABLE_NAME,
            /* columns      */ null,
            /* selection    */ null,
            /* selectionArgs*/ null,
            /* groupBy      */ null,
            /* having       */ null,
            /* orderBy      */ "$COLUMN_NOM COLLATE NOCASE ASC, $COLUMN_PRENOM COLLATE NOCASE ASC"
        )
        return cursor.use { it.toContactList() }
    }

    /**
     * Retourne le contact correspondant à [id], ou `null` s'il n'existe pas.
     */
    fun getById(id: Long): Contact? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) it.toContact() else null
        }
    }

    /**
     * Recherche les contacts dont le nom, prénom, téléphone ou e-mail
     * contient [texte] (recherche insensible à la casse via LIKE).
     *
     * Les paramètres `?` protègent contre toute injection SQL.
     */
    fun search(texte: String): List<Contact> {
        val db = dbHelper.readableDatabase
        val pattern = "%$texte%"
        val cursor = db.query(
            TABLE_NAME,
            null,
            """
                $COLUMN_NOM       LIKE ? OR
                $COLUMN_PRENOM    LIKE ? OR
                $COLUMN_TELEPHONE LIKE ? OR
                $COLUMN_EMAIL     LIKE ?
            """.trimIndent(),
            arrayOf(pattern, pattern, pattern, pattern),
            null, null,
            "$COLUMN_NOM COLLATE NOCASE ASC, $COLUMN_PRENOM COLLATE NOCASE ASC"
        )
        return cursor.use { it.toContactList() }
    }

    // ── Helpers privés ───────────────────────────────────────────────────────

    /** Mappe un [Contact] vers un [ContentValues] pour INSERT / UPDATE. */
    private fun Contact.toContentValues(): ContentValues = ContentValues().apply {
        put(COLUMN_NOM, nom)
        put(COLUMN_PRENOM, prenom)
        put(COLUMN_TELEPHONE, telephone)
        if (email != null) put(COLUMN_EMAIL, email) else putNull(COLUMN_EMAIL)
        if (photoUri != null) put(COLUMN_PHOTO_URI, photoUri) else putNull(COLUMN_PHOTO_URI)
        if (latitude != null) put(COLUMN_LATITUDE, latitude) else putNull(COLUMN_LATITUDE)
        if (longitude != null) put(COLUMN_LONGITUDE, longitude) else putNull(COLUMN_LONGITUDE)
    }

    /**
     * Lit le [Contact] sur la ligne courante du curseur.
     * Utilise [Cursor.getColumnIndexOrThrow] pour détecter tout désalignement de schéma.
     */
    private fun Cursor.toContact(): Contact {
        val latIndex = getColumnIndexOrThrow(COLUMN_LATITUDE)
        val lonIndex = getColumnIndexOrThrow(COLUMN_LONGITUDE)
        val emailIndex = getColumnIndexOrThrow(COLUMN_EMAIL)
        val photoIndex = getColumnIndexOrThrow(COLUMN_PHOTO_URI)
        return Contact(
            id = getLong(getColumnIndexOrThrow(COLUMN_ID)),
            nom = getString(getColumnIndexOrThrow(COLUMN_NOM)),
            prenom = getString(getColumnIndexOrThrow(COLUMN_PRENOM)),
            telephone = getString(getColumnIndexOrThrow(COLUMN_TELEPHONE)),
            email = if (isNull(emailIndex)) null else getString(emailIndex),
            photoUri = if (isNull(photoIndex)) null else getString(photoIndex),
            latitude = if (isNull(latIndex)) null else getDouble(latIndex),
            longitude = if (isNull(lonIndex)) null else getDouble(lonIndex)
        )
    }

    /** Itère sur toutes les lignes du curseur et retourne la liste de [Contact]. */
    private fun Cursor.toContactList(): List<Contact> {
        val list = mutableListOf<Contact>()
        while (moveToNext()) {
            list.add(toContact())
        }
        return list
    }
}
