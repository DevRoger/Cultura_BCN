package com.example.culturabcn.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.MainActivity
import com.example.culturabcn.R

class LoginFragment: Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val txtRegistrar = view.findViewById<TextView>(R.id.txtRegistrar)
        val btnOlvidada = view.findViewById<TextView>(R.id.btnOlvidada)
        val btnIniciar = view.findViewById<Button>(R.id.btnIniciar)


        txtRegistrar.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 1
        }


        // BOTÓN PROVISIONAL PARA ACCEDER A LA RECUPERACIÓN
        btnOlvidada.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 4
        }

        btnIniciar.setOnClickListener {
            /*
            -------------------------------------------------------------------
            AQUI FALTA LA COMPROBACIÓN DEL USUARIO Y LA CONTRASEÑA INTRODUCIDOS
            -------------------------------------------------------------------
             */
            // Redirigir al usuario a la MainActivity
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)

            requireActivity().finish()
        }
    }
}