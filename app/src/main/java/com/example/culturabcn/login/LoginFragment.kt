package com.example.culturabcn.login

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.R

class LoginFragment: Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtRegistrar = view.findViewById<TextView>(R.id.txtRegistrar)
        val txtPrueba = view.findViewById<TextView>(R.id.txtPrueba)

        txtRegistrar.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 2
        }


        // BOTÓN PROVISIONAL PARA ACCEDER A LA RECUPERACIÓN
        txtPrueba.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 4
        }
    }
}