package com.example.contactv4.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.contactv4.MainActivity
import com.example.contactv4.R
import com.example.contactv4.data.Contact
import com.example.contactv4.data.ContactContract.COLUMN_ID
import com.example.contactv4.databinding.FragmentDetailContactBinding
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DetailContactFragment : Fragment() {

    private var _binding: FragmentDetailContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()

    /** _id transmis via arguments. 0L = mode création. */
    private var contactId: Long = 0L

    /** Chemin local de la photo enregistrée dans filesDir. */
    private var photoLocalPath: String? = null

    /** URI temporaire utilisé par la caméra (dans cacheDir). */
    private var cameraUri: Uri? = null

    // ── ActivityResult launchers ──────────────────────────────────────────────

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@registerForActivityResult
            copyImageToInternalStorage(uri)?.let { path ->
                photoLocalPath = path
                showPhoto(path)
            }
        }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val tempUri = cameraUri ?: return@registerForActivityResult
                copyUriToInternalStorage(tempUri)?.let { path ->
                    photoLocalPath = path
                    showPhoto(path)
                }
            }
        }

    // ── Cycle de vie ──────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactId = arguments?.getLong(COLUMN_ID, 0L) ?: 0L
        val modeCreation = contactId == 0L

        // Titre Toolbar + bouton retour
        val titre = if (modeCreation) getString(R.string.titre_creation) else getString(R.string.titre_edition)
        (activity as? MainActivity)?.setToolbarTitle(titre)
        (activity as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Applique les préférences de style dès l'ouverture
        appliquerPreferences()

        // Bouton Supprimer visible uniquement en édition
        binding.btnSupprimer.visibility = if (modeCreation) View.GONE else View.VISIBLE

        // Charge le contact existant
        if (!modeCreation) {
            viewModel.getById(contactId) { contact ->
                contact?.let { remplirFormulaire(it) }
            }
        }

        // ── Galerie ───────────────────────────────────────────────────────────
        binding.btnGalerie.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // ── Caméra ────────────────────────────────────────────────────────────
        binding.btnCamera.setOnClickListener {
            val tempFile = createTempCameraFile()
            val uri = FileProvider.getUriForFile(
                requireContext(),
                getString(R.string.file_provider_authority),
                tempFile
            )
            cameraUri = uri
            takePictureLauncher.launch(uri)
        }

        // ── Appeler ───────────────────────────────────────────────────────────
        binding.btnAppeler.setOnClickListener {
            val tel = binding.etTelephone.text?.toString()?.trim()
            if (tel.isNullOrBlank()) {
                Toast.makeText(context, getString(R.string.erreur_telephone_vide), Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel")))
            }
        }

        // ── E-mail ────────────────────────────────────────────────────────────
        binding.btnEmailAction.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim()
            if (email.isNullOrBlank()) {
                Toast.makeText(context, getString(R.string.erreur_email_absent), Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")))
            }
        }

        // ── Carte ─────────────────────────────────────────────────────────────
        binding.btnCarte.setOnClickListener {
            val lat = binding.etLatitude.text?.toString()?.toDoubleOrNull()
            val lon = binding.etLongitude.text?.toString()?.toDoubleOrNull()
            when {
                lat == null || lon == null ->
                    Toast.makeText(context, getString(R.string.erreur_coordonnees_absentes), Toast.LENGTH_SHORT).show()
                !latitudeValide(lat) || !longitudeValide(lon) ->
                    Toast.makeText(context,
                        "${getString(R.string.erreur_latitude)} / ${getString(R.string.erreur_longitude)}",
                        Toast.LENGTH_SHORT).show()
                else -> {
                    val prenom = binding.etPrenom.text?.toString()?.trim() ?: ""
                    val nom    = binding.etNom.text?.toString()?.trim() ?: ""
                    val fragment = CarteContactFragment().apply {
                        arguments = Bundle().apply {
                            putDouble("latitude", lat)
                            putDouble("longitude", lon)
                            putString("nom_complet", "$prenom $nom".trim())
                        }
                    }
                    (activity as? MainActivity)?.navigateTo(fragment, "carte_$contactId")
                }
            }
        }

        // ── Enregistrer ───────────────────────────────────────────────────────
        binding.btnEnregistrer.setOnClickListener {
            if (validerFormulaire()) enregistrer()
        }

        // ── Annuler ───────────────────────────────────────────────────────────
        binding.btnAnnuler.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ── Supprimer ─────────────────────────────────────────────────────────
        binding.btnSupprimer.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.dialog_supprimer_titre))
                .setMessage(getString(R.string.dialog_supprimer_message))
                .setPositiveButton(getString(R.string.dialog_oui)) { _, _ ->
                    viewModel.delete(contactId) {
                        parentFragmentManager.popBackStack()
                    }
                }
                .setNegativeButton(getString(R.string.dialog_non), null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-applique les préférences si l'utilisateur revient des paramètres
        appliquerPreferences()
    }

    // ── Préférences ───────────────────────────────────────────────────────────

    /**
     * Applique la taille de police et la couleur des préférences aux champs
     * nom, prénom et téléphone (les trois champs "identité" du contact).
     */
    private fun appliquerPreferences() {
        val ctx    = requireContext()
        val taille = PreferencesManager.getTaillePolice(ctx)
        val couleur = PreferencesManager.getCouleur(ctx)

        binding.etNom.textSize       = taille
        binding.etPrenom.textSize    = taille
        binding.etTelephone.textSize = taille

        binding.etNom.setTextColor(couleur)
        binding.etPrenom.setTextColor(couleur)
        binding.etTelephone.setTextColor(couleur)
    }

    // ── Formulaire ────────────────────────────────────────────────────────────

    private fun remplirFormulaire(contact: Contact) {
        binding.etNom.setText(contact.nom)
        binding.etPrenom.setText(contact.prenom)
        binding.etTelephone.setText(contact.telephone)
        binding.etEmail.setText(contact.email ?: "")
        binding.etLatitude.setText(contact.latitude?.toString() ?: "")
        binding.etLongitude.setText(contact.longitude?.toString() ?: "")

        contact.photoUri?.let { path ->
            photoLocalPath = path
            showPhoto(path)
        }
    }

    private fun validerFormulaire(): Boolean {
        var ok = true

        val nom = binding.etNom.text?.toString()?.trim()
        if (nom.isNullOrBlank()) {
            binding.tilNom.error = getString(R.string.erreur_nom_vide)
            ok = false
        } else {
            binding.tilNom.error = null
        }

        val prenom = binding.etPrenom.text?.toString()?.trim()
        if (prenom.isNullOrBlank()) {
            binding.tilPrenom.error = getString(R.string.erreur_prenom_vide)
            ok = false
        } else {
            binding.tilPrenom.error = null
        }

        val telephone = binding.etTelephone.text?.toString()?.trim()
        if (telephone.isNullOrBlank()) {
            binding.tilTelephone.error = getString(R.string.erreur_telephone_vide)
            ok = false
        } else {
            binding.tilTelephone.error = null
        }

        val latStr = binding.etLatitude.text?.toString()?.trim()
        if (!latStr.isNullOrBlank()) {
            val lat = latStr.toDoubleOrNull()
            if (lat == null || !latitudeValide(lat)) {
                binding.tilLatitude.error = getString(R.string.erreur_latitude)
                ok = false
            } else {
                binding.tilLatitude.error = null
            }
        } else {
            binding.tilLatitude.error = null
        }

        val lonStr = binding.etLongitude.text?.toString()?.trim()
        if (!lonStr.isNullOrBlank()) {
            val lon = lonStr.toDoubleOrNull()
            if (lon == null || !longitudeValide(lon)) {
                binding.tilLongitude.error = getString(R.string.erreur_longitude)
                ok = false
            } else {
                binding.tilLongitude.error = null
            }
        } else {
            binding.tilLongitude.error = null
        }

        return ok
    }

    private fun enregistrer() {
        val contact = Contact(
            id        = contactId,
            nom       = binding.etNom.text?.toString()?.trim() ?: "",
            prenom    = binding.etPrenom.text?.toString()?.trim() ?: "",
            telephone = binding.etTelephone.text?.toString()?.trim() ?: "",
            email     = binding.etEmail.text?.toString()?.trim()?.ifBlank { null },
            photoUri  = photoLocalPath,
            latitude  = binding.etLatitude.text?.toString()?.trim()?.toDoubleOrNull(),
            longitude = binding.etLongitude.text?.toString()?.trim()?.toDoubleOrNull()
        )
        if (contactId == 0L) {
            viewModel.insert(contact) { newId ->
                if (newId != -1L) parentFragmentManager.popBackStack()
            }
        } else {
            viewModel.update(contact) { rows ->
                if (rows > 0) parentFragmentManager.popBackStack()
            }
        }
    }

    // ── Photo ─────────────────────────────────────────────────────────────────

    private fun showPhoto(path: String) {
        BitmapFactory.decodeFile(path)?.let { binding.ivPhoto.setImageBitmap(it) }
    }

    private fun copyImageToInternalStorage(sourceUri: Uri): String? = try {
        val dir  = File(requireContext().filesDir, "photos").also { it.mkdirs() }
        val dest = File(dir, "${UUID.randomUUID()}.jpg")
        requireContext().contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur copie image : ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }

    private fun copyUriToInternalStorage(sourceUri: Uri): String? = try {
        val dir  = File(requireContext().filesDir, "photos").also { it.mkdirs() }
        val dest = File(dir, "${UUID.randomUUID()}.jpg")
        requireContext().contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur copie photo : ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }

    private fun createTempCameraFile(): File {
        val dir = File(requireContext().cacheDir, "photos").also { it.mkdirs() }
        return File(dir, "temp_camera_${UUID.randomUUID()}.jpg")
    }

    // ── Validation ────────────────────────────────────────────────────────────

    private fun latitudeValide(lat: Double)  = lat in -90.0..90.0
    private fun longitudeValide(lon: Double) = lon in -180.0..180.0

    // ── Nettoyage ─────────────────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
