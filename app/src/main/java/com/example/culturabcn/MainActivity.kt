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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.UserLogged
import com.example.culturabcn.databinding.ActivityMainBinding
import com.example.culturabcn.login.LoginActivity
import com.example.culturabcn.ui.perfil.PerfilFragment
import com.google.android.material.navigation.NavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //___________________________________________________________________________________________
        // Esto es el salto del opnboarding en caso de que el usuario ya lo haya visto
        /*
        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val onboardingCompleted = sharedPref.getBoolean("onboardingCompleted", false)

        if (onboardingCompleted) {
            // Si ya se completó el onboarding, ir directamente a la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Si no se completó el onboarding, mostrar la actividad de onboarding
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }*/
        //___________________________________________________________________________________________



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
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
        // Botón de cerrar sesión
        btnLogOut.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

            this.finish()
        }



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

                            val headerView = binding.navView.getHeaderView(0)
                            val txtNombreNav = headerView.findViewById<TextView>(R.id.txtNombreNav)
                            val txtCorreoNav = headerView.findViewById<TextView>(R.id.txtCorreoNav)

                            txtNombreNav.text = usuarioIniciado.nombre + " " + usuarioIniciado.apellidos
                            txtCorreoNav.text = usuarioIniciado.correo

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
                    Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
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


                            val headerView = binding.navView.getHeaderView(0)
                            val txtNombreNav = headerView.findViewById<TextView>(R.id.txtNombreNav)
                            val txtCorreoNav = headerView.findViewById<TextView>(R.id.txtCorreoNav)

                            txtNombreNav.text = usuarioIniciado.nombre + " " + usuarioIniciado.apellidos
                            txtCorreoNav.text = usuarioIniciado.correo


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
                    Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}