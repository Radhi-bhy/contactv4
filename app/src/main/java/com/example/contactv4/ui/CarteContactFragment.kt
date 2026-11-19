package com.example.contactv4.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.contactv4.MainActivity
import com.example.contactv4.R
import com.example.contactv4.databinding.FragmentCarteContactBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class CarteContactFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentCarteContactBinding? = null
    private val binding get() = _binding!!

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var nomComplet: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCarteContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        latitude = arguments?.getDouble("latitude", 0.0) ?: 0.0
        longitude = arguments?.getDouble("longitude", 0.0) ?: 0.0
        nomComplet = arguments?.getString("nom_complet") ?: ""

        (activity as? MainActivity)?.setToolbarTitle(getString(R.string.titre_carte))
        (activity as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Récupère le SupportMapFragment enfant et demande la carte
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(latitude, longitude)
        googleMap.addMarker(
            MarkerOptions()
                .position(position)
                .title(nomComplet)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 14f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
