package com.example.culturabcn.ui.inicio

import android.annotation.SuppressLint
import android.graphics.Outline
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.RutaImagenDto // Importa RutaImagenDto
import okhttp3.ResponseBody // Importa ResponseBody
import retrofit2.Call // Importa Call
import retrofit2.Callback // Importa Callback
import retrofit2.Response // Importa Response
// Importa si fas servir formats de data/hora
// import java.text.SimpleDateFormat


class EventosAdapter(private var eventos: List<Evento>) :
    RecyclerView.Adapter<EventosAdapter.EventoViewHolder>() {

    // Variable para almacenar el índice del item desplegado
    private var lastExpandedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_evento, parent, false
                                                              )
        return EventoViewHolder(view)
    }

    // Metodo que vincula los datos a las vistas del layout
    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onBindViewHolder(
        holder: EventoViewHolder, @SuppressLint("RecyclerView") position: Int
                                 ) {
        val evento = eventos[position]

        val imageUrl = evento.foto_url // Aquesta és la ruta local del servidor (C:\...)

        // Log la URL que s'està intentant enviar a l'API POST
        Log.d("IMAGE_DEBUG", "Intentant carregar imatge per a l'esdeveniment ${evento.nombre}. Ruta local enviada a POST: ${imageUrl}")

        // Mostra una imatge de placeholder mentre es fa la crida API
        holder.imgLogo.setImageResource(R.drawable.side_nav_bar) // Utilitza el teu drawable de placeholder

        if (!imageUrl.isNullOrBlank()) {
            // Creem l'objecte que enviarem en el cos de la petició POST
            val rutaImagenDto = RutaImagenDto(Foto_url = imageUrl)

            // *** Fem la crida API per obtenir el contingut binari de la imatge ***
            RetrofitClient.apiService.postImagen(rutaImagenDto).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val eventNameForLog = evento.nombre // Per tenir el nom de l'esdeveniment en els logs
                    val receivedUrlForLog = imageUrl // Per tenir la URL rebuda en els logs

                    // *** LOGS DETALLATS DE LA RESPOSTA API ***
                    Log.d("IMAGE_API_RESPONSE", "onResponse per a l'esdeveniment $eventNameForLog. Codi: ${response.code()}")

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        // Log si el cos de la resposta és nul i les capçaleres de contingut
                        Log.d("IMAGE_API_RESPONSE", "postImagen exitós per a $eventNameForLog. Cos nul: ${responseBody == null}. Content-Type: ${response.headers().get("Content-Type")}. Content-Length: ${responseBody?.contentLength() ?: "N/A"}")

                        if (responseBody != null) {
                            // *** Si la resposta és exitosa i té cos, intentem carregar el contingut amb Glide ***
                            try {
                                // Utilitza Glide per carregar des de l'InputStream del ResponseBody
                                Glide.with(holder.itemView.context)
                                    .load(responseBody.byteStream())
                                    // Aplica opcions de Glide com placeholder i imatge d'error
                                    .apply(RequestOptions()
                                               .placeholder(R.drawable.side_nav_bar) // Placeholder
                                               .error(R.drawable.ic_menu_slideshow)) // Imatge si falla la càrrega des del contingut rebut
                                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Estratègia de cache
                                    .into(holder.imgLogo) // Carrega a la ImageView

                                Log.d("IMAGE_LOAD_SUCCESS", "Imatge carregada amb èxit per a l'esdeveniment $eventNameForLog des del contingut de l'API.")

                            } catch (e: Exception) {
                                // Captura qualsevol excepció que pugui ocórrer durant el processament del stream per part de Glide
                                Log.e("IMAGE_LOAD_ERROR", "Error processant el contingut de la imatge per a l'esdeveniment $eventNameForLog. Ruta: $receivedUrlForLog", e)
                                // Mostra la imatge d'error si hi ha un problema processant el contingut
                                holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                            }

                        } else {
                            // La resposta va ser exitosa (codi 2xx) però el cos és nul
                            Log.e("IMAGE_API_RESPONSE", "postImagen exitós però cos de resposta nul per a $eventNameForLog. Ruta: $receivedUrlForLog")
                            // Mostra la imatge d'error si el cos és nul tot i l'èxit
                            holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // O R.drawable.no_image_available
                        }
                    } else {
                        // La crida API no va ser exitosa (codi d'error 4xx o 5xx)
                        val statusCode = response.code()
                        val errorBody = response.errorBody()?.string()
                        Log.e("IMAGE_API_RESPONSE", "postImagen: Error API per a $eventNameForLog. Codi: ${statusCode}. Cos d'error: ${errorBody}. Ruta enviada: $receivedUrlForLog")
                        // Mostra la imatge d'error si la crida API retorna un codi d'error
                        holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // O R.drawable.no_image_available
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    val eventNameForLog = evento.nombre
                    val receivedUrlForLog = imageUrl
                    // *** LOGS DETALLATS DE LA FALLADA DE XARXA ***
                    Log.e("IMAGE_API_FAILURE", "postImagen: Fallo de connexió a la API per a ${eventNameForLog}. Ruta enviada: ${receivedUrlForLog}. Error: ${t.message}", t)
                    // Mostra la imatge d'error en cas de fallada de xarxa
                    holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // O R.drawable.no_image_available
                }
            })
        } else {
            // Si foto_url és nul·la o buida a l'objecte Evento
            Log.d("IMAGE_DEBUG", "foto_url és nul·la o buida per a l'esdeveniment ${evento.nombre}. No es fa crida API.")
            // Mostra la imatge per defecte immediatament
            holder.imgLogo.setImageResource(R.drawable.ic_menu_gallery) // Utilitza el teu drawable per defecte
        }

        // *** La crida API getAsientosCount DINS DE onBindViewHolder (ineficient, però aquí per ara) ***
        // Mantenim aquest codi per aïllar el problema de la imatge.
        RetrofitClient.apiService.getAsientosCount(evento.id_evento).enqueue(object : Callback<Int> {
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                if (response.isSuccessful) {
                    val seatCount: Int? = response.body()
                    if (seatCount != null) {
                        if (evento.enumerada) {
                            holder.aforoEvento.text = " $seatCount asientos"
                        } else {
                            holder.aforoEvento.text = " $seatCount persones"
                        }
                    } else {
                        Log.e("API_CALL", "getAsientosCount: Resposta exitosa per a l'esdeveniment ${evento.id_evento}, però cos de resposta nul.")
                        if (evento.enumerada) holder.aforoEvento.text = "? asientos" else holder.aforoEvento.text = "? persones"
                    }
                } else {
                    val statusCode = response.code()
                    Log.e("API_CALL", "getAsientosCount: La crida a l'API va fallar per a l'esdeveniment ${evento.id_evento}. Codi: $statusCode")
                    if (evento.enumerada) holder.aforoEvento.text = "? asientos" else holder.aforoEvento.text = "? persones"
                }
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Log.e("API_CALL", "getAsientosCount: Fallo de connexió a la API per a l'esdeveniment ${evento.id_evento}", t)
                if (evento.enumerada) holder.aforoEvento.text = "? asientos" else holder.aforoEvento.text = "? persones"
            }
        })
        // -----------------------------------------------------------------------------


        // ------------------- Vincular altres dades de l'esdeveniment (el teu codi existent) -------------------
        holder.nombreEvento.text = evento.nombre
        holder.precioEvento.text = "${evento.precio}€"
        holder.fechaEvento.text = evento.fecha.replace("T00:00:00", "")
        holder.horaEvento.text =
            "${evento.hora_inicio.replace(":00.0000000", "")} - ${evento.hora_fin.replace(":00.0000000", "")}"
        holder.descripcionEvento.text = evento.descripcion
        holder.edadMinima.text = "${evento.edad_minima} anys"
        holder.lugarEvento.text = evento.lugar


        // Inicialment, ocultar o mostrar el desplegable
        holder.itemDesplegable.visibility = if (position == lastExpandedPosition) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Listener del botó Reservar
        holder.btnReservar.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Reservar per a ${evento.nombre}", Toast.LENGTH_SHORT).show()
        }

        // Listener per expandir/colapsar l'item
        holder.cardView.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (lastExpandedPosition == currentPosition) {
                holder.itemDesplegable.visibility = View.GONE
                lastExpandedPosition = -1
            } else {
                if (lastExpandedPosition != -1) {
                    notifyItemChanged(lastExpandedPosition)
                }
                holder.itemDesplegable.visibility = View.VISIBLE
                lastExpandedPosition = currentPosition
            }
        }

        // Aplica les cantonades arrodonides a la imatge
        holder.imgLogo.apply {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(
                        0, 0, view.width, view.height,
                        12f * resources.displayMetrics.density
                                        )
                }
            }
            clipToOutline = true
        }
    } // Fi de onBindViewHolder


    override fun getItemCount(): Int {
        return eventos.size
    }

    // ViewHolder (sense canvis)
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
        val btnReservar: Button = view.findViewById<Button>(R.id.btnReservar) // Pot ser Button o MaterialButton
    }

    // Metodo para actualizar la lista de eventos
    fun updateData(newList: List<Evento>) {
        eventos = newList
        lastExpandedPosition = -1
        notifyDataSetChanged()
    }
}