package com.example.culturabcn.ui.perfil

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.MainActivity
import com.example.culturabcn.R
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.RutaImagenDto
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
import okhttp3.ResponseBody
import org.osmdroid.tileprovider.cachemanager.CacheManager.getFileName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.sql.Date
import java.sql.Time
import java.util.Calendar


class PerfilFragment : Fragment() {
    private lateinit var usuarioC: Cliente
    private lateinit var usuarioG: Gestor
    private lateinit var reservasAdapter: EventosAdapter

    private lateinit var imgPerfil: ImageView

    // *** Variables i Launchers per a la Selecció d'Imatge ***
    // Launchers (registrats una vegada al Fragment)
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    // Variable per a la URI temporal de la càmera
    private var cameraPhotoUri: Uri? = null

    // Variable per guardar la URI de la NOVA imatge seleccionada per l'usuari (nul·la si no se'n selecciona una de nova)
    private var newProfilePhotoUri: Uri? = null

    // Variable per guardar la referència a la ImageView objectiu per a la selecció (la principal o la del diàleg)
    private var targetImageView: ImageView? = null

    // Variable per guardar la referència a la ImageView de la imatge del diàleg (per poder accedir-hi des dels callbacks del Fragment)
    private var imgPerfilDialog: ImageView? = null

    private fun createImageFile(): Uri? {
        val timestamp = System.currentTimeMillis()
        val storageDir = requireContext().getExternalFilesDir(null)
        return try {
            val tempFile = File.createTempFile("PROFILE_IMG_${timestamp}_", ".jpg", storageDir)
            FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                tempFile
                                      )
        } catch (e: Exception) {
            Log.e("PerfilFragment", "Error creant fitxer temporal per a càmera", e)
            null
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver: ContentResolver = context.contentResolver ?: return null
        val fileName =
            getFileName(contentResolver, uri) ?: "temp_profile_file_${System.currentTimeMillis()}"
        val file = File(context.cacheDir, fileName)

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return file
        } catch (e: Exception) {
            Log.e(
                "FileHelper",
                "Error obtenint fitxer des de Uri ${uri.toString()}: ${e.message}",
                e
                 )
            return null
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex =
                    it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) // Utilitza la constant qualificada
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        if (name == null) {
            name = uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it + 1) }
        }
        return name
    }


    // Metodo onCreateView para inflar el layout del fragmento
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View? {
        // Set up ActivityResultLaunchers aquí (es manté igual)
        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    newProfilePhotoUri = uri
                    targetImageView?.setImageURI(uri) // Utilitza crida segura
                } else {
                    newProfilePhotoUri = null
                }
                targetImageView = null
                cameraPhotoUri = null
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    newProfilePhotoUri = cameraPhotoUri
                    targetImageView?.setImageURI(newProfilePhotoUri) // Utilitza crida segura
                } else {
                    newProfilePhotoUri = null
                    cameraPhotoUri = null
                }
                targetImageView = null
            }

        // Inflamos el layout para este fragmento
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // *** Obtenir referència a la ImageView principal del perfil ***
        // Asigna el resultat de findViewById directament a la variable anul·lable
        imgPerfil = view.findViewById(R.id.imgPerfil)

        // Opcional: Log per confirmar si la ImageView principal es va trobar
        if (imgPerfil == null) {
            Log.e("PerfilFragment", "ImageView R.id.imgPerfil not found in layout.")
            // Considera deshabilitar funcionalitat que depèn d'aquesta ImageView si no es troba.
        }


        // Recibimos el cliente o gestor (es manté igual)
        if (UserLogged.rolId == 1) {
            RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<List<Cliente>>,
                    response: Response<List<Cliente>>
                                       ) {
                    if (response.isSuccessful) {
                        val clientes = response.body()
                        val usuarioIniciado = clientes?.find { it.id == UserLogged.userId }
                        if (usuarioIniciado != null) {
                            usuarioC = usuarioIniciado
                            // Carregar la imatge i actualitzar UI. loadProfileImage ja utilitzarà crida segura per a imgPerfil.
                            loadProfileImage(usuarioC.foto)
                            updateProfileUI(usuarioC)
                        } else {
                            Log.e(
                                "PerfilFragment",
                                "Error al recibir el usuario: ${response.errorBody()?.string()}"
                                 )
                        }
                    } else {
                        Log.e(
                            "PerfilFragment",
                            "Error en la respuesta: ${response.errorBody()?.string()}"
                             )
                    }
                }

                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) { /* ... */
                }
            })
        } else {
            // Si el usuario es gestor
            RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                override fun onResponse(
                    call: Call<List<Gestor>>,
                    response: Response<List<Gestor>>
                                       ) {
                    if (response.isSuccessful) {
                        val gestores = response.body()
                        val usuarioIniciado = gestores?.find { it.id == UserLogged.userId }
                        if (usuarioIniciado != null) {
                            usuarioG = usuarioIniciado
                            // Carregar la imatge i actualitzar UI
                            loadProfileImage(usuarioG.foto)
                            updateProfileUI(usuarioG)
                        } else {
                            Log.e(
                                "PerfilFragment",
                                "Error al recibir el usuario: ${response.errorBody()?.string()}"
                                 )
                        }
                    } else {
                        Log.e(
                            "PerfilFragment",
                            "Error en la respuesta: ${response.errorBody()?.string()}"
                             )
                    }
                }

                override fun onFailure(call: Call<List<Gestor>>, t: Throwable) { /* ... */
                }
            })
        }

        val btnEditar = view.findViewById<Button>(R.id.btnEditar)

        btnEditar.setOnClickListener {
            // Comprovacions (es mantenen igual)
            if (UserLogged.rolId == 1 && !::usuarioC.isInitialized) { /* ... */ return@setOnClickListener
            }
            if (UserLogged.rolId != 1 && !::usuarioG.isInitialized) { /* ... */ return@setOnClickListener
            }

            val currentUser = if (UserLogged.rolId == 1) usuarioC else usuarioG
            showEditDialog(requireContext(), currentUser) // Passa l'usuari
        }

        // Configurar el RecyclerView per a reserves (es manté igual)
        val recyclerViewReservas: RecyclerView = view.findViewById(R.id.recyclerViewReservas)
        recyclerViewReservas.layoutManager = LinearLayoutManager(activity)
        reservasAdapter = EventosAdapter(emptyList()) { clickedEvento ->
            // Aquesta és la lògica que s'executarà quan es cliqui el botó "Reservar"
            // en un esdeveniment DINS del RecyclerView de RESERVES.
            // Pots posar la lògica que vulguis aquí.
            Toast.makeText(
                requireContext(),
                "Ya tienes una reserva para ${clickedEvento.nombre}. ¡Disfruta del evento!",
                Toast.LENGTH_SHORT
                          ).show()
            // Si volguessis navegar a una pantalla de gestió de reserva o similar, ho faries aquí.
            // findNavController().navigate(R.id.action_perfilFragment_to_gestionReservaFragment, bundle)
        }
        recyclerViewReservas.adapter = reservasAdapter

        val userId = UserLogged.userId
        if (userId != null) {
            RetrofitClient.apiService.getReservasPorUsuario(userId)
                .enqueue(object : Callback<List<Evento>> {
                    override fun onResponse(
                        call: Call<List<Evento>>,
                        response: Response<List<Evento>>
                                           ) { /* ... */
                    }

                    override fun onFailure(call: Call<List<Evento>>, t: Throwable) { /* ... */
                    }
                })
        } else { /* ... */
        }

        // *** El botó de canviar imatge principal (si existeix a l'XML del fragment) hauria d'estar aquí ***
        // Si SÍ que tens un botó a fragment_perfil.xml amb ID btnCambiarImg, afegeix aquí:
        // val btnCambiarImgLocal = view.findViewById<Button>(R.id.btnCambiarImg)
        // btnCambiarImgLocal?.setOnClickListener {
        //     targetImageView = imgPerfil
        //     showImagePickerDialog()
        // }
        // Si NO tens un botó a fragment_perfil.xml amb ID btnCambiarImg, llavors l'única manera de canviar la imatge és des del diàleg.
        // Sembla que el teu codi original tenia la declaració però no l'inicialització i listener aquí.

    } // Fi de onViewCreated

    private fun showImagePickerDialog() {
        AlertDialog.Builder(requireContext()).setTitle("Seleccionar Imágen")
            .setItems(arrayOf("Desde Galería", "Tomar Foto")) { dialog, which ->
                when (which) {
                    0 -> openGallery() // Utilitza el launcher de Galeria del Fragment
                    1 -> openCamera() // Utilitza el launcher de Càmera del Fragment
                }
            }.show()
    }

    private fun openGallery() {
        pickImageLauncher.launch("image/*") // El resultat es gestiona al callback del launcher
    }

    // Llança la càmera
    private fun openCamera() {
        cameraPhotoUri = createImageFile()
        if (cameraPhotoUri != null) {
            takePictureLauncher.launch(cameraPhotoUri) // El resultat es gestiona al callback del launcher
        } else {
            Toast.makeText(
                requireContext(),
                "Error al preparar para tomar foto.",
                Toast.LENGTH_SHORT
                          ).show()
        }
    }

    private fun loadProfileImage(fotoUrl: String?) {
        // Utilitza crida segura per mostrar un placeholder només si imgPerfil no és nul
        imgPerfil?.setImageResource(R.drawable.side_nav_bar) // Placeholder


        if (!fotoUrl.isNullOrBlank()) {
            val rutaImagenDto = RutaImagenDto(Foto_url = fotoUrl)

            RetrofitClient.apiService.postImagen(rutaImagenDto)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                                           ) {
                        val receivedUrlForLog = fotoUrl // Per als logs

                        Log.d(
                            "PROFILE_IMAGE_LOAD",
                            "onResponse (Save to File) per a principal. Codi: ${response.code()}"
                             )

                        var tempFile: File? = null // Fitxer temporal per guardar la imatge rebuda
                        var inputStream: InputStream? = null
                        var outputStream: FileOutputStream? = null

                        try {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null) {
                                    // *** Guardar el ResponseBody a un Fitxer Temporal ***
                                    // Creem un nom de fitxer temporal únic a la cache dir
                                    tempFile = File(
                                        requireContext().cacheDir,
                                        "profile_img_${System.currentTimeMillis()}.temp"
                                                   )
                                    inputStream = responseBody.byteStream()
                                    outputStream = FileOutputStream(tempFile)

                                    // Copiar l'stream d'entrada a l'stream de sortida (al fitxer)
                                    inputStream.copyTo(outputStream)

                                    // *** Ara carregar la imatge des del Fitxer Temporal usant Glide ***
                                    // Comprovar que el fitxer es va crear correctament i no està buit
                                    if (tempFile.exists() && tempFile.length() > 0) {
                                        // Utilitza let per assegurar que imgPerfil no és nul abans de cridar a Glide
                                        imgPerfil?.let { imageView ->
                                            Glide.with(requireContext())
                                                .load(tempFile) // Carregar des de l'objecte File
                                                .apply(
                                                    RequestOptions().placeholder(R.drawable.side_nav_bar) // Placeholder
                                                        .error(R.drawable.ic_menu_slideshow)
                                                      ) // Imatge d'error
                                                // Evita la cache de la font temporal, Glide cachejarà la imatge decodificada
                                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                                .skipMemoryCache(true) // Evita que la font temporal es guardi a la cache de memòria
                                                .into(imageView) // Carregar a la ImageView principal

                                            Log.d(
                                                "PROFILE_LOAD_SUCCESS_FILE",
                                                "Imatge de perfil carregada amb èxit des del fitxer temporal: ${tempFile.absolutePath}"
                                                 )
                                        } ?: run {
                                            // Aquest bloc s'executa si imgPerfil era nul (findViewById va retornar nul)
                                            Log.e(
                                                "PROFILE_LOAD_ERROR_FILE",
                                                "imgPerfil és nul, no es pot carregar la imatge des del fitxer temporal."
                                                 )
                                        }
                                    } else {
                                        Log.e(
                                            "PROFILE_LOAD_ERROR_FILE",
                                            "Fitxer temporal creat buit o inexistent per a $receivedUrlForLog"
                                             )
                                        // Utilitza crida segura per establir la imatge d'error
                                        imgPerfil?.setImageResource(R.drawable.ic_menu_slideshow)
                                    }

                                } else { // La resposta API no va ser exitosa (codi d'error)
                                    val statusCode = response.code()
                                    val errorBody = response.errorBody()?.string()
                                    Log.e(
                                        "PROFILE_LOAD_ERROR_FILE",
                                        "API Response Error ${statusCode} loading image to file for $receivedUrlForLog. Body: ${errorBody}"
                                         )
                                    // Utilitza crida segura per establir la imatge d'error
                                    imgPerfil?.setImageResource(R.drawable.ic_menu_slideshow)
                                }
                            } else { // La resposta API va ser exitosa (2xx) però el cos era nul
                                Log.e(
                                    "PROFILE_LOAD_ERROR_FILE",
                                    "API Response exitosa però cos nul loading image to file for $receivedUrlForLog"
                                     )
                                // Utilitza crida segura per establir la imatge d'error
                                imgPerfil?.setImageResource(R.drawable.ic_menu_slideshow)
                            }
                        } catch (e: IOException) {
                            // Error d'entrada/sortida en guardar o llegir el fitxer
                            Log.e(
                                "PROFILE_LOAD_ERROR_FILE",
                                "IO Error saving image to file for $receivedUrlForLog",
                                e
                                 )
                            // Utilitza crida segura per establir la imatge d'error
                            imgPerfil?.setImageResource(R.drawable.ic_menu_slideshow)
                        } catch (e: Exception) {
                            // Qualsevol altra excepció durant el procés
                            Log.e(
                                "PROFILE_LOAD_ERROR_FILE",
                                "Unexpected Error saving image to file for $receivedUrlForLog",
                                e
                                 )
                            // Utilitza crida segura per establir la imatge d'error
                            imgPerfil?.setImageResource(R.drawable.ic_menu_slideshow)
                        } finally {
                            // *** Crucial: Tancar els streams i eliminar el fitxer temporal ***
                            try {
                                inputStream?.close()
                            } catch (e: IOException) {
                                Log.e("PROFILE_LOAD_ERROR_FILE", "Error closing input stream", e)
                            }
                            try {
                                outputStream?.close()
                            } catch (e: IOException) {
                                Log.e("PROFILE_LOAD_ERROR_FILE", "Error closing output stream", e)
                            }
                            // Eliminar el fitxer temporal després d'intentar carregar-lo o en cas d'error
                            if (tempFile != null && tempFile.exists()) {
                                val deleted = tempFile.delete()
                                if (deleted) Log.d(
                                    "PROFILE_LOAD_SUCCESS_FILE",
                                    "Fitxer temporal eliminat: ${tempFile.absolutePath}"
                                                  )
                                else Log.e(
                                    "PROFILE_LOAD_ERROR_FILE",
                                    "No s'ha pogut eliminar el fitxer temporal: ${tempFile.absolutePath}"
                                          )
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        val receivedUrlForLog = fotoUrl
                        Log.e(
                            "PROFILE_LOAD_FAILURE_FILE",
                            "Network Error loading image to file for $receivedUrlForLog",
                            t
                             )
                        // Utilitza crida segura per establir la imatge d'error en fallada de xarxa
                        imgPerfil?.setImageResource(R.drawable.ic_menu_slideshow)
                        // No es crea fitxer temporal en fallada de xarxa, però la neteja aquí és per robustesa.
                    }
                })
        } else {
            // Si fotoUrl és nul·la o buida, mostra la imatge per defecte
            Log.d(
                "PROFILE_IMAGE_LOAD",
                "fotoUrl de perfil principal és nul·la o buida. Mostrant imatge per defecte."
                 )
            // Utilitza crida segura per establir la imatge per defecte
            imgPerfil?.setImageResource(R.drawable.side_nav_bar) // Utilitza el teu drawable per defecte
        }
    }


    // Mostrar dialogo cuando editamos el perfil
    @SuppressLint("MissingInflatedId")
    // Passa l'usuari actual com a paràmetre per accedir a les seves dades (inclosa la foto_url original)
    private fun showEditDialog(context: Context, currentUser: Usuario) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edt_user, null)

        // *** Obtenir referències als elements de UI del diàleg (camps de text i botons) ***
        val edtNombre = dialogView.findViewById<EditText>(R.id.edtNombre)
        val edtApellido = dialogView.findViewById<EditText>(R.id.edtApellidos)
        val edtCorreo = dialogView.findViewById<EditText>(R.id.edtCorreo)
        val edtTelefono = dialogView.findViewById<EditText>(R.id.edtTelefono)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmar)
        val edtFechaNacimiento = dialogView.findViewById<EditText>(R.id.edtFechaNacimiento)

        val imgPerfilDialogLocal =
            dialogView.findViewById<ImageView>(R.id.imgPerfilDialog) // ImageView del diàleg
        // *** NOTA: btnCambiarImgDialog es troba DINS del layout del diàleg ***
        val btnCambiarImgDialog =
            dialogView.findViewById<Button>(R.id.btnCambiarImg) // Botó de canvi dins del diàleg


        // *** Assignar la referència de la ImageView del diàleg a la variable del Fragment ***
        // Això permetrà que els callbacks dels launchers del Fragment accedeixin a aquesta ImageView
        imgPerfilDialog =
            imgPerfilDialogLocal // Assigna la referència local a la variable del Fragment

        // Validació bàsica per assegurar que imgPerfilDialog no és nul abans d'utilitzar-lo
        if (imgPerfilDialog == null) {
            Log.e("PerfilFragment", "ImageView R.id.imgPerfilDialog not found in dialog layout.")
            // Potser deshabilitar la funcionalitat d'imatge al diàleg si no es troba la ImageView
            btnCambiarImgDialog.isEnabled = false // Deshabilita el botó de canvi
            // No podem carregar la imatge actual si la ImageView és nul·la
            // Continuem amb la resta del diàleg sense la funcionalitat d'imatge
        } else {
            // Mostrar la imatge de perfil ACTUAL al diàleg només si imgPerfilDialog no és nul
            // Necessitem la foto_url actual de l'usuari. Assumeix que l'objecte currentUser la té.
            val currentFotoUrl = currentUser.foto // Assumeix que el camp 'foto' conté la foto_url
            // Utilitza !! aquí perquè acabem de comprovar que imgPerfilDialog no és nul
            loadProfileImageInDialog(
                imgPerfilDialog!!,
                currentFotoUrl
                                    ) // Carregar la imatge a la ImageView del diàleg


            // *** Set click listener per al botó Cambiar Imágen DINS DEL DIÀLEG ***
            // Quan es clica al botó del diàleg, l'objectiu és la ImageView del diàleg
            btnCambiarImgDialog.setOnClickListener {
                targetImageView =
                    imgPerfilDialog // Estableix la ImageView del diàleg com a objectiu

                // Crida al diàleg de selecció de foto compartit del Fragment
                showImagePickerDialog() // Aquest diàleg utilitzarà els launchers del Fragment
            }
        }


        // Omplir els camps de text del diàleg
        edtNombre.setText(currentUser.nombre)
        edtApellido.setText(currentUser.apellidos)
        edtCorreo.setText(currentUser.correo)
        edtTelefono.setText(currentUser.telefono)
        edtFechaNacimiento.setText(currentUser.fechaNacimiento.replace("T00:00:00", ""))


        // Configuració del DatePickerDialog (es manté igual)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        edtFechaNacimiento.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format(
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                                                    )
                    edtFechaNacimiento.setText(selectedDate)
                }, year, month, day)
            datePickerDialog.show()
        }

        // Crea el AlertDialog
        val dialogBuilder = AlertDialog.Builder(context).setView(dialogView)
            .setCancelable(true) // Permet tancar el diàleg clicant fora
        val dialog = dialogBuilder.create()


        btnConfirmar.setOnClickListener {

            // 1. Recollir les dades dels camps editables
            val updatedNombre = edtNombre.text.toString().trim()
            val updatedApellidos = edtApellido.text.toString().trim()
            val updatedCorreo = edtCorreo.text.toString().trim()
            val updatedTelefono = edtTelefono.text.toString().trim()
            val updatedFechaNacimiento = edtFechaNacimiento.text.toString().trim()

            // Validació: El nom no pot estar buit
            if (updatedNombre.isBlank()) {
                Toast.makeText(
                    context, getString(R.string.el_nombre_no_puede_estar_vac_o), Toast.LENGTH_SHORT
                              ).show()
                edtNombre.requestFocus()
                return@setOnClickListener
            }

            // Validació: Els cognoms no poden estar buits
            if (updatedApellidos.isBlank()) {
                Toast.makeText(
                    context,
                    getString(R.string.los_apellidos_no_pueden_estar_vac_os),
                    Toast.LENGTH_SHORT
                              ).show()
                edtApellido.requestFocus()
                return@setOnClickListener
            }

            // Validació: La data de naixement no pot estar buida
            if (updatedFechaNacimiento.isBlank()) {
                Toast.makeText(
                    context,
                    getString(R.string.la_fecha_de_nacimiento_no_puede_estar_vac_a),
                    Toast.LENGTH_SHORT
                              ).show()
                edtFechaNacimiento.requestFocus()
                return@setOnClickListener
            }

            // Validació opcional: updatedTelefono (p.ex., que no estigui buit si és obligatori, o format/longitud)
            if (updatedTelefono.isBlank()) {
                Toast.makeText(
                    context,
                    getString(R.string.el_telefono_no_puede_estar_vac_o),
                    Toast.LENGTH_SHORT
                              ).show()
                edtTelefono.requestFocus()
                return@setOnClickListener
            }

            // Validació: El telèfon només pot contenir números
            if (!updatedTelefono.all { it.isDigit() }) {
                Toast.makeText(
                    context, getString(R.string.el_telefono_solamente_puede_contener_n_meros),
                    Toast.LENGTH_SHORT
                              ).show()
                edtTelefono.requestFocus()
                return@setOnClickListener // Surt si falla la validació
            }

            // 2. Obtenir dades de referència
            val userId = UserLogged.userId
            val userRolId = UserLogged.rolId
            val currentContrasenaHash = currentUser.contrasenaHash
            val originalFotoUrl = currentUser.foto


            // Validacions prèvies
            if (userId == null || userRolId == null || currentContrasenaHash.isNullOrBlank() || originalFotoUrl.isNullOrBlank()) {
                Toast.makeText(
                    context,
                    getString(R.string.error_datos_del_usuario_incompletos),
                    Toast.LENGTH_SHORT
                              ).show()
                Log.e(
                    "PerfilFragment",
                    "Dades crítiques de l'usuari nul·les abans de PUT: userId=$userId, rolId=$userRolId, hashNul=${currentContrasenaHash.isNullOrBlank()}, fotoUrlNulOrBlank=${originalFotoUrl.isNullOrBlank()}"
                     )
                return@setOnClickListener
            }

            // 3. Preparar el FITXER de la part "photo"
            val photoFile: File? = if (newProfilePhotoUri != null) {
                getFileFromUri(context, newProfilePhotoUri!!)
            } else {
                val defaultDrawableId = R.drawable.side_nav_bar
                getFileFromDrawable(
                    context,
                    defaultDrawableId,
                    "default_profile_photo_edit_${userId}_${System.currentTimeMillis()}.png"
                                   )
            }

            // *** FER LA COMPROVACIÓ DE NUL·LITAT JUST AQUÍ ***
            if (photoFile == null || !photoFile.exists() || photoFile.length() == 0L) {
                Toast.makeText(
                    context,
                    getString(R.string.error_interno_no_se_puede_obtener_la_imagen_para_actualizarla),
                    Toast.LENGTH_SHORT
                              ).show()
                Log.e(
                    "PerfilFragment",
                    "No es va poder obtenir el fitxer de la foto (nou o per defecte) per a l'edició. Fitxer nul o invàlid."
                     )
                if (photoFile != null) photoFile.delete()
                return@setOnClickListener // Surt si el fitxer no és vàlid
            }

            // *** ARA, photoFile ÉS SEGUR QUE NO ÉS NUL I ÉS VÀLID ***

            // 4. Preparar les PARTS de la petició multipart
            val idUsuarioPart = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val nombrePart = updatedNombre.toRequestBody("text/plain".toMediaTypeOrNull())
            val apellidosPart = updatedApellidos.toRequestBody("text/plain".toMediaTypeOrNull())
            val correoPart = updatedCorreo.toRequestBody("text/plain".toMediaTypeOrNull())
            val fotoUrlPart =
                originalFotoUrl.toRequestBody("text/plain".toMediaTypeOrNull()) // fotoUrl ORIGINAL
            val contrasenaHashPart =
                currentContrasenaHash.toRequestBody("text/plain".toMediaTypeOrNull())
            val fechaNacimientoPart =
                updatedFechaNacimiento.toRequestBody("text/plain".toMediaTypeOrNull())
            val telefonoPart = updatedTelefono.toRequestBody("text/plain".toMediaTypeOrNull())
            val idRolPart = userRolId.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            // Part del fitxer "photo" (utilitza el photoFile vàlid)
            val photoRequestBody = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
            val photoPart =
                MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody)

            Log.d(
                "PerfilFragment",
                "Fitxer de foto a enviar per a edició: ${photoFile.name}, Mida: ${photoFile.length()} bytes"
                 )


            // 5. Fer la crida a l'API PUT
            RetrofitClient.apiService.putUsuario(
                idUsuarioPart,
                nombrePart,
                apellidosPart,
                correoPart,
                fotoUrlPart,
                contrasenaHashPart,
                fechaNacimientoPart,
                telefonoPart,
                idRolPart,
                photoPart
                                                ).enqueue(object : Callback<Boolean> {

                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    dialog.dismiss()

                    // Neteja el fitxer temporal creat
                    photoFile.delete() // Segur cridar-ho aquí

                    if (response.isSuccessful && response.body() == true) {
                        Log.d("PerfilFragment", "Perfil d'usuari actualitzat amb èxit a l'API.")
                        Toast.makeText(
                            context, getString(R.string.perfil_actualizado), Toast.LENGTH_SHORT
                                      ).show()

                        // Refrescar dades i imatge
                        val currentUserId = UserLogged.userId
                        val currentUserRolId = UserLogged.rolId
                        if (currentUserId != null && currentUserRolId != null) {
                            refreshProfileData(currentUserId, currentUserRolId)
                        } else { /* ... */
                        }

                    } else {
                        val statusCode = response.code()
                        val errorBody = response.errorBody()?.string()
                        Log.e(
                            "PerfilFragment",
                            "Error actualitzant perfil. Codi: $statusCode, Error: $errorBody"
                             )
                    }

                    // Netejar newProfilePhotoUri un cop acabada la lògica de la petició PUT
                    newProfilePhotoUri = null
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    dialog.dismiss()
                    // Neteja el fitxer temporal
                    photoFile.delete() // Segur cridar-ho aquí
                    Log.e("PerfilFragment", "Error de connexió actualitzant perfil", t)
                    Toast.makeText(
                        context,
                        getString(R.string.error_de_conexi_n_actualizando_el_perfil),
                        Toast.LENGTH_SHORT
                                  ).show()
                    newProfilePhotoUri = null
                }
            })

        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
            // Opcional: Netejar newProfilePhotoUri si es cancel·la
            newProfilePhotoUri = null
        }

        // Listener per netejar les referències de la ImageView del diàleg quan el diàleg es tanca
        dialog.setOnDismissListener {
            imgPerfilDialog = null
            targetImageView = null // Neteja la referència
        }


        // Muestra el diálogo
        dialog.show()
    }

    private fun loadProfileImageInDialog(imageView: ImageView, fotoUrl: String?) {
        // Mostra un placeholder a la ImageView objectiu (la del diàleg)
        imageView.setImageResource(R.drawable.side_nav_bar) // Placeholder


        if (!fotoUrl.isNullOrBlank()) {
            val rutaImagenDto = RutaImagenDto(Foto_url = fotoUrl)

            RetrofitClient.apiService.postImagen(rutaImagenDto)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                                           ) {
                        val receivedUrlForLog = fotoUrl // Per als logs

                        Log.d(
                            "PROFILE_IMAGE_LOAD_DIALOG",
                            "onResponse per a imatge de perfil (diàleg). Codi: ${response.code()}"
                             )

                        var tempFile: File? = null // Fitxer temporal per guardar la imatge rebuda
                        var inputStream: InputStream? = null
                        var outputStream: FileOutputStream? = null

                        try {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null) {
                                    // *** Guardar el ResponseBody a un Fitxer Temporal ***
                                    // Creem un nom de fitxer temporal únic a la cache dir
                                    tempFile = File(
                                        requireContext().cacheDir,
                                        "profile_img_dialog_${System.currentTimeMillis()}.temp"
                                                   ) // Nom diferent per diàleg
                                    inputStream = responseBody.byteStream()
                                    outputStream = FileOutputStream(tempFile)

                                    // Copiar l'stream d'entrada a l'stream de sortida (al fitxer)
                                    inputStream.copyTo(outputStream)

                                    // *** Ara carregar la imatge des del Fitxer Temporal usant Glide ***
                                    // Comprovar que el fitxer es va crear correctament i no està buit
                                    if (tempFile.exists() && tempFile.length() > 0) {
                                        Glide.with(requireContext())
                                            .load(tempFile) // Carregar des de l'objecte File
                                            .apply(
                                                RequestOptions().placeholder(R.drawable.side_nav_bar) // Placeholder
                                                    .error(R.drawable.ic_menu_slideshow)
                                                  ) // Imatge d'error
                                            // Evita la cache de la font temporal
                                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                                            .skipMemoryCache(true) // Evita la cache de memòria
                                            .into(imageView) // Carregar a la ImageView del diàleg

                                        Log.d(
                                            "PROFILE_LOAD_SUCCESS_FILE_DIALOG",
                                            "Imatge de perfil (diàleg) carregada amb èxit des del fitxer temporal: ${tempFile.absolutePath}"
                                             )
                                    } else {
                                        Log.e(
                                            "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                            "Fitxer temporal creat buit o inexistent per a $receivedUrlForLog (diàleg)"
                                             )
                                        imageView.setImageResource(R.drawable.ic_menu_slideshow)
                                    }

                                } else { // La resposta API va ser exitosa (2xx) però el cos era nul
                                    Log.e(
                                        "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                        "API Response exitosa però cos nul loading image to file for $receivedUrlForLog (diàleg)"
                                         )
                                    imageView.setImageResource(R.drawable.ic_menu_slideshow)
                                }
                            } else { // La resposta API no va ser exitosa (codi d'error)
                                val statusCode = response.code()
                                val errorBody = response.errorBody()?.string()
                                Log.e(
                                    "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                    "API Response Error ${statusCode} loading image to file for $receivedUrlForLog (diàleg). Body: ${errorBody}"
                                     )
                                imageView.setImageResource(R.drawable.ic_menu_slideshow)
                            }
                        } catch (e: IOException) {
                            // Error d'entrada/sortida en guardar o llegir el fitxer
                            Log.e(
                                "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                "IO Error saving image to file for $receivedUrlForLog (diàleg)",
                                e
                                 )
                            imageView.setImageResource(R.drawable.ic_menu_slideshow)
                        } catch (e: Exception) {
                            // Qualsevol altra excepció durant el procés
                            Log.e(
                                "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                "Unexpected Error saving image to file for $receivedUrlForLog (diàleg)",
                                e
                                 )
                            imageView.setImageResource(R.drawable.ic_menu_slideshow)
                        } finally {
                            // *** Crucial: Tancar els streams i eliminar el fitxer temporal ***
                            try {
                                inputStream?.close()
                            } catch (e: IOException) {
                                Log.e(
                                    "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                    "Error closing input stream (diàleg)",
                                    e
                                     )
                            }
                            try {
                                outputStream?.close()
                            } catch (e: IOException) {
                                Log.e(
                                    "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                    "Error closing output stream (diàleg)",
                                    e
                                     )
                            }
                            // Eliminar el fitxer temporal després d'intentar carregar-lo o en cas d'error
                            if (tempFile != null && tempFile.exists()) {
                                val deleted = tempFile.delete()
                                if (deleted) Log.d(
                                    "PROFILE_LOAD_SUCCESS_FILE_DIALOG",
                                    "Fitxer temporal eliminat (diàleg): ${tempFile.absolutePath}"
                                                  )
                                else Log.e(
                                    "PROFILE_LOAD_ERROR_FILE_DIALOG",
                                    "No s'ha pogut eliminar el fitxer temporal (diàleg): ${tempFile.absolutePath}"
                                          )
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        val receivedUrlForLog = fotoUrl
                        Log.e(
                            "PROFILE_LOAD_FAILURE_FILE_DIALOG",
                            "Network Error loading image to file for $receivedUrlForLog (diàleg)",
                            t
                             )
                        imageView.setImageResource(R.drawable.ic_menu_slideshow)
                        // No es crea fitxer temporal en fallada de xarxa, però la neteja és aquí per robustesa.
                    }
                })
        } else {
            // Si fotoUrl és nul·la o buida, mostra la imatge per defecte al diàleg
            Log.d(
                "PROFILE_IMAGE_LOAD_DIALOG",
                "fotoUrl de perfil (diàleg) és nul·la o buida. Mostrant imatge per defecte."
                 )
            imageView.setImageResource(R.drawable.side_nav_bar) // Utilitza el teu drawable per defecte
        }
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
            Log.e(
                "FileHelper",
                "Error creant fitxer des de drawable ${drawableId}: ${e.message}",
                e
                 )
            return null
        }
    }

    private fun refreshProfileData(userId: Int, userRolId: Int) {
        // Opcional: Mostra un indicador de càrrega (Progress Bar)
        // showLoadingIndicator(true)

        if (userRolId == 1) { // Si l'usuari actual és un Client (Rol 1)
            RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                override fun onResponse(
                    call: Call<List<Cliente>>,
                    response: Response<List<Cliente>>
                                       ) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)

                    if (response.isSuccessful) {
                        val clientes = response.body()
                        // Troba l'usuari amb la ID correcta a la llista rebuda
                        val usuarioIniciado = clientes?.find { it.id == userId }

                        if (usuarioIniciado != null) {
                            // *** Dades de Client rebudes. Guarda-les i actualitza la UI. ***
                            usuarioC =
                                usuarioIniciado // Guarda l'objecte Client a la variable del Fragment
                            updateProfileUI(usuarioC!!) // Actualitza la UI amb les dades del Client (ús !! ja que acabem de comprovar que no és nul)

                        } else {
                            // Usuari no trobat a la llista rebuda (molt improbable després d'una edició exitosa)
                            Log.e(
                                "PerfilFragment",
                                "Refetch: Usuari (Client) amb ID $userId no trobat a la llista API."
                                 )
                            Toast.makeText(
                                requireContext(),
                                "Error al carregar dades actualitzades.",
                                Toast.LENGTH_SHORT
                                          ).show()
                        }
                    } else {
                        // Error a la resposta de l'API
                        Log.e(
                            "PerfilFragment",
                            "Refetch: Error API carregar Clients. Codi: ${response.code()}, Cos: ${
                                response.errorBody()?.string()
                            }"
                             )
                        Toast.makeText(
                            requireContext(),
                            "Error al carregar dades del perfil.",
                            Toast.LENGTH_SHORT
                                      ).show()
                    }
                }

                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)
                    // Fallada a nivell de xarxa
                    Log.e(
                        "PerfilFragment",
                        "Refetch: Error xarxa carregar Clients: ${t.message}",
                        t
                         )
                    Toast.makeText(
                        requireContext(),
                        "Error de connexió al carregar perfil.",
                        Toast.LENGTH_SHORT
                                  ).show()
                }
            })
        } else { // Si l'usuari actual és un Gestor (Rol 2)
            RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                override fun onResponse(
                    call: Call<List<Gestor>>,
                    response: Response<List<Gestor>>
                                       ) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)

                    if (response.isSuccessful) {
                        val gestores = response.body()
                        // Troba el gestor amb la ID correcta a la llista rebuda
                        val usuarioIniciado = gestores?.find { it.id == userId }

                        if (usuarioIniciado != null) {
                            // *** Dades de Gestor rebudes. Guarda-les i actualitza la UI. ***
                            usuarioG =
                                usuarioIniciado // Guarda l'objecte Gestor a la variable del Fragment
                            updateProfileUI(usuarioG!!) // Actualitza la UI amb les dades del Gestor (ús !! ja que acabem de comprovar que no és nul)

                        } else {
                            // Gestor no trobat a la llista rebuda
                            Log.e(
                                "PerfilFragment",
                                "Refetch: Usuari (Gestor) amb ID $userId no trobat a la llista API."
                                 )
                            Toast.makeText(
                                requireContext(),
                                "Error al carregar dades actualitzades.",
                                Toast.LENGTH_SHORT
                                          ).show()
                        }
                    } else {
                        // Error a la resposta de l'API
                        Log.e(
                            "PerfilFragment",
                            "Refetch: Error API carregar Gestors. Codi: ${response.code()}, Cos: ${
                                response.errorBody()?.string()
                            }"
                             )
                        Toast.makeText(
                            requireContext(),
                            "Error al carregar dades del perfil.",
                            Toast.LENGTH_SHORT
                                      ).show()
                    }
                }

                override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                    // Opcional: Amaga l'indicador de càrrega
                    // showLoadingIndicator(false)
                    // Fallada a nivell de xarxa
                    Log.e(
                        "PerfilFragment",
                        "Refetch: Error xarxa carregar Gestors: ${t.message}",
                        t
                         )
                    Toast.makeText(
                        requireContext(),
                        "Error de connexió al carregar perfil.",
                        Toast.LENGTH_SHORT
                                  ).show()
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

            loadProfileImage(user.foto)
        }
    }
}