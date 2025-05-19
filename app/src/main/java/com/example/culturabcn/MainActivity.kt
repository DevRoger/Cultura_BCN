package com.example.culturabcn

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.ImageView // Importa ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext // Probablement no necessites aquest import
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.RutaImagenDto
import com.example.culturabcn.clases.UserLogged
import com.example.culturabcn.databinding.ActivityMainBinding
import com.example.culturabcn.login.LoginActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    // *** Variables per guardar les dades de l'usuari i l'estat de la imatge del header ***
    private var userFotoUrl: String? = null
    private var userName: String? = null
    private var userEmail: String? = null
    // Flag per saber si ja hem intentat carregar la imatge del header en aquesta sessió
    private var isNavHeaderImageLoaded = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ... (Codi existent per a Onboarding si el fas servir) ...

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_inicio,
                R.id.nav_perfil,
                R.id.nav_mensajes,
                R.id.nav_configuracion,
                R.id.nav_informacion,
                R.id.nav_contacto
                 ), drawerLayout
                                                 )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val btnLogOut = findViewById<Button>(R.id.btn_logout)
        btnLogOut.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        // --- Carregar dades de l'usuari i GUARDAR-les (NO carregar imatge encara) ---

        // Obtenir la vista del header de navegació
        val headerView = binding.navView.getHeaderView(0)
        val txtNombreNav = headerView.findViewById<TextView>(R.id.txtNombreNav)
        val txtCorreoNav = headerView.findViewById<TextView>(R.id.txtCorreoNav)
        // Obtenir referència a la ImageView del header (la necessitem per a quan s'obri el calaix)
        val imgNav = headerView.findViewById<ImageView>(R.id.imgNav)


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
                        val usuarioIniciado = clientes?.find { it.id == UserLogged.userId}

                        if (usuarioIniciado != null) {
                            // *** Guardar les dades de l'usuari a les variables de l'Activity ***
                            userName = usuarioIniciado.nombre + " " + usuarioIniciado.apellidos
                            userEmail = usuarioIniciado.correo
                            userFotoUrl = usuarioIniciado.foto // Guarda la foto URL

                            // Actualitzar TextViews del header immediatament (són sempre visibles si el header es dibuixa)
                            txtNombreNav.text = userName
                            txtCorreoNav.text = userEmail

                            // *** NO carregar la imatge aquí. Es carregarà quan s'obri el calaix. ***
                            // loadNavHeaderImage(imgNav, usuarioIniciado.foto) // ELIMINAT d'aquí

                        } else { Log.e("MainActivity", "Error al recibir el usuario (Cliente): ${response.errorBody()?.string()}") }
                    } else { Log.e("MainActivity", "Error en la respuesta (Cliente): ${response.errorBody()?.string()}") }
                }
                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                    Log.e("MainActivity", "Error de red (Cliente): ${t.message}")
                    Toast.makeText(this@MainActivity, "Error de conexión al cargar datos de usuario.", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Si el usuario es gestor
            RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                override fun onResponse(call: Call<List<Gestor>>, response: Response<List<Gestor>>) {
                    if (response.isSuccessful) {
                        val gestores = response.body()
                        val usuarioIniciado = gestores?.find { it.id == UserLogged.userId}

                        if (usuarioIniciado != null) {
                            // *** Guardar les dades de l'usuari a les variables de l'Activity ***
                            userName = usuarioIniciado.nombre + " " + usuarioIniciado.apellidos
                            userEmail = usuarioIniciado.correo
                            userFotoUrl = usuarioIniciado.foto // Guarda la foto URL

                            // Actualitzar TextViews del header immediatament
                            txtNombreNav.text = userName
                            txtCorreoNav.text = userEmail

                            // *** NO carregar la imatge aquí. Es carregarà quan s'obri el calaix. ***
                            // loadNavHeaderImage(imgNav, usuarioIniciado.foto) // ELIMINAT d'aquí

                        } else { Log.e("MainActivity", "Error al recibir el usuario (Gestor): ${response.errorBody()?.string()}") }
                    } else { Log.e("MainActivity", "Error en la respuesta (Gestor): ${response.errorBody()?.string()}") }
                }
                override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                    Log.e("MainActivity", "Error de red (Gestor): ${t.message}")
                    Toast.makeText(this@MainActivity, "Error de conexión al cargar datos de gestor.", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // --- Afegir Listener al DrawerLayout per carregar la imatge quan s'obri el calaix ---

        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // No fem res durant el moviment
            }

            override fun onDrawerOpened(drawerView: View) {
                // *** Cargar la imatge quan el calaix està COMPLETAMENT obert ***
                Log.d("MainActivity", "Drawer opened. Checking to load nav header image.")
                // Comprovar que la imatge encara no s'ha carregat EN AQUESTA SESSIÓ de l'Activity
                // i que tenim una foto URL vàlida guardada.
                if (!isNavHeaderImageLoaded && !userFotoUrl.isNullOrBlank()) {
                    // Hem obtingut la referència a imgNav abans, reutilitzem-la.
                    // Tot i així, comprovem si imgNav encara és una referència vàlida per seguretat.
                    val imgNav = headerView.findViewById<ImageView>(R.id.imgNav) // Re-trobar per seguretat o utilitzar la variable exterior
                    if (imgNav != null) {
                        Log.d("MainActivity", "Carregant imatge a imgNav quan s'obre el Drawer. URL: $userFotoUrl")
                        loadNavHeaderImage(imgNav, userFotoUrl) // Crida la funció de càrrega amb la URL guardada
                        isNavHeaderImageLoaded = true // Marcar com a carregada per no fer-ho més
                    } else {
                        Log.e("MainActivity", "imgNav not found in nav header view when drawer opened.")
                    }
                } else {
                    if (isNavHeaderImageLoaded) Log.d("MainActivity", "Nav header image already loaded for this session.")
                    if (userFotoUrl.isNullOrBlank()) Log.d("MainActivity", "User photo URL is null or blank, cannot load image.")
                }
                // Opcional: Si les dades de l'usuari (nom, email) es poguessin actualitzar sense tancar i reobrir l'Activity,
                // podries actualitzar els TextViews aquí també amb les variables guardades (userName, userEmail).
            }

            override fun onDrawerClosed(drawerView: View) {
                // No fem res en tancar el calaix, la imatge ja està carregada (o no).
                // Si volguessis que la imatge es recarregués CADA VEGADA que s'obre el calaix,
                // podries afegir aquí: isNavHeaderImageLoaded = false
                // Però carregar només una vegada per sessió de l'Activity és més eficient.
            }

            override fun onDrawerStateChanged(newState: Int) {
                // No necessitem fer res aquí
            }
        })


    } // Fi de onCreate


    // *** Funció auxiliar per carregar la imatge al header de navegació (amb Guardar a Fitxer Temporal) ***
    // Aquesta funció és una adaptació de loadProfileImage de PerfilFragment.
    // El codi d'aquesta funció es manté EXACTAMENT igual que en la resposta anterior.
    private fun loadNavHeaderImage(imageView: ImageView, fotoUrl: String?) {
        // Mostra un placeholder a la ImageView del header
        imageView.setImageResource(R.drawable.side_nav_bar) // Utilitza el teu drawable de placeholder


        if (!fotoUrl.isNullOrBlank()) {
            val rutaImagenDto = RutaImagenDto(Foto_url = fotoUrl)

            RetrofitClient.apiService.postImagen(rutaImagenDto).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val receivedUrlForLog = fotoUrl

                    Log.d("NAV_IMAGE_LOAD", "onResponse (Save to File) per a nav header. Codi: ${response.code()}")

                    var tempFile: File? = null
                    var inputStream: InputStream? = null
                    var outputStream: FileOutputStream? = null

                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            if (responseBody != null) {
                                tempFile = File(this@MainActivity.cacheDir, "nav_header_img_${System.currentTimeMillis()}.temp")
                                inputStream = responseBody.byteStream()
                                outputStream = FileOutputStream(tempFile)
                                inputStream.copyTo(outputStream)

                                if (tempFile.exists() && tempFile.length() > 0) {
                                    Glide.with(this@MainActivity)
                                        .load(tempFile)
                                        .apply(RequestOptions()
                                                   .placeholder(R.drawable.side_nav_bar)
                                                   .error(R.drawable.ic_menu_slideshow))
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .skipMemoryCache(true)
                                        .into(imageView) // Carregar a la ImageView del header

                                    Log.d("NAV_LOAD_SUCCESS_FILE", "Imatge de nav header carregada amb èxit des del fitxer temporal: ${tempFile.absolutePath}")
                                } else {
                                    Log.e("NAV_LOAD_ERROR_FILE", "Fitxer temporal creat buit o inexistent per a $receivedUrlForLog (nav header)")
                                    imageView.setImageResource(R.drawable.ic_menu_slideshow)
                                }

                            } else {
                                Log.e("NAV_LOAD_ERROR_FILE", "API Response exitosa però cos nul loading image to file for $receivedUrlForLog (nav header)")
                                imageView.setImageResource(R.drawable.ic_menu_slideshow)
                            }
                        } else {
                            val statusCode = response.code()
                            val errorBody = response.errorBody()?.string()
                            Log.e("NAV_LOAD_ERROR_FILE", "API Response Error ${statusCode} loading image to file for $receivedUrlForLog (nav header). Body: ${errorBody}")
                            imageView.setImageResource(R.drawable.ic_menu_slideshow)
                        }
                    } catch (e: IOException) {
                        Log.e("NAV_LOAD_ERROR_FILE", "IO Error saving image to file for $receivedUrlForLog (nav header)", e)
                        imageView.setImageResource(R.drawable.ic_menu_slideshow)
                    } catch (e: Exception) {
                        Log.e("NAV_LOAD_ERROR_FILE", "Unexpected Error saving image to file for $receivedUrlForLog (nav header)", e)
                        imageView.setImageResource(R.drawable.ic_menu_slideshow)
                    } finally {
                        try { inputStream?.close() } catch (e: IOException) { Log.e("NAV_LOAD_ERROR_FILE", "Error closing input stream (nav header)", e) }
                        try { outputStream?.close() } catch (e: IOException) { Log.e("NAV_LOAD_ERROR_FILE", "Error closing output stream (nav header)", e) }
                        if (tempFile != null && tempFile.exists()) {
                            val deleted = tempFile.delete()
                            if (deleted) Log.d("NAV_LOAD_SUCCESS_FILE", "Fitxer temporal eliminat (nav header): ${tempFile.absolutePath}")
                            else Log.e("NAV_LOAD_ERROR_FILE", "No s'ha pogut eliminar el fitxer temporal (nav header): ${tempFile.absolutePath}")
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    val receivedUrlForLog = fotoUrl
                    Log.e("NAV_LOAD_FAILURE_FILE", "Network Error loading image to file for $receivedUrlForLog (nav header)", t)
                    imageView.setImageResource(R.drawable.ic_menu_slideshow)
                }
            })
        } else {
            Log.d("NAV_IMAGE_LOAD", "fotoUrl per a nav header és nul·la o buida. Mostrant imatge per defecte.")
            imageView.setImageResource(R.drawable.ic_user) // Utilitza el teu drawable per defecte per al header
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    // Elimina la funció loadProfileImageInDialog si l'has copiat aquí per error.
    // Ha d'estar només a PerfilFragment o ser una utilitat compartida.
}