package com.example.culturabcn.ui.inicio

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Chat
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.UserLogged
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.sql.Time

class InicioFragment : Fragment() {

    // Declara el RecyclerView y el Adapter a nivel del Fragment
    private lateinit var recyclerView: RecyclerView
    private lateinit var eventosAdapter: EventosAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        // Inflamos el layout para este fragmento
        return inflater.inflate(R.layout.fragment_inicio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --------------------------------------------------
        // Configurar el RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewEventos)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Inicializa el adapter con una lista vacía
        eventosAdapter = EventosAdapter(emptyList()) { clickedEvento ->
            // Aquesta lògica s'executarà quan es cliqui el botó "Reservar" en un element
            Log.d("InicioFragment", "Botón Reservar clicado para: ${clickedEvento.nombre}")

            // 1. Convertir el objeto Evento a JSON string
            val eventoJson = Gson().toJson(clickedEvento)

            // 2. Crear un Bundle para pasar los argumentos
            val bundle = Bundle().apply {
                putString("evento", eventoJson)
            }

            // 3. Navegar al EventoDetallesFragment con los argumentos
            findNavController().navigate(
                R.id.eventoDetallesFragment,
                bundle
                                        )
        }
        recyclerView.adapter = eventosAdapter
        // --------------------------------------------------

        // Ahora llama a la función para obtener los eventos de la API
        // Los datos se actualizarán en el callback onResponse
        getEventosFromApi()


        val btnAnadirEvento = view.findViewById<ImageView>(R.id.btnAnadirEvento)

        if (UserLogged.rolId == 2) {
            btnAnadirEvento.visibility = View.VISIBLE
        } else {
            btnAnadirEvento.visibility = View.GONE
        }

        btnAnadirEvento.setOnClickListener {
            findNavController().navigate(R.id.crearFragment)

        }

    }

    // Función para obtener los eventos
    private fun getEventosFromApi() {
        RetrofitClient.apiService.getEventos().enqueue(object : Callback<List<Evento>> {
            override fun onResponse(call: Call<List<Evento>>, response: Response<List<Evento>>) {
                if (response.isSuccessful) {
                    val eventos: List<Evento>? = response.body()
                    if (eventos != null) {
                        // Aquí tienes la lista de eventos (List<Evento>)
                        Log.d("API_CALL", "Eventos recibidos: ${eventos.size}")

                        // ¡Actualiza los datos del adapter con la lista recibida!
                        eventosAdapter.updateData(eventos)

                    } else {
                        // El cuerpo de la respuesta es nulo, puede indicar un error en el servidor o no hay datos
                        Toast.makeText(requireContext(), "No se recibieron eventos", Toast.LENGTH_SHORT).show()
                        Log.e("API_CALL", "Respuesta exitosa pero cuerpo nulo")
                    }
                } else {
                    // La respuesta no fue exitosa (código de error HTTP como 404, 500, etc.)
                    Toast.makeText(requireContext(), "Error al obtener eventos: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("API_CALL", "Error en la respuesta de la API: ${response.code()}")
                    // Puedes obtener más detalles del error: response.errorBody()
                }
            }

            override fun onFailure(call: Call<List<Evento>>, t: Throwable) {
                // Fallo en la comunicación (problemas de red, URL incorrecta, etc.)
                Toast.makeText(requireContext(), "Fallo al conectar con la API", Toast.LENGTH_SHORT).show()
                Log.e("API_CALL", "Fallo al obtener eventos", t)
            }
        })
    }
}