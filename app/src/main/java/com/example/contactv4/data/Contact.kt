package com.example.contactv4.data

/**
 * Modèle de domaine représentant un contact dans l'application.
 *
 * @property id      Clé primaire SQLite (0 = non encore persisté).
 * @property nom     Nom de famille (obligatoire).
 * @property prenom  Prénom (obligatoire).
 * @property telephone Numéro de téléphone (obligatoire).
 * @property email   Adresse e-mail (optionnelle).
 * @property photoUri URI de la photo de profil (optionnelle).
 * @property latitude  Latitude GPS (optionnelle).
 * @property longitude Longitude GPS (optionnelle).
 */
data class Contact(
    val id: Long = 0,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String? = null,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
