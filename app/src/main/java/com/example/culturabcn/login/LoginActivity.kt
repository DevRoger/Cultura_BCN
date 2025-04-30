package com.example.culturabcn.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Evento
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {



    @SuppressLint("MissingInflatedId", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_principal)

        supportActionBar?.hide()

        // Configura el ViewPager2
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.isUserInputEnabled = false // Esto inhabilita el deslizamiento lateral
        val adapter = LoginAdapter(this)
        viewPager.adapter = adapter


        // Prueba API
        /*val call = RetrofitClient.apiService.getEventos()
        call.enqueue(object : Callback<List<Evento>> {
            override fun onResponse(call: Call<List<Evento>>, response: Response<List<Evento>>) {

                Log.e("IniciarSesion", "Código de respuesta: ${response.body()}")

                if (response.isSuccessful) {
                    val gson = Gson()
                    // Si la respuesta es exitosa, mostramos el cuerpo del JSON
                    val jsonResponse = response.body()?.let { gson.toJson(it) } // Convierte la respuesta a un JSON String
                    Log.d("IniciarSesion", "Cuerpo de respuesta: $jsonResponse")
                } else {
                    Log.e("IniciarSesion", "Error en la respuesta: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<Evento>>, t: Throwable) {
                Log.e("IniciarSesion", "Error de red: ${t.message}")
                Toast.makeText(this@LoginActivity, "Error de conexión", Toast.LENGTH_SHORT)
                    .show()
            }
        })*/

    }



}
