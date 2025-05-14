package com.example.culturabcn.ui.contacto

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.culturabcn.databinding.FragmentConfiguracionBinding
import com.example.culturabcn.databinding.FragmentContactoBinding

class ContactoFragment : Fragment() {

    private var _binding: FragmentContactoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View {
        _binding = FragmentContactoBinding.inflate(inflater, container, false)

        binding.btnInstagram.setOnClickListener {
            abrirEnlace("https://www.instagram.com/barcelona_cat/")
        }

        binding.btnTwitter.setOnClickListener {
            abrirEnlace("https://x.com/bcn_ajuntament?lang=es")
        }

        binding.btnAjuntament.setOnClickListener {
            abrirEnlace("https://guia.barcelona.cat/es/agenda")
        }



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
}