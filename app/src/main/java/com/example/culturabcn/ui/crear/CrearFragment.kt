package com.example.culturabcn.ui.crear

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.Context
import android.media.MediaSyncEvent.createEvent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.Sala
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class CrearFragment : Fragment() {

    private lateinit var edtFecha: EditText
    private lateinit var edtHora: EditText // Hora Inicio
    private lateinit var edtHoraFin: EditText // *** Referència a Hora Fin (si has afegit ID) ***
    private lateinit var edtSala: TextView // TextView per a Sala (simulant Spinner)
    private lateinit var checkBoxNumeradas: CheckBox
    private lateinit var edtFilas: EditText
    private lateinit var edtColumnas: EditText
    private lateinit var edtAforo: EditText
    private lateinit var edtNombre: EditText
    private lateinit var edtDescripcion: EditText
    private lateinit var edtLugar: EditText // *** Referència a Lugar (si has afegit ID) ***
    private lateinit var edtPrecio: EditText
    private lateinit var edtEdadMinima: EditText
    private lateinit var btnCrear: Button // O MaterialButton
    private lateinit var imgEventPhoto: ImageView // *** Referència a la ImageView de la imatge de l'esdeveniment ***

    private lateinit var pickImageLauncher: ActivityResultLauncher<String> // Per a Galeria
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri> // Per a Càmera
    private var cameraPhotoUri: Uri? = null // Uri temporal per a la sortida de la càmera
    private var selectedPhotoUri: Uri? = null // Uri de la foto seleccionada (Galeria o Càmera)

    private var salasList: List<Sala> = emptyList() // Llista de sales carregades des de l'API
    private var selectedSalaId: Int? = null // ID de la sala seleccionada per l'usuari



    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View? {
        val view = inflater.inflate(R.layout.fragment_crear, container, false)

        pickImageLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    selectedPhotoUri = uri
                    // Mostrar la imatge seleccionada a la ImageView
                    imgEventPhoto.setImageURI(uri)
                } else {
                    selectedPhotoUri = null // L'usuari ha cancel·lat la selecció
                    // Opcional: Restablir la ImageView a la imatge per defecte si cal
                    imgEventPhoto.setImageResource(R.drawable.add_image) // Substitueix pel teu drawable per defecte
                }
            }

        takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
                if (success) {
                    // La foto s'ha fet amb èxit, la URI temporal (cameraPhotoUri) conté la imatge
                    selectedPhotoUri =
                        cameraPhotoUri // Guarda la URI de la càmera com la seleccionada
                    // Mostrar la foto feta a la ImageView
                    imgEventPhoto.setImageURI(selectedPhotoUri)
                } else {
                    // L'usuari ha cancel·lat o ha fallat en fer la foto
                    selectedPhotoUri = null
                    cameraPhotoUri = null // Netejar la URI temporal
                    // Opcional: Restablir la ImageView
                    imgEventPhoto.setImageResource(R.drawable.add_image) // Substitueix pel teu drawable per defecte
                }
            }

        edtNombre = view.findViewById(R.id.edtNombre)
        edtDescripcion = view.findViewById(R.id.edtDescripcion)
        edtFecha = view.findViewById(R.id.edtFecha)
        edtHora = view.findViewById(R.id.edtHora) // Hora Inicio
        edtHoraFin = view.findViewById(R.id.edtHoraFin) // *** Referència a Hora Fin ***
        edtSala = view.findViewById(R.id.edtSala) // TextView per a Sala
        checkBoxNumeradas = view.findViewById(R.id.checkBoxNumeradas)
        edtFilas = view.findViewById(R.id.edtFilas)
        edtColumnas = view.findViewById(R.id.edtColumnas)
        edtAforo = view.findViewById(R.id.edtAforo)
        edtLugar = view.findViewById(R.id.edtLugar) // *** Referència a Lugar ***
        edtPrecio = view.findViewById(R.id.edtPrecio)
        edtEdadMinima = view.findViewById(R.id.edtEdadMinima)
        btnCrear =
            view.findViewById(R.id.btnCrear) // O view.findViewById<MaterialButton>(R.id.btnCrear)
        imgEventPhoto = view.findViewById(R.id.imgEventPhoto) // *** Referència a la ImageView ***


        val edtFecha = view.findViewById<EditText>(R.id.edtFecha)

        // Configuració dels Pickers i Listeners existents

        // Fecha DatePickerDialog
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        edtFecha.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format(
                        "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay
                                                    )
                    edtFecha.setText(selectedDate)
                }, year, month, day
                                                   )
            datePickerDialog.show()
        }

        val edtHora = view.findViewById<EditText>(R.id.edtHora)

        // Hora Inicio TimePickerDialog
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        edtHora.setOnClickListener {
            val timePickerDialog =
                TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    val selectedTime = String.format(
                        "%02d:%02d", selectedHour, selectedMinute
                                                    ) // Format "HH:mm"
                    edtHora.setText(selectedTime)
                }, hour, minute, true)  // 'true' per format de 24 hores
            timePickerDialog.show()
        }

        // *** Hora Fin TimePickerDialog (Copia de Hora Inicio, assegura't que la UI tingui un camp per això) ***
        edtHoraFin.setOnClickListener {
            val timePickerDialog =
                TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    val selectedTime = String.format(
                        "%02d:%02d", selectedHour, selectedMinute
                                                    ) // Format "HH:mm"
                    edtHoraFin.setText(selectedTime)
                }, hour, minute, true)  // 'true' per format de 24 hores
            timePickerDialog.show()
        }

        // Configuració de l'AlertDialog per seleccionar Sala
        edtSala.setOnClickListener {
            showSalaPickerDialog()
        }


        val checkBoxNumeradas = view.findViewById<CheckBox>(R.id.checkBoxNumeradas)
        val edtFilas = view.findViewById<EditText>(R.id.edtFilas)
        val edtColumnas = view.findViewById<EditText>(R.id.edtColumnas)
        val edtAforo = view.findViewById<EditText>(R.id.edtAforo)


        // Lògica del CheckBox Numeradas i els camps de Filas/Columnas/Aforo
        checkBoxNumeradas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Si numerades està marcat, habilita Filas/Columnas i deshabilita Aforo
                edtFilas.isEnabled = true
                edtColumnas.isEnabled = true
                edtFilas.setBackgroundResource(R.drawable.rounded_edittext) // Canvia fons per indicar habilitat
                edtColumnas.setBackgroundResource(R.drawable.rounded_edittext)

                edtAforo.isEnabled = false
                edtAforo.setBackgroundResource(R.drawable.rounded_edittext_black) // Canvia fons per indicar deshabilitat
                edtAforo.text.clear() // Neteja el camp Aforo
            } else {
                // Si numerades NO està marcat, deshabilita Filas/Columnas i habilita Aforo
                edtFilas.isEnabled = false
                edtColumnas.isEnabled = false
                edtFilas.setBackgroundResource(R.drawable.rounded_edittext_black)
                edtColumnas.setBackgroundResource(R.drawable.rounded_edittext_black)
                edtFilas.text.clear() // Neteja els camps de Filas/Columnas
                edtColumnas.text.clear()

                edtAforo.isEnabled = true
                edtAforo.setBackgroundResource(R.drawable.rounded_edittext)
            }
        }

        imgEventPhoto.setOnClickListener {
            showImagePickerDialog() // Crida a la funció per mostrar el diàleg
        }

        btnCrear.setOnClickListener {
            createEvent() // Crida a la funció per crear l'esdeveniment
        }



        return view
    }

    // Funció per mostrar el diàleg de selecció d'imatge
    private fun showImagePickerDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona Imágen")
            .setItems(arrayOf("Desde Galería", "Tomar Foto")) { dialog, which ->
                when (which) {
                    0 -> openGallery() // Selecció des de Galeria
                    1 -> openCamera() // Fer foto amb Càmera
                }
            }
            .show()
    }

    // Funció per obrir la galeria d'imatges
    private fun openGallery() {
        // Utilitza el launcher per a la galeria
        pickImageLauncher.launch("image/*") // Sol·licita qualsevol tipus d'imatge
    }

    // Funció per obrir la càmera
    private fun openCamera() {
        // Crear un fitxer temporal Uri on la càmera guardarà la imatge
        cameraPhotoUri = createImageFile()
        if (cameraPhotoUri != null) {
            // Utilitza el launcher per a la càmera, passant la URI on guardar la imatge
            takePictureLauncher.launch(cameraPhotoUri)
        } else {
            Toast.makeText(requireContext(), "Error al preparar para tomar foto.", Toast.LENGTH_SHORT).show()
        }
    }

    // Funció auxiliar per crear un fitxer temporal per a la càmera
    private fun createImageFile(): Uri? {
        val timestamp = System.currentTimeMillis() // Marca de temps per a nom únic
        // Utilitza getExternalFilesDir per guardar fitxers a l'emmagatzematge extern de l'app (no necessita permisos addicionals en versions recents)
        val storageDir = requireContext().getExternalFilesDir(null)

        return try {
            // Crea un fitxer temporal amb prefix, sufix i directori
            val tempFile = File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
            // Utilitza FileProvider per obtenir una Uri segura (content://) per al fitxer temporal
            // Necessites configurar FileProvider al teu AndroidManifest.xml
            FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", tempFile)
        } catch (e: Exception) {
            Log.e("CrearFragment", "Error creating temporary file for camera", e)
            null
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val contentResolver: ContentResolver = context.contentResolver ?: return null
        // Intenta obtenir el nom del fitxer de la Uri
        val fileName = getFileName(contentResolver, uri) ?: "temp_file_${System.currentTimeMillis()}"
        // Crea un fitxer temporal a la cache
        val file = File(context.cacheDir, fileName)

        try {
            // Obre un InputStream des de la Uri
            contentResolver.openInputStream(uri)?.use { inputStream ->
                // Obre un OutputStream per al fitxer temporal
                FileOutputStream(file).use { outputStream ->
                    // Copia el contingut de l'InputStream a l'OutputStream
                    inputStream.copyTo(outputStream)
                }
            }
            // Retorna el fitxer temporal si s'ha pogut copiar el contingut
            return file
        } catch (e: Exception) {
            Log.e("FileHelper", "Error obtenint fitxer des de Uri ${uri.toString()}: ${e.message}", e)
            return null // Retorna null si hi ha un error (p.ex., permisos, fitxer no trobat)
        }
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        var name: String? = null
        // Prova a obtenir el nom utilitzant ContentResolver.query (típic per a content:// Uris)
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            // Si el cursor conté dades (p.ex., per a Uris de MediaStore)
            if (it.moveToFirst()) {
                // Intenta obtenir el nom de la columna DISPLAY_NAME
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        // Si no es pot obtenir el nom amb query (p.ex., per a file:// Uris velles, encara que menys comunes ara)
        if (name == null) {
            // Agafa l'últim segment de la ruta de la Uri com a nom
            name = uri.path?.lastIndexOf('/')?.let { uri.path?.substring(it + 1) }
        }
        return name
    }

    private fun fetchSalas() {
        Log.d("CrearFragment", "Iniciant càrrega de sales des de l'API.")
        // Opcional: Mostrar un indicador de càrrega
        // showLoadingIndicator(true)

        RetrofitClient.apiService.getSalas().enqueue(object : Callback<List<Sala>> {
            override fun onResponse(call: Call<List<Sala>>, response: Response<List<Sala>>) {
                // Opcional: Amagar l'indicador de càrrega
                // showLoadingIndicator(false)

                if (response.isSuccessful) {
                    val salas = response.body()
                    if (salas != null) {
                        salasList = salas // *** Guarda la llista de sales rebuda ***
                        Log.d("CrearFragment", "Salas rebudes amb èxit: ${salasList.size}")
                        // Ara la llista de sales està carregada i llesta per ser utilitzada al diàleg de selecció.
                    } else {
                        Log.e("CrearFragment", "fetchSalas: Resposta exitosa (2xx) però cos nul.")
                        Toast.makeText(requireContext(), "Error al carregar la llista de sales.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Error a la resposta de l'API
                    val statusCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e("CrearFragment", "fetchSalas: Error API. Codi: $statusCode, Error: $errorBody")
                    Toast.makeText(requireContext(), "Error al carregar la llista de sales.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Sala>>, t: Throwable) {
                // Opcional: Amagar l'indicador de càrrega
                // showLoadingIndicator(false)
                // Fallada a nivell de xarxa
                Log.e("CrearFragment", "fetchSalas: Fallo de red", t)
                Toast.makeText(requireContext(), "Error de connexió al carregar sales.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showSalaPickerDialog() {
        // Comprova si la llista de sales s'ha carregat correctament
        if (salasList.isEmpty()) {
            Toast.makeText(requireContext(), "La lista de salas aún no está cargada. Intente de nuevo.", Toast.LENGTH_SHORT).show()
            // Opcional: Tornar a intentar carregar les sales si la llista és buida inesperadament
            fetchSalas()
            return
        }

        // Obtenir només els noms de les sales per mostrar al diàleg
        val salaNames = salasList.map { it.nombre }.toTypedArray() // Converteix la llista de noms a un Array<String>

        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona una sala")
            // Estableix els elements del diàleg utilitzant l'array de noms
            .setItems(salaNames) { dialog, which ->
                // L'índex 'which' correspon a l'índex de l'element seleccionat a l'array salaNames
                val selectedSala = salasList[which] // Obtenir l'objecte Sala corresponent de la llista original
                edtSala.text = selectedSala.nombre // Estableix el text del TextView amb el nom seleccionat
                selectedSalaId = selectedSala.id_sala // *** Guarda la ID real de la sala seleccionada ***
                Log.d("CrearFragment", "Sala seleccionada: ${selectedSala.nombre} (ID: ${selectedSalaId})")
            }
            .show() // Mostra l'AlertDialog
    }

    private fun createEvent() {
        // 1. Recollir dades de tots els camps d'entrada
        val nombre = edtNombre.text.toString().trim()
        val descripcion = edtDescripcion.text.toString().trim()
        val lugar = edtLugar.text.toString().trim() // Del camp Lugar
        val fecha = edtFecha.text.toString().trim() // "yyyy-MM-dd"
        // Afegim ":00" per obtenir el format "HH:mm:ss" que l'API sembla esperar per a TimeSpan
        val horaInicio = edtHora.text.toString().trim() + ":00"
        val horaFin = edtHoraFin.text.toString().trim() + ":00" // Del camp Hora Fin
        val enumerado = checkBoxNumeradas.isChecked // Boolean
        val aforoText = edtAforo.text.toString().trim() // Text de Aforo
        val filasText = edtFilas.text.toString().trim() // Text de Filas
        val columnasText = edtColumnas.text.toString().trim() // Text de Columnas
        val precioText = edtPrecio.text.toString().trim() // Text de Precio
        val edadMinimaText = edtEdadMinima.text.toString().trim() // Text de Edad Mínima
        val salaNombre = edtSala.text.toString().trim() // Nom seleccionat de la Sala

        val idSala = selectedSalaId


        // 2. Realitzar validació de tots els camps
        // AQUESTA ÉS UNA VALIDACIÓ BÀSICA. AMPLIA-LA AMB COMPROVACIONS MÉS ROBUSTES (dates futures, hores coherents, etc.)
        if (nombre.isBlank() || descripcion.isBlank() || lugar.isBlank() || fecha.isBlank() || horaInicio.isBlank() || horaFin.isBlank() || precioText.isBlank() || edadMinimaText.isBlank() || salaNombre.isBlank() || idSala == null) {
            Toast.makeText(requireContext(), "Completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show()
            // TODO: Opcionalment, posa missatges d'error als camps individuals (e.g., edtNombre.error = "Requerit")
            return // Surt si falten camps obligatoris
        }

        // Validar Aforo O Filas/Columnas basant-se en el checkbox
        if (enumerado) { // Si és numerat, validar files i columnes
            val filas = filasText.toIntOrNull()
            val columnas = columnasText.toIntOrNull()
            if (filas == null || columnas == null || filas <= 0 || columnas <= 0) {
                Toast.makeText(requireContext(), "Introduce un número válido para filas y columnas (mayor que 0).", Toast.LENGTH_SHORT).show()
                // TODO: Set error en edtFilas/edtColumnas
                return
            }
            // TODO: Si la API necessita Aforo quan és numerat (p.ex. per mostrar), calcula'l aquí: val aforoCalculado = filas * columnas
        } else { // Si NO és numerat, validar Aforo
            val aforo = aforoText.toIntOrNull()
            if (aforo == null || aforo <= 0) {
                Toast.makeText(requireContext(), "Introduce un número válido para el aforo (mayor que 0).", Toast.LENGTH_SHORT).show()
                // TODO: Set error en edtAforo
                return
            }
        }

        // Validar Precio i Edad Mínima són números vàlids i no negatius
        val precio = precioText.toFloatOrNull()
        val edadMinima = edadMinimaText.toIntOrNull()
        if (precio == null || edadMinima == null || precio < 0 || edadMinima < 0) { // Comprovar que no siguin negatius
            Toast.makeText(requireContext(), "Introduce valores numéricos válidos y no negativos para precio y edad mínima.", Toast.LENGTH_SHORT).show()
            // TODO: Set error en edtPrecio/edtEdadMinima
            return
        }


        // Validar que s'hagi seleccionat una foto
        if (selectedPhotoUri == null) {
            Toast.makeText(requireContext(), "Selecciona una imagen para el evento.", Toast.LENGTH_SHORT).show()
            // Opcional: Indicar visualment que la ImageView necessita una foto (e.g., canviar-li el fons o la vora)
            return // Surt si no hi ha foto seleccionada
        }


        // 3. Obtenir l'objecte File a partir de la Uri seleccionada (o una per defecte si cal, tot i que l'API exigeix fitxer)
        val photoFile: File? = getFileFromUri(requireContext(), selectedPhotoUri!!) // Utilitza la funció auxiliar amb la Uri seleccionada

        // Com que l'API exigeix un fitxer no buit, comprovem si hem pogut obtenir el File
        if (photoFile == null || !photoFile.exists() || photoFile.length() == 0L) { // Comprovem també que la mida no sigui 0
            Toast.makeText(requireContext(), "Error al obtener la imagen seleccionada.", Toast.LENGTH_SHORT).show()
            Log.e("CrearFragment", "No es pudo obtener el archivo de la foto seleccionada a partir de la URI o el archivo está vacío.")
            // Opcional: Neteja la Uri seleccionada i la imatge mostrada
            // selectedPhotoUri = null
            // imgEventPhoto.setImageResource(R.drawable.add_image)
            return // Surt si no es pot obtenir el fitxer vàlid
        }
        Log.d("CrearFragment", "Fitxer de foto a enviar per crear esdeveniment: ${photoFile.name}, Mida: ${photoFile.length()} bytes")


        // 4. Preparar les PARTS de la petició multipart
        // Assegura't que els noms de les parts ("nombre", "descripcion", etc.) coincideixin EXACTAMENT amb els que l'API espera.
        val nombrePart = nombre.toRequestBody("text/plain".toMediaTypeOrNull())
        val descripcionPart = descripcion.toRequestBody("text/plain".toMediaTypeOrNull())
        val lugarPart = lugar.toRequestBody("text/plain".toMediaTypeOrNull())
        val fechaPart = fecha.toRequestBody("text/plain".toMediaTypeOrNull()) // Format "yyyy-MM-dd"
        val horaInicioPart = horaInicio.toRequestBody("text/plain".toMediaTypeOrNull()) // Format "HH:mm:ss"
        val horaFinPart = horaFin.toRequestBody("text/plain".toMediaTypeOrNull()) // Format "HH:mm:ss"
        val precioPart = precioText.toRequestBody("text/plain".toMediaTypeOrNull()) // L'API parsejarà a decimal
        val enumeradoPart = enumerado.toString().toRequestBody("text/plain".toMediaTypeOrNull()) // "true" o "false"
        val edadMinimaPart = edadMinimaText.toRequestBody("text/plain".toMediaTypeOrNull()) // L'API parsejarà a int
        val idSalaPart = idSala.toString().toRequestBody("text/plain".toMediaTypeOrNull()) // Ús selectedSalaId aquí

        // *** Preparar la part de l'aforo condicionalment ***
        val aforoPart: RequestBody? = if (!enumerado) { // Si NO és numerat
            // Ja hem validat que aforoText és un int vàlid si no és numerat
            aforoText.toRequestBody("text/plain".toMediaTypeOrNull())
        } else {
            null // Si és numerat, enviem null per a l'aforo
        }


        // 5. Preparar MultipartBody.Part per a la foto
        // "photo" ha de coincidir EXACTAMENT amb el nom que espera l'API a HttpContext.Current.Request.Files["photo"]
        // Intenta obtenir el tipus MIME real de la Uri si és possible per més precisió
        val photoMimeType = requireContext().contentResolver.getType(selectedPhotoUri!!) ?: "image/*" // Obtenir tipus MIME o usar genèric
        val photoRequestBody = photoFile.asRequestBody(photoMimeType.toMediaTypeOrNull())
        val photoPart = MultipartBody.Part.createFormData("photo", photoFile.name, photoRequestBody) // Nom de la part "photo"


        // 6. Fer la crida a l'API POST per crear l'esdeveniment
        RetrofitClient.apiService.postEvento(
            nombrePart,
            descripcionPart,
            lugarPart,
            fechaPart,
            horaInicioPart,
            horaFinPart,
            precioPart,
            enumeradoPart,
            edadMinimaPart,
            idSalaPart,
            aforoPart,
            photoPart // Incloure la part de la foto
                                            ).enqueue(object : Callback<Evento> { // Esperem que l'API retorni un objecte Evento

            override fun onResponse(call: Call<Evento>, response: Response<Evento>) {
                // S'executa al fil principal (UI thread)
                // Netejar el fitxer temporal un cop la petició ha acabat
                photoFile.delete()
                Log.d("CrearFragment", "Fitxer temporal de foto eliminat després de la petició POST.")


                if (response.isSuccessful) {
                    val nuevoEvento: Evento? = response.body()
                    if (nuevoEvento != null) {
                        Log.d("CrearFragment", "Esdeveniment creat amb èxit. ID: ${nuevoEvento.id_evento}, Nom: ${nuevoEvento.nombre}")
                        Toast.makeText(requireContext(), "Esdeveniment creat amb èxit!", Toast.LENGTH_LONG).show()

                        // *** TODO: Acció després de la creació exitosa ***
                        // Normalment:
                        // - Netejar el formulari
                        // clearForm() // Implementa una funció per netejar tots els camps
                        // - O navegar a una altra pantalla (p.ex., la llista d'esdeveniments)
                        // requireActivity().supportFragmentManager.popBackStack() // Exemple: tornar enrere en la pila de fragments
                        // findNavController().navigate(...) // Si utilitzes Navigation Component

                    } else {
                        Log.e("CrearFragment", "Creació d'esdeveniment exitosa (codi 2xx), però cos de resposta nul.")
                        Toast.makeText(requireContext(), "Esdeveniment creat, però dades de resposta buides.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // La petició va fallar (codi 400, 500, etc.)
                    val statusCode = response.code()
                    val errorBody = response.errorBody()?.string()
                    Log.e("CrearFragment", "Error creant esdeveniment. Codi: $statusCode, Error: $errorBody")
                }
            }

            override fun onFailure(call: Call<Evento>, t: Throwable) {
                // Fallada a nivell de xarxa o excepció
                photoFile.delete() // Neteja el fitxer temporal
                Log.e("CrearFragment", "Fallo de red al crear esdeveniment", t)
                Toast.makeText(requireContext(), "Error de connexió al crear esdeveniment.", Toast.LENGTH_SHORT).show()
            }
        })

    }





}
