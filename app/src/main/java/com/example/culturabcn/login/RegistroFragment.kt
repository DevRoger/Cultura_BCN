package com.example.culturabcn.login

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Usuario
import com.example.culturabcn.clases.UsuarioRegistrat
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
import java.util.Calendar

class RegistroFragment: Fragment(R.layout.fragment_registro) {

    // Elements de la Fase 1
    private lateinit var tvNombreLabel: TextView
    private lateinit var edtNombre: EditText
    private lateinit var tvApellidosLabel: TextView
    private lateinit var edtApellidos: EditText
    private lateinit var tvCorreoLabel: TextView
    private lateinit var edtCorreo: EditText
    private lateinit var btnRegistrarse1: Button


    // Elements de la Fase 2
    private lateinit var tvFechaNacimientoLabel: TextView
    private lateinit var edtFechaNacimiento: EditText
    private lateinit var tvTelefonoLabel: TextView
    private lateinit var edtTelefono: EditText
    private lateinit var tvContrasenyaLabel: TextView
    private lateinit var edtContrasenya: EditText
    private lateinit var btnRegistrarse2: Button

    private var nombre: String = ""
    private var apellidos: String = ""
    private var correo: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgBack = view.findViewById<ImageView>(R.id.imgBack)
        val txtInicia = view.findViewById<TextView>(R.id.txtInicia)

        imgBack.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 2
        }

        txtInicia.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 2
        }

        // 1. Obtenir referències als elements de la UI
        // Fase 1
        tvNombreLabel = view.findViewById(R.id.tvNombreLabel)
        edtNombre = view.findViewById(R.id.edtNombre)
        tvApellidosLabel = view.findViewById(R.id.tvApellidosLabel)
        edtApellidos = view.findViewById(R.id.edtApellidos)
        tvCorreoLabel = view.findViewById(R.id.tvCorreoLabel)
        edtCorreo = view.findViewById(R.id.edtCorreo)
        btnRegistrarse1 = view.findViewById(R.id.btnRegistrarse1)

        // Fase 2
        tvFechaNacimientoLabel = view.findViewById(R.id.tvFechaNacimientoLabel)
        edtFechaNacimiento = view.findViewById(R.id.edtFechaNacimiento)
        tvTelefonoLabel = view.findViewById(R.id.tvTelefonoLabel)
        edtTelefono = view.findViewById(R.id.edtTelefono)
        tvContrasenyaLabel = view.findViewById(R.id.tvContrasenyaLabel)
        edtContrasenya = view.findViewById(R.id.edtContrasenya)
        btnRegistrarse2 = view.findViewById(R.id.btnRegistrarse2)


        showPhase1()

        btnRegistrarse1.setOnClickListener {
            if (validatePhase1()) {
                // Si la validació de la Fase 1 passa, guardar les dades i passar a la Fase 2
                nombre = edtNombre.text.toString().trim() // Utilitza trim() per eliminar espais blancs innecessaris
                apellidos = edtApellidos.text.toString().trim()
                correo = edtCorreo.text.toString().trim()

                showPhase2() // Transició a la segona fase
            }
        }

        btnRegistrarse2.setOnClickListener {
            if (validatePhase2()) {
                // Si la validació de la Fase 2 passa, recollir totes les dades
                val fechaNacimiento = edtFechaNacimiento.text.toString().trim()
                val telefono = edtTelefono.text.toString().trim()
                val contrasenya = edtContrasenya.text.toString() // Contrasenya en text pla del camp


                // *** 1. Generar el HASH de la contrasenya amb BCrypt ***
                val contrasenaHash = Usuario.Companion.generarHash(contrasenya)
                if (contrasenaHash.isNullOrBlank()) {
                    Toast.makeText(requireContext(), "Error intern al processar la contrasenya.", Toast.LENGTH_SHORT).show()
                    Log.e("RegistroAPI", "Hash de contrasenya generat és nul o buit.")
                    return@setOnClickListener // Sortir si falla el hashing
                }
                Log.d("RegistroAPI", "Hash de contrasenya generat (NO loguejar en producció!): $contrasenaHash")


                // *** 2. Obtenir el fitxer de la foto per defecte des del drawable ***
                val defaultDrawableId = R.drawable.add_image // *** Utilitza l'ID del teu drawable ***
                // Utilitzem la funció auxiliar que hem creat
                val photoFile: File? = getFileFromDrawable(requireContext(), defaultDrawableId, "default_profile_photo.png") // Nom del fitxer temporal

                if (photoFile == null || !photoFile.exists()) {
                    Toast.makeText(requireContext(), "Error intern: No es pot obtenir la imatge per defecte.", Toast.LENGTH_SHORT).show()
                    Log.e("RegistroAPI", "No es va poder obtenir el fitxer de la foto per defecte.")
                    return@setOnClickListener
                }
                Log.d("RegistroAPI", "Fitxer de foto per defecte a enviar: ${photoFile.name}, Path: ${photoFile.absolutePath}")


                // 3. Preparar les PARTS de la petició multipart

                val nombrePart = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
                val apellidosPart = apellidos.toRequestBody("text/plain".toMediaTypeOrNull())
                val correoPart = correo.toRequestBody("text/plain".toMediaTypeOrNull())
                val contrasenaHashPart = contrasenaHash.toRequestBody("text/plain".toMediaTypeOrNull()) // *** Enviem el HASH ***
                val fechaNacimientoPart = fechaNacimiento.toRequestBody("text/plain".toMediaTypeOrNull())
                val telefonoPart = telefono.toRequestBody("text/plain".toMediaTypeOrNull())
                // *** Determina la ID del rol segons la lògica de l'app ***
                val idRolValue = 1 // *** Substitueix 1 per la lògica per obtenir el rol real (1 per Client, 2 per Gestor, etc.) ***
                val idRolPart = idRolValue.toString().toRequestBody("text/plain".toMediaTypeOrNull())

                // *** Preparar la part del fitxer "photo" ***
                // "photo" ha de coincidir EXACTAMENT amb el nom que espera l'API HttpContext.Current.Request.Files["photo"]
                val photoRequestBody = photoFile.asRequestBody("image/png".toMediaTypeOrNull()) // Utilitza el tipus MIME correcte (image/png, image/jpeg, etc.)
                val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody) // "photo" és el nom esperat pel servidor


                // 4. Fer la crida a l'API POST utilitzant RetrofitClient
                RetrofitClient.apiService.postUsuario(
                    nombrePart,
                    apellidosPart,
                    correoPart,
                    contrasenaHashPart, // Enviem el HASH
                    fechaNacimientoPart,
                    telefonoPart,
                    idRolPart,
                    photoPart // *** Incloem la part de la foto ***
                                                     ).enqueue(object : Callback<UsuarioRegistrat> { // Esperem la teva classe de resposta

                    override fun onResponse(call: Call<UsuarioRegistrat>, response: Response<UsuarioRegistrat>) {
                        // Aquest codi s'executa al fil principal (UI thread)
                        if (response.isSuccessful) {
                            val nuevoUsuario: UsuarioRegistrat? = response.body()
                            if (nuevoUsuario != null) {
                                Log.d("RegistroAPI", "Usuari registrat amb èxit. ID: ${nuevoUsuario.id}, Correu: ${nuevoUsuario.correo}")
                                Toast.makeText(requireContext(), "Registre completat!", Toast.LENGTH_LONG).show()

                                // *** Netejar el fitxer temporal després de l'èxit ***
                                photoFile.delete()
                                Log.d("RegistroAPI", "Fitxer temporal eliminat després d'èxit.")


                                // *** Després del registre exitós, navega a la pantalla de Login o a la principal ***
                                // startActivity(Intent(this@RegistroActivity, LoginActivity::class.java))
                                // finish()

                            } else {
                                Log.e("RegistroAPI", "Registre exitós (codi 2xx), però resposta del cos nul.")
                                Toast.makeText(requireContext(), "Registre completat, però dades de resposta buides.", Toast.LENGTH_SHORT).show()
                                // *** Netejar el fitxer temporal fins i tot si el cos és nul ***
                                photoFile.delete()
                                Log.d("RegistroAPI", "Fitxer temporal eliminat després d'èxit (cos nul).")
                            }
                        } else {
                            // La petició va fallar (codi 400, 409, 500, etc.)
                            val statusCode = response.code()
                            val errorBody = response.errorBody()?.string()
                            Log.e("RegistroAPI", "Error al registrar usuari. Codi: $statusCode, Error: $errorBody")

                            Toast.makeText(requireContext(), "Error en el registre: $errorBody", Toast.LENGTH_LONG).show()

                            // *** Netejar el fitxer temporal en cas d'error de l'API ***
                            photoFile.delete()
                            Log.d("RegistroAPI", "Fitxer temporal eliminat després d'error API.")
                        }
                    }

                    override fun onFailure(call: Call<UsuarioRegistrat>, t: Throwable) {
                        // Fallada a nivell de xarxa o excepció inesperada
                        Log.e("RegistroAPI", "Fallo de red o excepció al registrar usuari", t)
                        Toast.makeText(requireContext(), "Error de connexió. Torna a intentar-ho.", Toast.LENGTH_SHORT).show()

                        // *** Netejar el fitxer temporal en cas de fallada de xarxa ***
                        photoFile.delete()
                        Log.d("RegistroAPI", "Fitxer temporal eliminat després de fallada de xarxa.")
                    }
                }) // Fi de enqueue


            }
        }

        edtFechaNacimiento.setOnClickListener {
            showDatePickerDialog()
        }
    }

    // --- Funcions per controlar la visibilitat de les fases ---

    private fun showPhase1() {
        // Fer visibles els elements de la Fase 1
        tvNombreLabel.visibility = View.VISIBLE
        edtNombre.visibility = View.VISIBLE
        tvApellidosLabel.visibility = View.VISIBLE
        edtApellidos.visibility = View.VISIBLE
        tvCorreoLabel.visibility = View.VISIBLE
        edtCorreo.visibility = View.VISIBLE
        btnRegistrarse1.visibility = View.VISIBLE

        // Amagar els elements de la Fase 2
        tvFechaNacimientoLabel.visibility = View.GONE
        edtFechaNacimiento.visibility = View.GONE
        tvTelefonoLabel.visibility = View.GONE
        edtTelefono.visibility = View.GONE
        tvContrasenyaLabel.visibility = View.GONE
        edtContrasenya.visibility = View.GONE
        btnRegistrarse2.visibility = View.GONE

        // Opcional: Amagar els errors dels camps de la Fase 1 si tornes enrere
        edtNombre.error = null
        edtApellidos.error = null
        edtCorreo.error = null
    }

    private fun showPhase2() {
        // Amagar els elements de la Fase 1
        tvNombreLabel.visibility = View.GONE
        edtNombre.visibility = View.GONE
        tvApellidosLabel.visibility = View.GONE
        edtApellidos.visibility = View.GONE
        tvCorreoLabel.visibility = View.GONE
        edtCorreo.visibility = View.GONE
        btnRegistrarse1.visibility = View.GONE

        // Fer visibles els elements de la Fase 2
        tvFechaNacimientoLabel.visibility = View.VISIBLE
        edtFechaNacimiento.visibility = View.VISIBLE
        tvTelefonoLabel.visibility = View.VISIBLE
        edtTelefono.visibility = View.VISIBLE
        tvContrasenyaLabel.visibility = View.VISIBLE
        edtContrasenya.visibility = View.VISIBLE
        btnRegistrarse2.visibility = View.VISIBLE

        // Opcional: Amagar els errors dels camps de la Fase 2 si tornes enrere
        edtFechaNacimiento.error = null
        edtTelefono.error = null
        edtContrasenya.error = null
    }

    // --- Funcions de validació ---

    private fun validatePhase1(): Boolean {
        var isValid = true

        val nombreText = edtNombre.text.toString().trim()
        val apellidosText = edtApellidos.text.toString().trim()
        val correoText = edtCorreo.text.toString().trim()

        if (nombreText.isBlank()) {
            edtNombre.error = "El nombre no puede estar vacío"
            isValid = false
        } else {
            edtNombre.error = null // Netejar l'error si és vàlid
        }

        if (apellidosText.isBlank()) {
            edtApellidos.error = "Los apellidos no pueden estar vacíos"
            isValid = false
        } else {
            edtApellidos.error = null
        }

        if (correoText.isBlank()) {
            edtCorreo.error = "El correo no puede estar vacío"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoText).matches()) {
            // Validació bàsica de format de correu electrònic
            edtCorreo.error = "Introduce un correo electronico válido"
            isValid = false
        } else {
            edtCorreo.error = null
        }

        return isValid
    }

    private fun validatePhase2(): Boolean {
        var isValid = true

        val fechaNacimientoText = edtFechaNacimiento.text.toString().trim()
        val telefonoText = edtTelefono.text.toString().trim()
        val contrasenyaText = edtContrasenya.text.toString() // No fer trim() aquí normalment

        if (fechaNacimientoText.isBlank()) {
            edtFechaNacimiento.error = "La fecha de nacimiento no puede estar vacía"
            isValid = false
        } else {
            edtFechaNacimiento.error = null
        }

        if (telefonoText.isBlank()) {
            edtTelefono.error = "El telefono no puede estar vacío"
            isValid = false
        } else {
            // Validació bàsica: només dígits. Potser vols una validació més complexa.
            if (!telefonoText.all { it.isDigit() }) {
                edtTelefono.error = "Introduce un numero de telefono válido (solamente digitos)"
                isValid = false
            } else {
                edtTelefono.error = null
            }
        }


        if (contrasenyaText.isBlank()) {
            edtContrasenya.error = "La contraseña no puede estar vacía"
            isValid = false
        } else if (contrasenyaText.length < 8) {
            edtContrasenya.error = "La contraseña debe tener como mínimo 8 carácteres"
            isValid = false
        }
        else {
            edtContrasenya.error = null
        }

        return isValid
    }


    // --- Funció per mostrar el DatePickerDialog ---

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // selectedMonth és indexat des de 0, per això sumem 1
            val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            edtFechaNacimiento.setText(selectedDate)
        }, year, month, day)

        // Opcional: Estableix la data màxima a avui o una data en el passat (un usuari no pot haver nascut en el futur)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Opcional: Estableix la data mínima si cal

        datePickerDialog.show()
    }

    fun getFileFromDrawable(context: Context, drawableId: Int, fileName: String): File? {
        try {
            val resources = context.resources
            // Utilitza openRawResource per a drawables a la carpeta raw, o openResource per a drawables generals.
            // La majoria dels drawables .png estan a la carpeta drawable general.
            val inputStream: InputStream = resources.openRawResource(drawableId) // Si estàs segur que és a res/raw
            // O millor per a drawables generals:
            // val drawable = resources.getDrawable(drawableId, null)
            // Si necessites obtenir un bitmap primer:
            // val bitmap = (drawable as? BitmapDrawable)?.bitmap ?: return null
            // val inputStream = bitmapToInputStream(bitmap) // Necessites implementar bitmapToInputStream

            // Alternativa senzilla: Copiar directament el stream del recurs
            val file = File(context.cacheDir, fileName) // Crea un fitxer temporal a la cache de l'app

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream) // Copia el contingut del drawable al fitxer
            }
            inputStream.close() // Tanca el stream d'entrada

            return file // Retorna el fitxer temporal creat

        } catch (e: Exception) {
            Log.e("FileHelper", "Error creant fitxer des de drawable ${drawableId}: ${e.message}", e)
            return null // Retorna null si hi ha un error
        }
    }
}