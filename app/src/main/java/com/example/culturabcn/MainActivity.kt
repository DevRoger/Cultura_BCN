package com.example.culturabcn

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Gestor
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


        // Aquí deberías agregar el fragmento al contenedor correspondiente
        /*supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, perfilFragment)
            .commit()*/


        /*if (idRol == 1) {
            // Aquí haces la consulta con el ID

            RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                override fun onResponse(call: Call<List<Cliente>>, response: Response<List<Cliente>>) {
                    if (response.isSuccessful) {
                        val clientes = response.body()
                        val usuarioValido = clientes?.find { it.correo == correo && it.contrasenaHash == contrasena }

                        if (usuarioValido != null) {
                            autenticado = true
                            Toast.makeText(requireContext(), "Inicio de sesión con éxito", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            intent.putExtra("usuario_id", usuarioValido.id)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {

                        }
                    } else if (idRol == 2 ) {

                    } else {
                        Log.e("IniciarSesion", "Error en la respuesta: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                    Log.e("IniciarSesion", "Error de red: ${t.message}")
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Manejo de error
        }

        Log.d("UsuarioIniciado", "Usuario iniciado: $usuarioIniciado")*/


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
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}