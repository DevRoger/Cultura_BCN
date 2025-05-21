package com.example.culturabcn.ui.inicio

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.RutaImagenDto
import com.example.culturabcn.clases.UserLogged
import com.google.gson.Gson
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream
import java.text.DecimalFormat

class EventoDetallesFragment : Fragment() {
    companion object {
    private const val MAX_ENTRADAS = 6
        }


    // UI elements from your XML
    private lateinit var imgEventoDetalle: ImageView
    private lateinit var tvNombreDetalle: TextView
    private lateinit var tvDescripcionDetalle: TextView
    private lateinit var tvFechaDetalle: TextView
    private lateinit var tvHoraDetalle: TextView
    private lateinit var tvLugarDetalle: TextView
    private lateinit var tvPrecioDetalle: TextView
    private lateinit var tvEdadMinimaDetalle: TextView
    private lateinit var tvAforoDetalle: TextView
    private lateinit var btnReservarDetalle: Button

    // UI elements for ticket quantity
    private lateinit var layoutNumeroEntradas: LinearLayout
    private lateinit var btnRestarEntrada: Button
    private lateinit var tvCantidadEntradas: TextView
    private lateinit var btnSumarEntrada: Button
    private lateinit var tvTotalPrecio: TextView

    // State variables
    private var currentEvento: Evento? = null
    private var cantidadEntradas: Int = 1 // Default to 1 ticket

    private val priceFormat = DecimalFormat("0.00")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        return inflater.inflate(R.layout.fragment_evento_detalles, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        imgEventoDetalle = view.findViewById(R.id.imgEventoDetalle)
        tvNombreDetalle = view.findViewById(R.id.tvNombreDetalle)
        tvDescripcionDetalle = view.findViewById(R.id.tvDescripcionDetalle)
        tvFechaDetalle = view.findViewById(R.id.tvFechaDetalle)
        tvHoraDetalle = view.findViewById(R.id.tvHoraDetalle)
        tvLugarDetalle = view.findViewById(R.id.tvLugarDetalle)
        tvPrecioDetalle = view.findViewById(R.id.tvPrecioDetalle)
        tvEdadMinimaDetalle = view.findViewById(R.id.tvEdadMinimaDetalle)
        tvAforoDetalle = view.findViewById(R.id.tvAforoDetalle)
        btnReservarDetalle = view.findViewById(R.id.btnReservarDetalle)

        layoutNumeroEntradas = view.findViewById(R.id.layoutNumeroEntradas)
        btnRestarEntrada = view.findViewById(R.id.btnRestarEntrada)
        tvCantidadEntradas = view.findViewById(R.id.tvCantidadEntradas)
        btnSumarEntrada = view.findViewById(R.id.btnSumarEntrada)
        tvTotalPrecio = view.findViewById(R.id.tvTotalPrecio)

        // Retrieve event data from arguments
        arguments?.let {
            val eventoJson = it.getString("evento")
            if (eventoJson != null) {
                val evento: Evento = Gson().fromJson(eventoJson, Evento::class.java)
                currentEvento = evento
                displayEventoDetails(evento)
            } else {
                Toast.makeText(requireContext(), "Error al cargar detalles del evento.", Toast.LENGTH_SHORT).show()
                Log.e("EventoDetallesFragment", "No se recibió JSON de evento en los argumentos.")
            }
        }

        // --- Event Listeners for Ticket Quantity ---
        btnRestarEntrada.setOnClickListener {
            if (cantidadEntradas > 1) {
                cantidadEntradas--
                updateCantidadEntradasUI()
            }
        }

        btnSumarEntrada.setOnClickListener {
            // We only care about our hardcoded MAX_ENTRADAS limit
            if (cantidadEntradas < MAX_ENTRADAS) {
                cantidadEntradas++
                updateCantidadEntradasUI()
            } else {
                // If the quantity already reached MAX_ENTRADAS, show the message
                Toast.makeText(
                    requireContext(),
                    "Solo puedes seleccionar un máximo de ${MAX_ENTRADAS} entradas.",
                    Toast.LENGTH_SHORT
                              ).show()
            }

        }

        // --- Main Reserve Button Listener (only for non-enumerated tickets) ---
        btnReservarDetalle.setOnClickListener {
            Toast.makeText(requireContext(), "Se han reservado $cantidadEntradas entradas!", Toast.LENGTH_SHORT).show()
        }
    } // End of onViewCreated

    // --- Display Event Details ---
    private fun displayEventoDetails(evento: Evento) {
        tvNombreDetalle.text = evento.nombre
        tvDescripcionDetalle.text = evento.descripcion
        tvFechaDetalle.text = "Fecha: ${evento.fecha.replace("T00:00:00", "")}"
        tvHoraDetalle.text = "Hora: ${evento.hora_inicio.replace(":00.0000000", "")} - ${evento.hora_fin.replace(":00.0000000", "")}"
        tvLugarDetalle.text = "Lugar: ${evento.lugar}"
        tvPrecioDetalle.text = "Precio: ${priceFormat.format(evento.precio)}€"
        tvEdadMinimaDetalle.text = "Edad mínima: ${evento.edad_minima} años"
        tvAforoDetalle.text = "Aforo: ${evento.aforo ?: "?"} personas" // Display aforo directly

        loadImage(evento.foto_url)

        // Always show the ticket quantity layout since we're focusing on non-enumerated
        layoutNumeroEntradas.visibility = View.VISIBLE

        updateCantidadEntradasUI() // Initialize UI for ticket quantity
    }

    // --- Image Loading ---
    private fun loadImage(imageUrl: String?) {
        if (!imageUrl.isNullOrBlank()) {
            val rutaImagenDto = RutaImagenDto(Foto_url = imageUrl)
            RetrofitClient.apiService.postImagen(rutaImagenDto).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            var inputStream: InputStream? = null
                            try {
                                inputStream = responseBody.byteStream()
                                val bitmap = BitmapFactory.decodeStream(inputStream)
                                imgEventoDetalle.setImageBitmap(bitmap)
                            } catch (e: IOException) {
                                Log.e("EventoDetalles", "Error de IO al cargar imagen: ${e.message}")
                                imgEventoDetalle.setImageResource(R.drawable.side_nav_bar)
                            } finally {
                                try { inputStream?.close() } catch (e: IOException) { /* Ignorar */ }
                            }
                        } else {
                            imgEventoDetalle.setImageResource(R.drawable.side_nav_bar)
                        }
                    } else {
                        Log.e("EventoDetalles", "Error API al cargar imagen: ${response.code()}")
                        imgEventoDetalle.setImageResource(R.drawable.side_nav_bar)
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e("EventoDetalles", "Fallo de conexión al cargar imagen: ${t.message}")
                    imgEventoDetalle.setImageResource(R.drawable.side_nav_bar)
                }
            })
        } else {
            imgEventoDetalle.setImageResource(R.drawable.side_nav_bar)
        }
    }

    // --- Non-Enumerated Ticket Quantity Logic ---
    private fun updateCantidadEntradasUI() {
        tvCantidadEntradas.text = cantidadEntradas.toString()
        currentEvento?.let {
            val total = cantidadEntradas * it.precio
            tvTotalPrecio.text = "Total: ${priceFormat.format(total)}€"
        }
    }
}