package com.example.culturabcn.ui.contacto

import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.culturabcn.databinding.FragmentContactoBinding
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ContactoFragment : Fragment() {

    private var _binding: FragmentContactoBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private var ubicacionMarker: Marker? = null

    // Constante para permisos
    private val PERMISSIONS_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View {
        _binding = FragmentContactoBinding.inflate(inflater, container, false)

        // Inicializar configuración de osmdroid
        Configuration.getInstance().load(requireContext(), requireActivity().getSharedPreferences("osmdroid", AppCompatActivity.MODE_PRIVATE))

        // Inicializar MapView con binding
        mapView = binding.mapView
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        // Botón para abrir enlaces
        binding.btnInstagram.setOnClickListener {
            abrirEnlace("https://www.instagram.com/barcelona_cat/")
        }

        binding.btnTwitter.setOnClickListener {
            abrirEnlace("https://x.com/bcn_ajuntament?lang=es")
        }

        binding.btnAjuntament.setOnClickListener {
            abrirEnlace("https://guia.barcelona.cat/es/agenda")
        }

        // Configurar ubicación inicial del mapa
        mapView.controller.setZoom(18.0)
        mapView.controller.setCenter(GeoPoint(41.3870, 2.1700)) // Barcelona como ejemplo

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun abrirEnlace(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun initializeLocationUpdates() {
        val locationManager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f) { location ->
                updateLocationOnMap(location)
            }
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                updateLocationOnMap(location)
            } else {
                Toast.makeText(requireContext(), "Esperando actualización de ubicación...", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationOnMap(location: Location) {
        // Mover el mapa a la ubicación actual
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        mapView.controller.setCenter(geoPoint)

        // Añadir marcador de ubicación
        ubicacionMarker?.let {
            mapView.overlays.remove(it)
        }

        val marker = Marker(mapView)
        marker.position = geoPoint
        marker.title = "Tu ubicación"
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        mapView.overlays.add(marker)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLocationUpdates()
            } else {
                Toast.makeText(requireContext(), "Permiso de ubicación requerido", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
