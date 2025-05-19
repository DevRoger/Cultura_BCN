package com.example.culturabcn.ui.configuracion

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.culturabcn.R
import com.example.culturabcn.databinding.FragmentConfiguracionBinding
import java.util.Locale

class ConfiguracionFragment : Fragment() {

    private var _binding: FragmentConfiguracionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View {
        _binding = FragmentConfiguracionBinding.inflate(inflater, container, false)


        val imgEspana = binding.imgEspana
        val imgCatalan = binding.imgCatalan
        val imgIngles = binding.imgIngles


        imgEspana.setOnClickListener {
            setLocale("es") // Español
            Toast.makeText(requireContext(), "Se ha cambiado el idioma a Español.", Toast.LENGTH_SHORT).show()
        }

        imgCatalan.setOnClickListener {
            setLocale("ca") // Catalán
            Toast.makeText(requireContext(), "S\\'ha canviat l\\'idioma a Català.", Toast.LENGTH_SHORT).show()
        }

        imgIngles.setOnClickListener {
            setLocale("en") // Inglés
            Toast.makeText(requireContext(), "Language has been changed to English.", Toast.LENGTH_SHORT).show()
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        requireActivity().baseContext.resources.updateConfiguration(
            config,
            requireActivity().baseContext.resources.displayMetrics
                                                                   )

        // Reinicia la actividad actual para aplicar el nuevo idioma
        requireActivity().recreate()
    }

}
