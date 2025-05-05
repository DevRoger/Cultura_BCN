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
import com.example.culturabcn.clases.Gestor
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

                var autenticado = false
                var respuestasRecibidas = 0

                fun manejarRespuestaFinal() {
                    respuestasRecibidas++
                    if (respuestasRecibidas == 2 && !autenticado) {
                        Toast.makeText(requireContext(), "Correo y/o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
                    }
                }

                // Cliente
                RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                    override fun onResponse(call: Call<List<Cliente>>, response: Response<List<Cliente>>) {
                        if (response.isSuccessful) {
                            val clientes = response.body()
                            val usuarioValido = clientes?.find { it.correo == correo && it.contrasenaHash == contrasena }

                            if (usuarioValido != null) {
                                autenticado = true
                                Toast.makeText(requireContext(), "Inicio de sesión con éxito", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(requireActivity(), MainActivity::class.java))
                                requireActivity().finish()
                            } else {
                                manejarRespuestaFinal()
                            }
                        } else {
                            Log.e("IniciarSesion", "Error en la respuesta: ${response.errorBody()?.string()}")
                            manejarRespuestaFinal()
                        }
                    }

                    override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                        Log.e("IniciarSesion", "Error de red: ${t.message}")
                        Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                        manejarRespuestaFinal()
                    }
                })

                // Gestor
                RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                    override fun onResponse(call: Call<List<Gestor>>, response: Response<List<Gestor>>) {
                        if (response.isSuccessful) {
                            val gestores = response.body()
                            val usuarioValido = gestores?.find { it.correo == correo && it.contrasenaHash == contrasena }

                            if (usuarioValido != null) {
                                autenticado = true
                                Toast.makeText(requireContext(), "Inicio de sesión con éxito", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(requireActivity(), MainActivity::class.java))
                                requireActivity().finish()
                            } else {
                                manejarRespuestaFinal()
                            }
                        } else {
                            Log.e("IniciarSesion", "Error en la respuesta: ${response.errorBody()?.string()}")
                            manejarRespuestaFinal()
                        }
                    }

                    override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                        Log.e("IniciarSesion", "Error de red: ${t.message}")
                        Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                        manejarRespuestaFinal()
                    }
                })

            } else {
                Toast.makeText(requireContext(), "Correo o contraseña vacíos", Toast.LENGTH_SHORT).show()
            }
        }



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