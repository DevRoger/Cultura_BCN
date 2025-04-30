package com.example.culturabcn.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.MainActivity
import com.example.culturabcn.R
import com.example.culturabcn.clases.Cliente
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class LoginFragment : Fragment(R.layout.fragment_login) {

    companion object {
        private const val AES = "AES"
        private const val SECRET_KEY = "1234567890123456"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnIniciar = view.findViewById<Button>(R.id.btnIniciar)

        // Comprobación inicio de sesión
        val edtCorreo = view.findViewById<EditText>(R.id.edtCorreo)
        val edtContrasenya = view.findViewById<EditText>(R.id.edtContrasenya)

        btnIniciar.setOnClickListener {
            val correo = edtCorreo.text.toString().trim()
            val contrasena = edtContrasenya.text.toString().trim()

            if (correo.isNotEmpty() && contrasena.isNotEmpty()) {

                val call = RetrofitClient.apiService.getUsuariosRol1()
                call.enqueue(object : Callback<List<Cliente>> {
                    override fun onResponse(
                        call: Call<List<Cliente>>, response: Response<List<Cliente>>
                                           ) {

                        // Verificamos si la respuesta es exitosa
                        if (response.isSuccessful) {
                            // Accedemos al cuerpo de la respuesta
                            val clientes = response.body()

                            if (clientes != null) {
                                // Iteramos sobre la lista de clientes y mostramos sus datos en los logs
                                for (cliente in clientes) {
                                    Log.d("IniciarSesion", "Cliente ID: ${cliente.id}")
                                    Log.d("IniciarSesion", "Nombre: ${cliente.nombre} ${cliente.apellidos}")
                                    Log.d("IniciarSesion", "Correo: ${cliente.correo}")
                                    Log.d("IniciarSesion", "Telefono: ${cliente.telefono}")
                                    Log.d("IniciarSesion", "Fecha de Nacimiento: ${cliente.fechaNacimiento}")
                                    Log.d("IniciarSesion", "Edad: ${cliente.edad}")
                                    Log.d("IniciarSesion", "Foto: ${cliente.foto}")
                                }
                            } else {
                                Log.e("IniciarSesion", "Respuesta vacía o nula")
                            }

                        } else {
                            // Si la respuesta no fue exitosa, mostramos el error
                            Log.e("IniciarSesion", "Error en la respuesta: ${response.errorBody()?.string()}")
                        }

                        if (response.isSuccessful) {
                            // Redirigir al usuario a la MainActivity
                            val intent = Intent(requireActivity(), MainActivity::class.java)
                            startActivity(intent)

                            requireActivity().finish()
                        }

                    }

                    override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                        Log.e("IniciarSesion", "Error de red: ${t.message}")
                        Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT)
                            .show()
                    }
                })

            } else {
                // Correo y contraseña vacíos
                Toast.makeText(requireContext(), "Correo o contraseña vacíos", Toast.LENGTH_SHORT).show()

            }

        }

        /*
        -------------------------------------------------------------------
        AQUI FALTA LA COMPROBACIÓN DEL USUARIO Y LA CONTRASEÑA INTRODUCIDOS
        -------------------------------------------------------------------
         */

        val txtRegistrar = view.findViewById<TextView>(R.id.txtRegistrar)
        val btnOlvidada = view.findViewById<TextView>(R.id.btnOlvidada)


        txtRegistrar.setOnClickListener {
            val viewPager2 =
                requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 1
        }


        // BOTÓN PROVISIONAL PARA ACCEDER A LA RECUPERACIÓN
        btnOlvidada.setOnClickListener {
            val viewPager2 =
                requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 4
        }




    }

    // Metodo para desencriptar las contraseñas obtenidas de la BBDD
    @SuppressLint("GetInstance")
    fun decryptAES(encryptedData: String): String {
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), AES)
        val cipher = Cipher.getInstance(AES)
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
        val decrypted = cipher.doFinal(decoded)
        return String(decrypted)
    }

    // Metodo para encriptar las contraseñas antes de enviarlas a la BBDD
    @SuppressLint("GetInstance")
    fun encryptAES(data: String): String {
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), AES)
        val cipher = Cipher.getInstance(AES)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encrypted = cipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }
}