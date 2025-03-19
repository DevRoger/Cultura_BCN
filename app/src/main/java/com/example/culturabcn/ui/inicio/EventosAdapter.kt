package com.example.culturabcn.ui.inicio

import android.annotation.SuppressLint
import android.graphics.Outline
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.R
import com.example.culturabcn.clases.Evento
import java.text.SimpleDateFormat

class EventosAdapter(private val eventos: List<Evento>) :
    RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    // Variable para almacenar el índice del item desplegado
    private var lastExpandedPosition = -1

    // Metodo que infla el layout y lo pasa al ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_evento, parent, false
                                                              ) // Reemplaza item_evento con el nombre de tu layout
        return EventoViewHolder(view)
    }

    // Metodo que vincula los datos a las vistas del layout
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(
        holder: EventoViewHolder, @SuppressLint("RecyclerView") position: Int
                                 ) {
        val evento = eventos[position]

        // Formatear la fecha y la hora para que se vea correctamente
        val fechaFormat = SimpleDateFormat("dd/MM/yyyy")
        val horaFormat = SimpleDateFormat("HH:mm")

        holder.nombreEvento.text = evento.nombre
        holder.precioEvento.text = "${evento.precio}€"
        holder.fechaEvento.text = fechaFormat.format(evento.fecha)
        holder.horaEvento.text =
            "${horaFormat.format(evento.hora_inicio)} - ${horaFormat.format(evento.hora_fin)}"
        holder.descripcionEvento.text = evento.descripcion
        holder.aforoEvento.text = " ${evento.aforo} persones"
        holder.edadMinima.text = "${evento.edad_minima} anys"
        holder.lugarEvento.text = evento.lugar

        // Inicialmente, ocultar el desplegable si no es el último item expandido
        holder.itemDesplegable.visibility = if (position == lastExpandedPosition) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Configura el botón para realizar una acción
        holder.btnReservar.setOnClickListener {
            // Aquí puedes agregar la lógica de lo que debe hacer el botón
        }

        // Establecer el listener para mostrar u ocultar el desplegable
        holder.cardView.setOnClickListener {
            // Si el item clickeado es el que ya está expandido, lo ocultamos
            if (lastExpandedPosition == position) {
                holder.itemDesplegable.visibility = View.GONE
                lastExpandedPosition = -1
            } else {
                // Si hay otro item expandido, lo cerramos
                notifyItemChanged(lastExpandedPosition) // Esto notificará que el ítem previo debe colapsarse

                // Expandir el item actual
                holder.itemDesplegable.visibility = View.VISIBLE
                lastExpandedPosition = position
            }
        }

        holder.imgLogo.apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(
                        0,
                        0,
                        view.width,
                        view.height,
                        12f * resources.displayMetrics.density
                                        )
                }
            }
            clipToOutline = true
        }
    }

    override fun getItemCount(): Int {
        return eventos.size
    }

    // ViewHolder que hace referencia a las vistas dentro del layout
    class EventoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgLogo: ImageView = view.findViewById<ImageView>(R.id.imgLogo)
        val cardView: CardView = view.findViewById(R.id.cardViewEvento)
        val nombreEvento: TextView = view.findViewById(R.id.nombreEvento)
        val precioEvento: TextView = view.findViewById(R.id.precioEvento)
        val fechaEvento: TextView = view.findViewById(R.id.fechaEvento)
        val horaEvento: TextView = view.findViewById(R.id.horaEvento)
        val itemDesplegable: LinearLayout = view.findViewById(R.id.itemDesplegable)
        val descripcionEvento: TextView = view.findViewById(R.id.descripcionEvento)
        val aforoEvento: TextView = view.findViewById(R.id.aforoEvento)
        val edadMinima: TextView = view.findViewById(R.id.edadMinima)
        val lugarEvento: TextView = view.findViewById(R.id.lugarEvento)
        val btnReservar: Button = view.findViewById(R.id.btnReservar)
    }
}
