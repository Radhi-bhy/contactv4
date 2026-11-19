package com.example.contactv4.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.contactv4.data.ContactContract.COLUMN_EMAIL
import com.example.contactv4.data.ContactContract.COLUMN_LATITUDE
import com.example.contactv4.data.ContactContract.COLUMN_LONGITUDE
import com.example.contactv4.data.ContactContract.COLUMN_NOM
import com.example.contactv4.data.ContactContract.COLUMN_PHOTO_URI
import com.example.contactv4.data.ContactContract.COLUMN_PRENOM
import com.example.contactv4.data.ContactContract.COLUMN_TELEPHONE
import com.example.contactv4.data.ContactContract.DATABASE_NAME
import com.example.contactv4.data.ContactContract.DATABASE_VERSION
import com.example.contactv4.data.ContactContract.SQL_CREATE_TABLE
import com.example.contactv4.data.ContactContract.SQL_DROP_TABLE
import com.example.contactv4.data.ContactContract.TABLE_NAME

/**
 * Gestionnaire SQLite de la base de données contacts.
 *
 * Singleton : toujours instancié avec [applicationContext] pour éviter les fuites mémoire.
 * Usage : `ContactDbHelper.getInstance(context)`
 */
class ContactDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    // ── Singleton ────────────────────────────────────────────────────────────

    companion object {
        @Volatile
        private var INSTANCE: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactDbHelper(context).also { INSTANCE = it }
            }
    }

    // ── Cycle de vie de la base ──────────────────────────────────────────────

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE)
        insertDemoContacts(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Stratégie simple : recréer la table (les données de démo sont réinsérées).
        // Pour une migration non-destructive, ajouter ici des ALTER TABLE selon oldVersion.
        db.execSQL(SQL_DROP_TABLE)
        onCreate(db)
    }

    // ── Données de démonstration ─────────────────────────────────────────────

    /**
     * Insère 5 contacts tunisiens réalistes :
     *  - 3 avec coordonnées GPS valides,
     *  - 2 avec latitude/longitude à null.
     */
    private fun insertDemoContacts(db: SQLiteDatabase) {
        val demos = listOf(
            // Contacts AVEC coordonnées GPS
            demoValues(
                nom = "Ben Ali",
                prenom = "Ali",
                telephone = "+216 99 123 456",
                email = "ali.benali@example.tn",
                latitude = 33.825217,
                longitude = 10.921541
            ),
            demoValues(
                nom = "Ben Saleh",
                prenom = "Saleh",
                telephone = "+216 55 987 654",
                email = "saleh@example.tn",
                latitude = 33.818194,
                longitude = 10.972434
            ),
            demoValues(
                nom = "Jerbi",
                prenom = "Karim",
                telephone = "+216 98 456 789",
                email = "karim@example.tn",
                latitude = 33.868875,
                longitude = 10.862711
            ),
            demoValues(
                nom = "Trabelsi",
                prenom = "Slim",
                telephone = "+216 25 741 852",
                email = null,
                latitude = null,
                longitude = null
            ),
            demoValues(
                nom = "Tounsi",
                prenom = "Yassine",
                telephone = "+216 52 369 147",
                email = "yassine.hamdi@example.tn",
                latitude = null,
                longitude = null
            )
        )
        demos.forEach { db.insert(TABLE_NAME, null, it) }
    }

    /** Construit un [ContentValues] pour l'insertion d'un contact de démonstration. */
    private fun demoValues(
        nom: String,
        prenom: String,
        telephone: String,
        email: String?,
        latitude: Double?,
        longitude: Double?
    ): ContentValues = ContentValues().apply {
        put(COLUMN_NOM, nom)
        put(COLUMN_PRENOM, prenom)
        put(COLUMN_TELEPHONE, telephone)
        if (email != null) put(COLUMN_EMAIL, email) else putNull(COLUMN_EMAIL)
        putNull(COLUMN_PHOTO_URI)
        if (latitude != null) put(COLUMN_LATITUDE, latitude) else putNull(COLUMN_LATITUDE)
        if (longitude != null) put(COLUMN_LONGITUDE, longitude) else putNull(COLUMN_LONGITUDE)
    }
}
