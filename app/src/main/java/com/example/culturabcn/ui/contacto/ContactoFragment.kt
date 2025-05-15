package com.example.culturabcn.ui.contacto

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
        mapView.controller.setCenter(GeoPoint(41.4028766,2.1747596)) // Barcelona como ejemplo


        val edificioCulturaMark = GeoPoint(41.4028766,2.1747596) // Usamos las mismas coordenadas de antes

        val marcadorFijo = Marker(mapView)
        marcadorFijo.position = edificioCulturaMark
        marcadorFijo.title = "Edifici Cultural, Can Manyé" // Título para el marcador

        // Cargar el drawable 'location_mark.xml'
        // Asegúrate de que location_mark.xml está en la carpeta res/drawable
        val marcadorIcon = ResourcesCompat.getDrawable(resources, com.example.culturabcn.R.drawable.location_mark, null)
        marcadorFijo.icon = marcadorIcon

        // Ajustar el anclaje del marcador (opcional, para centrar la base del icono en la coordenada)
        marcadorFijo.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

        // Añadir el marcador al mapa
        mapView.overlays.add(marcadorFijo)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
