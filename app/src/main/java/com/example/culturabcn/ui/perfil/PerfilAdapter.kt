package com.example.culturabcn.ui.perfil

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.R
import com.example.culturabcn.clases.Evento
import java.text.SimpleDateFormat

class PerfilAdapter(private val eventos: List<Evento>) :
    RecyclerView.Adapter<PerfilAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_reserva, parent, false
                                                              )
        return EventoViewHolder(view)
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val evento = eventos[position]

        // Formateadores de fecha y hora
        val fechaFormat = SimpleDateFormat("dd/MM/yyyy")
        val horaFormat = SimpleDateFormat("HH:mm")

        with(holder) {
            nombreEvento.text = evento.nombre
            txtDescripcion.text = evento.descripcion
            txtLugar.text = evento.lugar
            txtPrecio.text = "${evento.precio}€"
            txtFecha.text = fechaFormat.format(evento.fecha)
            txtHora.text =
                "${horaFormat.format(evento.hora_inicio)} - ${horaFormat.format(evento.hora_fin)}"

        }
    }

    override fun getItemCount(): Int = eventos.size

    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreEvento: TextView = view.findViewById(R.id.nombreEvento)
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcioEvento)
        val txtLugar: TextView = view.findViewById(R.id.txtLugarEvento)
        val txtPrecio: TextView = view.findViewById(R.id.txtPrecioEvento)
        val txtFecha: TextView = view.findViewById(R.id.txtFechaEvento)
        val txtHora: TextView = view.findViewById(R.id.txtHoraEvento)
    }
}