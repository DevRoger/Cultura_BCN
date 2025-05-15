package com.example.culturabcn.ui.perfil

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.MainActivity
import com.example.culturabcn.R
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.UserLogged
import com.example.culturabcn.ui.inicio.EventosAdapter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.sql.Time
import java.util.Calendar

class PerfilFragment : Fragment() {
    private lateinit var usuarioC: Cliente
    private lateinit var usuarioG: Gestor
    private lateinit var reservasAdapter: EventosAdapter


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


        // Recibimos el cliente si es cliente
        if (UserLogged.rolId == 1) {
            RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<List<Cliente>>,
                    response: Response<List<Cliente>>
                                       ) {
                    if (response.isSuccessful) {
                        val clientes = response.body()
                        val usuarioIniciado =
                            clientes?.find { it.id == UserLogged.userId}

                        if (usuarioIniciado != null) {

                            usuarioC = usuarioIniciado
                            val txtNombreUsuario = view.findViewById<TextView>(R.id.txtNombreUsuario)
                            val txtFechaNacimiento = view.findViewById<TextView>(R.id.txtFechaNacimiento)
                            val txtEdad = view.findViewById<TextView>(R.id.txtEdad)
                            val txtCorreo = view.findViewById<TextView>(R.id.txtCorreo)
                            val txtTelefono = view.findViewById<TextView>(R.id.txtTelefono)
                            val txtGestor = view.findViewById<TextView>(R.id.txtGestor)

                            txtNombreUsuario.text = usuarioIniciado.nombre + " " + usuarioIniciado.apellidos
                            txtFechaNacimiento.text = usuarioIniciado.fechaNacimiento.replace("T00:00:00", "")
                            txtEdad.text = usuarioIniciado.edad.toString()
                            txtCorreo.text = usuarioIniciado.correo
                            txtTelefono.text = usuarioIniciado.telefono
                            txtGestor.visibility = View.GONE

                        } else {
                            Log.e(
                                "PerfilFragment",
                                "Error al recibir el usuario: ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.e(
                            "PerfilFragment",
                            "Error en la respuesta: ${response.errorBody()?.string()}"
                             )
                    }
                }

                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                    Log.e("PerfilFragment", "Error de red: ${t.message}")
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Si el usuario es gestor lo recibimos aqui
            RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                override fun onResponse(call: Call<List<Gestor>>, response: Response<List<Gestor>>) {
                    if (response.isSuccessful) {
                        val clientes = response.body()
                        val usuarioIniciado =
                            clientes?.find { it.id == UserLogged.userId}

                        if (usuarioIniciado != null) {

                            usuarioG = usuarioIniciado

                            val txtNombreUsuario = view.findViewById<TextView>(R.id.txtNombreUsuario)
                            val txtFechaNacimiento = view.findViewById<TextView>(R.id.txtFechaNacimiento)
                            val txtEdad = view.findViewById<TextView>(R.id.txtEdad)
                            val txtCorreo = view.findViewById<TextView>(R.id.txtCorreo)
                            val txtTelefono = view.findViewById<TextView>(R.id.txtTelefono)
                            val txtGestor = view.findViewById<TextView>(R.id.txtGestor)

                            txtNombreUsuario.text = usuarioIniciado.nombre + " " + usuarioIniciado.apellidos
                            txtFechaNacimiento.text = usuarioIniciado.fechaNacimiento.replace("T00:00:00", "")
                            txtEdad.text = usuarioIniciado.edad.toString()
                            txtCorreo.text = usuarioIniciado.correo
                            txtTelefono.text = usuarioIniciado.telefono
                            txtGestor.visibility = View.VISIBLE

                        } else {
                            Log.e(
                                "PerfilFragment",
                                "Error al recibir el usuario: ${response.errorBody()?.string()}")
                        }
                    } else {
                        Log.e(
                            "PerfilFragment",
                            "Error en la respuesta: ${response.errorBody()?.string()}"
                             )
                    }
                }

                override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                    Log.e("PerfilFragment", "Error de red: ${t.message}")
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }

        val btnEditar = view.findViewById<Button>(R.id.btnEditar)

        btnEditar.setOnClickListener {
            showEditDialog(requireContext())
        }


        // ________________________________________

        // Aqui quiero recibir los eventos del usuario con su id y pasarlos por el recyclerView


        // Configurar el RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerViewReservas)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        reservasAdapter = EventosAdapter(emptyList())
        recyclerView.adapter = reservasAdapter


        val userId = UserLogged.userId


        RetrofitClient.apiService.getReservasPorUsuario(userId).enqueue(object : Callback<List<Evento>> {
            override fun onResponse(call: Call<List<Evento>>, response: Response<List<Evento>>) {
                if (response.isSuccessful) {
                    val reservedEvents: List<Evento>? = response.body()

                    if (reservedEvents != null) {
                        Log.d("PerfilFragment", "Eventos reservats per a l'usuari $userId rebuts: ${reservedEvents.size}")

                        reservasAdapter.updateData(reservedEvents)

                    } else {
                        Log.e("PerfilFragment", "Resposta exitosa per a reserves de l'usuari $userId, però cos nul.")
                    }
                } else {
                    val statusCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e("PerfilFragment", "Error al obtenir reserves per a l'usuari $userId. Codi: $statusCode, Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<List<Evento>>, t: Throwable) {
                Log.e("PerfilFragment", "Fallo de red al obtener reservas para el usuario $userId", t)
                Toast.makeText(requireContext(), "Error de conexión al cargar reservas.", Toast.LENGTH_SHORT).show()
            }
        })

    }


    // Mostrar dialogo cuando editamos el perfil
    @SuppressLint("MissingInflatedId")
    private fun showEditDialog(context: Context) {
        // Infla el diseño del diálogo
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edt_user, null)

        // Inicializa los EditText y el botón del diálogo
        val edtNombre = dialogView.findViewById<EditText>(R.id.edtNombre)
        val edtApellido = dialogView.findViewById<EditText>(R.id.edtApellidos)
        val edtCorreo = dialogView.findViewById<EditText>(R.id.edtCorreo)
        val edtTelefono = dialogView.findViewById<EditText>(R.id.edtTelefono)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)
        val edtFechaNacimiento = dialogView.findViewById<EditText>(R.id.edtFechaNacimiento)


        // Fecha
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        edtFechaNacimiento.setOnClickListener {
            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                edtFechaNacimiento.setText(selectedDate)
            }, year, month, day)
            datePickerDialog.show()
        }


        if (UserLogged.rolId == 1 ) {
            // Rellenar EditText
            edtNombre.setText(usuarioC.nombre)
            edtApellido.setText(usuarioC.apellidos)
            edtCorreo.setText(usuarioC.correo)
            edtTelefono.setText(usuarioC.telefono)
            edtFechaNacimiento.setText(usuarioC.fechaNacimiento.replace("T00:00:00", ""))
        } else {
            // Rellenar EditText
            edtNombre.setText(usuarioG.nombre)
            edtApellido.setText(usuarioG.apellidos)
            edtCorreo.setText(usuarioG.correo)
            edtTelefono.setText(usuarioG.telefono)
            edtFechaNacimiento.setText(usuarioG.fechaNacimiento.replace("T00:00:00", ""))
        }


        // Crea el AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)

        // Crea el AlertDialog
        val dialog = dialogBuilder.create()

        btnConfirmar.setOnClickListener {
            // Implementar clase para guardar los datos
            /*val musicoEditado = musico.valoracion?.let { valoracion ->
                DataTransferObjectUsuario(
                    id = musico.id,
                    nombre = edtNombre.text.toString(),
                    correo = edtCorreo.text.toString(),
                    contrasenya = musico.contrasenya,
                    telefono = edtTelefono.text.toString(),
                    latitud = 0.0,
                    longitud = 0.0,
                    fechaRegistro = null,
                    estado = true,
                    valoracion = valoracion,
                    tipo = "Musico",
                    apodo = edtApodo.text.toString(),
                    apellido = edtApellido.text.toString(),
                    genero = edtGenero.text.toString(),
                    edad = edadCalculada,
                    biografia = edtBiografia.text.toString(),
                    imagen = "",
                    idUsuario = musico.idUsuario,
                    generosMusicales = musico.generoMusical,
                    direccion = "",
                    tipo_local = "",
                    HorarioApertura = null,
                    HorarioCierre = null,
                    descripcion = ""
                                         )*/

        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        // Muestra el diálogo
        dialog.show()
    }
}