package com.example.contactv4.ui

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

/**
 * Façade sur les SharedPreferences par défaut.
 * Toutes les clés sont centralisées ici pour éviter les typos.
 */
object PreferencesManager {

    // ── Clés (doivent correspondre exactement aux app:key dans preferences.xml) ──

    const val KEY_TAILLE_POLICE   = "taille_police"
    const val KEY_COULEUR          = "couleur_preferee"
    const val KEY_APPEL_LISTE      = "appel_depuis_liste"

    // ── Valeurs par défaut ────────────────────────────────────────────────────

    private const val DEFAULT_TAILLE  = "16"
    private const val DEFAULT_COULEUR = "#111111"
    private const val DEFAULT_APPEL   = false

    // ── Accesseurs ────────────────────────────────────────────────────────────

    /**
     * Retourne la taille de police en **sp** (Float) lue depuis les préférences.
     * Défaut : 16sp (Moyenne).
     */
    fun getTaillePolice(context: Context): Float {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(KEY_TAILLE_POLICE, DEFAULT_TAILLE)
            ?.toFloatOrNull() ?: DEFAULT_TAILLE.toFloat()
    }

    /**
     * Retourne la couleur préférée sous forme d'[Int] ARGB (prêt pour [android.widget.TextView.setTextColor]).
     * Défaut : #111111 (Noir).
     */
    fun getCouleur(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val hex = prefs.getString(KEY_COULEUR, DEFAULT_COULEUR) ?: DEFAULT_COULEUR
        return try {
            Color.parseColor(hex)
        } catch (e: IllegalArgumentException) {
            Color.parseColor(DEFAULT_COULEUR)
        }
    }

    /**
     * Retourne `true` si le bouton d'appel rapide doit être visible dans la liste.
     * Défaut : `false`.
     */
    fun isAppelDepuisListeActif(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(KEY_APPEL_LISTE, DEFAULT_APPEL)
    }
}
