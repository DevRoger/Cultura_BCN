package com.example.culturabcn.ui.crear

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.culturabcn.R

class CrearFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View? {
        val view = inflater.inflate(R.layout.fragment_crear, container, false)


        // Configuramos el spinner de las salas
        val edtSala = view.findViewById<TextView>(R.id.edtSala) // Cambia Spinner por TextView
        val salasDisponibles = arrayOf(
            "Restaurante", "Bar", "Cafetería", "Discoteca", "Librería",
            "Tienda de ropa", "Supermercado", "Tienda de electrónica",
            "Floristería", "Salón de belleza", "Gimnasio", "Hotel",
            "Centro de eventos", "Otro"
                                       )

        // Al hacer clic, muestra un AlertDialog con las opciones
        edtSala.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Selecciona una sala")
            builder.setItems(salasDisponibles) { _, which ->
                edtSala.text = salasDisponibles[which]
            }
            builder.show()
        }

        return view
    }
}
