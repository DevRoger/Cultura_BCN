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
import com.example.culturabcn.clases.Usuario
import com.example.culturabcn.ui.inicio.EventosAdapter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
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
            if (UserLogged.rolId == 1 && !::usuarioC.isInitialized) {
                Toast.makeText(requireContext(), "Cargando datos de usuario, intente de nuevo.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (UserLogged.rolId != 1 && !::usuarioG.isInitialized) {
                Toast.makeText(requireContext(), "Cargando datos de usuario, intente de nuevo.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showEditDialog(requireContext())
        }

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

        val currentUser = if (UserLogged.rolId == 1 ) usuarioC else usuarioG // Obtenir l'usuari actual carregat

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

            // 1. Recollir les dades dels camps editables
            val updatedNombre = edtNombre.text.toString().trim()
            val updatedApellidos = edtApellido.text.toString().trim()
            val updatedCorreo = edtCorreo.text.toString().trim()
            val updatedTelefono = edtTelefono.text.toString().trim()
            val updatedFechaNacimiento = edtFechaNacimiento.text.toString().trim() // Assegura't del format ("yyyy-MM-dd")

            // TODO: Pots afegir validacions bàsiques per a aquests camps actualitzats si cal.

            // 2. Obtenir les dades no editables necessàries per a la petició PUT (de l'objecte currentUser)
            val userId = UserLogged.userId // La ID de l'usuari (del UserLogged)
            val userRolId = UserLogged.rolId // La ID del rol (del UserLogged)
            val currentContrasenaHash = currentUser.contrasenaHash // El hash de contrasenya actual (l'API el demana)
            val currentFotoUrl = currentUser.foto // La foto_url actual (l'API la demana)

            // Verifica que les dades crítiques no siguin nul·les abans de fer la crida
            if (userId == null || userRolId == null || currentContrasenaHash.isNullOrBlank() || currentFotoUrl == null) {
                Toast.makeText(context, "Error: Dades d'usuari incompletes per actualitzar.", Toast.LENGTH_SHORT).show()
                Log.e("PerfilFragment", "Dades crítiques de l'usuari nul·les: userId=$userId, rolId=$userRolId, hashNul=${currentContrasenaHash.isNullOrBlank()}, fotoUrlNul=${currentFotoUrl == null}")
                return@setOnClickListener // Surt si falten dades
            }


            // 3. Preparar la part del fitxer "photo" (enviem la foto per defecte)
            // L'API exigeix un fitxer no buit anomenat "photo". Com que no es pot canviar la foto al diàleg,
            // enviem el contingut d'una imatge per defecte.
            val defaultDrawableId = R.drawable.side_nav_bar // *** SUBSTITUEIX amb la teva imatge per defecte per a edició si és diferent ***
            val photoFile: File? = getFileFromDrawable(context, defaultDrawableId, "default_profile_photo_edit_${userId}.png") // Nom del fitxer temporal (inclou ID usuari per unicitat)

            if (photoFile == null || !photoFile.exists()) {
                Toast.makeText(context, "Error intern: No es pot obtenir la imatge per defecte per a l'actualització.", Toast.LENGTH_SHORT).show()
                Log.e("PerfilFragment", "No es va poder obtenir el fitxer de la foto per defecte per a l'edició.")
                return@setOnClickListener // Surt si no es pot crear el fitxer per defecte
            }
            Log.d("PerfilFragment", "Fitxer de foto per defecte a enviar per a edició: ${photoFile.name}")


            // 4. Preparar les PARTS de la petició multipart (camps de text i fitxer)
            // *** IMPORTANT: Els noms de les parts han de coincidir EXACTAMENT amb el que l'API llegeix de Request.Form ***
            val idUsuarioPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val nombrePart = updatedNombre.toRequestBody("text/plain".toMediaTypeOrNull())
            val apellidosPart = updatedApellidos.toRequestBody("text/plain".toMediaTypeOrNull())
            val correoPart = updatedCorreo.toRequestBody("text/plain".toMediaTypeOrNull())
            // Enviem la URL de la foto actual de l'usuari al camp foto_url
            val fotoUrlPart = currentFotoUrl.toRequestBody("text/plain".toMediaTypeOrNull())
            // Enviem el hash de contrasenya actual de l'usuari al camp contrasena_hash
            val contrasenaHashPart = currentContrasenaHash.toRequestBody("text/plain".toMediaTypeOrNull())
            val fechaNacimientoPart = updatedFechaNacimiento.toRequestBody("text/plain".toMediaTypeOrNull())
            val telefonoPart = updatedTelefono.toRequestBody("text/plain".toMediaTypeOrNull())
            val idRolPart = userRolId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            // Prepara la part del fitxer "photo". El nom "photo" ha de coincidir amb el de l'API.
            val photoRequestBody = photoFile.asRequestBody("image/png".toMediaTypeOrNull()) // Utilitza el tipus MIME correcte
            val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody) // Nom de la part "photo"


            // 5. Fer la crida a l'API PUT
            RetrofitClient.apiService.putUsuario(
                idUsuarioPart,
                nombrePart,
                apellidosPart,
                correoPart,
                fotoUrlPart, // Enviem la URL de la foto actual
                contrasenaHashPart, // Enviem el hash actual
                fechaNacimientoPart,
                telefonoPart,
                idRolPart,
                photoPart // Enviem la foto per defecte com a contingut
                                                ).enqueue(object : Callback<Boolean> { // L'API retorna boolean (true si Ok)

                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    // S'executa al fil principal (UI thread)
                    dialog.dismiss() // Tanca el diàleg un cop rebuda la resposta

                    // *** Netejar el fitxer temporal creat ***
                    photoFile.delete()
                    Log.d("PerfilFragment", "Fitxer temporal per a edició eliminat després de la petició PUT.")


                    if (response.isSuccessful && response.body() == true) {
                        // La petició PUT va tenir èxit (codi 2xx i body true)
                        Log.d("PerfilFragment", "Perfil d'usuari actualitzat amb èxit a l'API.")
                        Toast.makeText(context, "Perfil actualitzat!", Toast.LENGTH_SHORT).show()

                        // *** CRIDAR a refreshProfileData per tornar a carregar les dades i actualitzar la UI ***
                        val currentUserId = UserLogged.userId
                        val currentUserRolId = UserLogged.rolId
                        if (currentUserId != null && currentUserRolId != null) {
                            refreshProfileData(currentUserId, currentUserRolId) // Torna a carregar les dades i refresca la UI
                        } else {
                            Log.e("PerfilFragment", "UserLogged userId o rolId és nul després d'actualització exitosa.")
                            // Potser mostrar un missatge a l'usuari
                        }
                    } else {
                        // La petició va fallar (no 2xx o body no és true)
                        val statusCode = response.code()
                        val errorBody = response.errorBody()?.string()
                        Log.e("PerfilFragment", "Error actualitzant perfil. Codi: $statusCode, Error: $errorBody")
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    // Fallada a nivell de xarxa o excepció
                    dialog.dismiss() // Tanca el diàleg
                    photoFile.delete() // Neteja el fitxer temporal
                    Log.e("PerfilFragment", "Error de connexió actualitzant perfil", t)
                    Toast.makeText(context, "Error de connexió actualitzant perfil.", Toast.LENGTH_SHORT).show()
                }
            })

        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        // Muestra el diálogo
        dialog.show()
    }

    fun getFileFromDrawable(context: Context, drawableId: Int, fileName: String): File? {
        try {
            val resources = context.resources
            val inputStream: InputStream = resources.openRawResource(drawableId)

            val file = File(context.cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            return file

        } catch (e: Exception) {
            Log.e("FileHelper", "Error creant fitxer des de drawable ${drawableId}: ${e.message}", e)
            return null
        }
    }

    private fun refreshProfileData(userId: Int, userRolId: Int) {
        // Opcional: Mostra un indicador de càrrega (Progress Bar)
        // showLoadingIndicator(true)

        if (userRolId == 1) { // Si l'usuari actual és un Client (Rol 1)
            RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                override fun onResponse(call: Call<List<Cliente>>, response: Response<List<Cliente>>) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)

                    if (response.isSuccessful) {
                        val clientes = response.body()
                        // Troba l'usuari amb la ID correcta a la llista rebuda
                        val usuarioIniciado = clientes?.find { it.id == userId}

                        if (usuarioIniciado != null) {
                            // *** Dades de Client rebudes. Guarda-les i actualitza la UI. ***
                            usuarioC = usuarioIniciado // Guarda l'objecte Client a la variable del Fragment
                            updateProfileUI(usuarioC!!) // Actualitza la UI amb les dades del Client (ús !! ja que acabem de comprovar que no és nul)

                        } else {
                            // Usuari no trobat a la llista rebuda (molt improbable després d'una edició exitosa)
                            Log.e("PerfilFragment", "Refetch: Usuari (Client) amb ID $userId no trobat a la llista API.")
                            Toast.makeText(requireContext(), "Error al carregar dades actualitzades.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Error a la resposta de l'API
                        Log.e("PerfilFragment", "Refetch: Error API carregar Clients. Codi: ${response.code()}, Cos: ${response.errorBody()?.string()}")
                        Toast.makeText(requireContext(), "Error al carregar dades del perfil.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)
                    // Fallada a nivell de xarxa
                    Log.e("PerfilFragment", "Refetch: Error xarxa carregar Clients: ${t.message}", t)
                    Toast.makeText(requireContext(), "Error de connexió al carregar perfil.", Toast.LENGTH_SHORT).show()
                }
            })
        } else { // Si l'usuari actual és un Gestor (Rol 2)
            RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                override fun onResponse(call: Call<List<Gestor>>, response: Response<List<Gestor>>) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)

                    if (response.isSuccessful) {
                        val gestores = response.body()
                        // Troba el gestor amb la ID correcta a la llista rebuda
                        val usuarioIniciado = gestores?.find { it.id == userId}

                        if (usuarioIniciado != null) {
                            // *** Dades de Gestor rebudes. Guarda-les i actualitza la UI. ***
                            usuarioG = usuarioIniciado // Guarda l'objecte Gestor a la variable del Fragment
                            updateProfileUI(usuarioG!!) // Actualitza la UI amb les dades del Gestor (ús !! ja que acabem de comprovar que no és nul)

                        } else {
                            // Gestor no trobat a la llista rebuda
                            Log.e("PerfilFragment", "Refetch: Usuari (Gestor) amb ID $userId no trobat a la llista API.")
                            Toast.makeText(requireContext(), "Error al carregar dades actualitzades.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // Error a la resposta de l'API
                        Log.e("PerfilFragment", "Refetch: Error API carregar Gestors. Codi: ${response.code()}, Cos: ${response.errorBody()?.string()}")
                        Toast.makeText(requireContext(), "Error al carregar dades del perfil.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)
                    // Fallada a nivell de xarxa
                    Log.e("PerfilFragment", "Refetch: Error xarxa carregar Gestors: ${t.message}", t)
                    Toast.makeText(requireContext(), "Error de connexió al carregar perfil.", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    @SuppressLint("SetTextI18n") // Permet concatenació de text per simplicitat
    private fun updateProfileUI(user: Usuario) {
        // Utilitza view? per accés segur a la vista del fragment
        view?.let { // Assegura't que la vista no és nul·la (per si es crida després de destruir la vista)
            val txtNombreUsuario = it.findViewById<TextView>(R.id.txtNombreUsuario)
            val txtFechaNacimiento = it.findViewById<TextView>(R.id.txtFechaNacimiento)
            val txtEdad = it.findViewById<TextView>(R.id.txtEdad)
            val txtCorreo = it.findViewById<TextView>(R.id.txtCorreo)
            val txtTelefono = it.findViewById<TextView>(R.id.txtTelefono)
            val txtGestor = it.findViewById<TextView>(R.id.txtGestor) // Label de Gestor

            // Actualitza els TextViews amb les dades de l'usuari
            txtNombreUsuario.text = user.nombre + " " + user.apellidos
            // Assegura't que el format de data sigui correcte
            txtFechaNacimiento.text = user.fechaNacimiento.replace("T00:00:00", "")
            txtEdad.text = user.edad.toString()
            txtCorreo.text = user.correo
            txtTelefono.text = user.telefono

            // Controla la visibilitat del label de Gestor segons el tipus d'usuari
            if (user is Gestor) { // Comprova si l'objecte és de tipus Gestor
                txtGestor.visibility = View.VISIBLE // Fer visible si és Gestor
            } else { // Assumeix que és un Client (o un altre tipus que no sigui Gestor)
                txtGestor.visibility = View.GONE // Amagar si no és Gestor
            }
        }
    }
}