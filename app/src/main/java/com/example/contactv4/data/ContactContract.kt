package com.example.contactv4.data

/**
 * Contrat de la base de données contacts.
 * Toutes les constantes SQL (nom de table, colonnes) sont centralisées ici.
 * Aucun nom de colonne ne doit être écrit en dur en dehors de cet objet.
 */
object ContactContract {

    /** Nom de la base de données SQLite. */
    const val DATABASE_NAME = "contacts.db"

    /** Version courante du schéma. Incrémentée à chaque changement de structure. */
    const val DATABASE_VERSION = 1

    /** Nom de la table principale. */
    const val TABLE_NAME = "contacts"

    // ── Colonnes ────────────────────────────────────────────────────────────

    /** Clé primaire auto-incrémentée. */
    const val COLUMN_ID = "_id"

    /** Nom de famille. */
    const val COLUMN_NOM = "nom"

    /** Prénom. */
    const val COLUMN_PRENOM = "prenom"

    /** Numéro de téléphone. */
    const val COLUMN_TELEPHONE = "telephone"

    /** Adresse e-mail (peut être NULL). */
    const val COLUMN_EMAIL = "email"

    /** URI de la photo (peut être NULL). */
    const val COLUMN_PHOTO_URI = "photo_uri"

    /** Latitude GPS (peut être NULL). */
    const val COLUMN_LATITUDE = "latitude"

    /** Longitude GPS (peut être NULL). */
    const val COLUMN_LONGITUDE = "longitude"

    // ── Instructions DDL ────────────────────────────────────────────────────

    /** Instruction CREATE TABLE. */
    internal const val SQL_CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS $TABLE_NAME (
            $COLUMN_ID        INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_NOM       TEXT    NOT NULL,
            $COLUMN_PRENOM    TEXT    NOT NULL,
            $COLUMN_TELEPHONE TEXT    NOT NULL,
            $COLUMN_EMAIL     TEXT,
            $COLUMN_PHOTO_URI TEXT,
            $COLUMN_LATITUDE  REAL,
            $COLUMN_LONGITUDE REAL
        )
    """

    /** Instruction DROP TABLE (utilisée dans onUpgrade). */
    internal const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
}
