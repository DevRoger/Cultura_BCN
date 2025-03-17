package com.example.culturabcn.login

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.R

class RegistroFragment: Fragment(R.layout.fragment_registro) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgBack = view.findViewById<ImageView>(R.id.imgBack)
        val txtInicia = view.findViewById<TextView>(R.id.txtInicia)

        imgBack.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 1
        }

        txtInicia.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 1
        }
    }
}