package com.example.culturabcn.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.R
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.ui.inicio.EventosAdapter
import java.sql.Date
import java.sql.Time

class PerfilFragment : Fragment() {

    // Metodo onCreateView para inflar el layout del fragmento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        // Inflamos el layout para este fragmento
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val eventos = mutableListOf(
            Evento(
                id_evento = 1,
                nombre = "Concierto de Rock",
                fecha = Date(2025, 3, 25), // 25 de marzo de 2025
                hora_inicio = Time(20, 0, 0), // 8:00 PM
                hora_fin = Time(22, 0, 0), // 10:00 PM
                descripcion = "Un concierto de rock en vivo con bandas locales.",
                lugar = "Estadio Nacional",
                aforo = 5000,
                enumerada = false,
                precio = 50.0f,
                edad_minima = 18
                  ),
            Evento(
                id_evento = 2,
                nombre = "Feria de Ciencia",
                fecha = Date(2025, 4, 10), // 10 de abril de 2025
                hora_inicio = Time(9, 0, 0), // 9:00 AM
                hora_fin = Time(17, 0, 0), // 5:00 PM
                descripcion = "Una feria para mostrar proyectos científicos y tecnológicos.",
                lugar = "Centro de Convenciones",
                aforo = 2000,
                enumerada = false,
                precio = 10.0f,
                edad_minima = 12
                  ),
            Evento(
                id_evento = 3,
                nombre = "Exposición de Arte",
                fecha = Date(2025, 5, 15), // 15 de mayo de 2025
                hora_inicio = Time(11, 0, 0), // 11:00 AM
                hora_fin = Time(18, 0, 0), // 6:00 PM
                descripcion = "Exposición de pintura contemporánea con artistas locales.",
                lugar = "Museo de Arte Moderno",
                aforo = 300,
                enumerada = false,
                precio = 20.0f,
                edad_minima = 16
                  ),
            Evento(
                id_evento = 4,
                nombre = "Maratón Nocturno",
                fecha = Date(2025, 6, 1), // 1 de junio de 2025
                hora_inicio = Time(20, 30, 0), // 8:30 PM
                hora_fin = Time(23, 30, 0), // 11:30 PM
                descripcion = "Una carrera nocturna por las calles de la ciudad.",
                lugar = "Parque Central",
                aforo = 2000,
                enumerada = false,
                precio = 25.0f,
                edad_minima = 18
                  ),
            Evento(
                id_evento = 5,
                nombre = "Festival Gastronómico",
                fecha = Date(2025, 7, 20), // 20 de julio de 2025
                hora_inicio = Time(12, 0, 0), // 12:00 PM
                hora_fin = Time(20, 0, 0), // 8:00 PM
                descripcion = "Un festival con comida de todo el mundo y actividades culturales.",
                lugar = "Plaza Mayor",
                aforo = 10000,
                enumerada = false,
                precio = 15.0f,
                edad_minima = 12
                  )
                                   )


        // Configurar el RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewReservas)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = PerfilAdapter(eventos)
    }
}