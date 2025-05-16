package com.example.culturabcn.ui.inicio

import android.annotation.SuppressLint
import android.graphics.Bitmap // *** Importa Bitmap ***
import android.graphics.BitmapFactory // *** Importa BitmapFactory ***
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
import com.example.culturabcn.clases.RutaImagenDto
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream // *** Importa InputStream ***
import java.io.IOException // *** Importa IOException ***


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

        val imageUrl = evento.foto_url // Ruta local del servidor

        Log.d("IMAGE_DEBUG", "Intentant carregar imatge per a l'esdeveniment ${evento.nombre}. Ruta local enviada a POST: ${imageUrl}")

        // Mostra una imatge de placeholder
        holder.imgLogo.setImageResource(R.drawable.side_nav_bar) // Utilitza el teu drawable de placeholder

        if (!imageUrl.isNullOrBlank()) {
            val rutaImagenDto = RutaImagenDto(Foto_url = imageUrl)

            RetrofitClient.apiService.postImagen(rutaImagenDto).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val eventNameForLog = evento.nombre
                    val receivedUrlForLog = imageUrl

                    Log.d("IMAGE_API_RESPONSE", "onResponse per a l'esdeveniment $eventNameForLog. Codi: ${response.code()}")

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        Log.d("IMAGE_API_RESPONSE", "postImagen exitós per a $eventNameForLog. Cos nul: ${responseBody == null}. Content-Type: ${response.headers().get("Content-Type")}. Content-Length: ${responseBody?.contentLength() ?: "N/A"}")

                        if (responseBody != null) {
                            // *** TEST DE DIAGNOSI: Intentar decodificar manualment a Bitmap ***
                            var inputStream: InputStream? = null // Declarar InputStream fora del try per al finally
                            try {
                                // Obtenim l'InputStream del cos de la resposta
                                inputStream = responseBody.byteStream()
                                // Intentem decodificar l'InputStream directament a un Bitmap
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                if (bitmap != null) {
                                    // *** Si la decodificació a Bitmap té èxit, establim el Bitmap a la ImageView ***
                                    // Aquí no utilitzem Glide per carregar el Bitmap, només el mostrem directament.
                                    holder.imgLogo.setImageBitmap(bitmap)
                                    Log.d("IMAGE_LOAD_SUCCESS", "Imatge decodificada a Bitmap i mostrada amb èxit per a l'esdeveniment $eventNameForLog.")
                                } else {
                                    // *** Si BitmapFactory.decodeStream retorna nul, les dades no són un Bitmap vàlid ***
                                    // Això passa si les dades no corresponen a un format d'imatge suportat o estan corruptes.
                                    Log.e("IMAGE_LOAD_ERROR", "BitmapFactory.decodeStream va retornar nul per a l'esdeveniment $eventNameForLog. Probablement dades d'imatge invàlides. Ruta: $receivedUrlForLog")
                                    holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                                }

                            } catch (e: IOException) {
                                // Captura errors d'entrada/sortida en llegir el stream
                                Log.e("IMAGE_LOAD_ERROR", "Error de IO en llegir el stream de la imatge per a l'esdeveniment $eventNameForLog. Ruta: $receivedUrlForLog", e)
                                holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                            } catch (e: Exception) {
                                // Captura altres possibles excepcions durant la decodificació
                                Log.e("IMAGE_LOAD_ERROR", "Excepció inesperada processant la imatge per a l'esdeveniment $eventNameForLog. Ruta: $receivedUrlForLog", e)
                                holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                            } finally {
                                // *** MOLT IMPORTANT: Tancar l'InputStream ***
                                try {
                                    inputStream?.close()
                                } catch (e: IOException) {
                                    Log.e("IMAGE_LOAD_ERROR", "Error tancant l'stream de la imatge per a l'esdeveniment $eventNameForLog", e)
                                }
                            }

                        } else {
                            // Resposta exitosa (codi 2xx) però cos nul
                            Log.e("IMAGE_API_RESPONSE", "postImagen exitós però cos de resposta nul per a $eventNameForLog. Ruta: $receivedUrlForLog")
                            holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                        }
                    } else {
                        // Crida API no exitosa (codi d'error)
                        val statusCode = response.code()
                        val errorBody = response.errorBody()?.string()
                        Log.e("IMAGE_API_RESPONSE", "postImagen: Error API per a $eventNameForLog. Codi: ${statusCode}. Cos d'error: ${errorBody}. Ruta enviada: $receivedUrlForLog")
                        holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    val eventNameForLog = evento.nombre
                    val receivedUrlForLog = imageUrl
                    Log.e("IMAGE_API_FAILURE", "postImagen: Fallo de connexió a la API per a ${eventNameForLog}. Ruta enviada: ${receivedUrlForLog}. Error: ${t.message}", t)
                    holder.imgLogo.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                }
            })
        } else {
            // foto_url és nul·la o buida
            Log.d("IMAGE_DEBUG", "foto_url és nul·la o buida per a l'esdeveniment ${evento.nombre}. No es fa crida API.")
            holder.imgLogo.setImageResource(R.drawable.ic_menu_gallery) // Drawable per defecte
        }

        // *** La crida API getAsientosCount DINS DE onBindViewHolder (ineficient, però aquí per ara) ***
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
        val btnReservar: Button = view.findViewById<Button>(R.id.btnReservar)
    }

    // Metodo para actualizar la lista de eventos
    fun updateData(newList: List<Evento>) {
        eventos = newList
        lastExpandedPosition = -1
        notifyDataSetChanged()
    }
}