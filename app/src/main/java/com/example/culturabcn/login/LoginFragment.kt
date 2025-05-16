package com.example.culturabcn.login

import android.content.Intent
import android.os.Bundle
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
import com.example.culturabcn.clases.UserLogged
import com.example.culturabcn.clases.Usuario
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response



class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnIniciar = view.findViewById<Button>(R.id.btnIniciar)

        // Comprobación inicio de sesión
        val edtCorreo = view.findViewById<EditText>(R.id.edtCorreo)
        val edtContrasenya = view.findViewById<EditText>(R.id.edtContrasenya) // Contrasenya en text pla introduïda per l'usuari

        btnIniciar.setOnClickListener {
            edtCorreo.setBackgroundResource(R.drawable.rounded_edittext)
            edtContrasenya.setBackgroundResource(R.drawable.rounded_edittext)
            val correo = edtCorreo.text.toString().trim()
            val contrasena = edtContrasenya.text.toString().trim() // Contrasenya en text pla introduïda per l'usuari

            if (correo.isNotEmpty() && contrasena.isNotEmpty()) {

                var autenticado = false
                var respuestasRecibidas = 0

                fun manejarRespuestaFinal() {
                    // Aquesta funció s'executa després de rebre les dues respostes API si l'autenticació encara no ha passat
                    respuestasRecibidas++
                    if (respuestasRecibidas == 2 && !autenticado) {
                        Toast.makeText(requireContext(), "Correo y/o contraseña incorrectos.", Toast.LENGTH_SHORT).show()
                        edtCorreo.setBackgroundResource(R.drawable.rounded_edittext_error)
                        edtContrasenya.setBackgroundResource(R.drawable.rounded_edittext_error)
                    }
                }

                // Crida per obtenir llistat de Clients (Rol 1)
                RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                    override fun onResponse(call: Call<List<Cliente>>, response: Response<List<Cliente>>) { // Tipus de resposta corregit a List<Cliente>
                        if (response.isSuccessful) {
                            val clientes = response.body()
                            // *** MODIFICACIÓ AQUÍ: Trobar usuari i verificar contrasenya utilitzant BCrypt.checkpw ***
                            val usuarioValido = clientes?.find {
                                it.correo == correo && // Compara el correu
                                        // *** Utilitza la funció de verificació de BCrypt que ja tens a la classe Usuario ***
                                        // Aquesta funció (BCrypt.checkpw internament) compara la contrasenya PLANA amb el HASH BCrypt
                                        try {
                                            // Assegura't que el hash rebut no sigui nul o buit abans de verificar
                                            !it.contrasenaHash.isNullOrBlank() &&
                                                    Usuario.Companion.verificarContraseña(contrasena, it.contrasenaHash)
                                        } catch (e: Exception) {
                                            // Captura possibles excepcions de BCrypt.checkpw si el hash no té el format esperat
                                            Log.e("Login", "Error verificant hash BCrypt per a l'usuari amb correu ${it.correo}", e)
                                            false // Considera que la verificació falla en cas d'error
                                        }
                            }

                            if (usuarioValido != null) {
                                // *** AUTENTICACIÓ EXITOSA COM A CLIENT (Verificació BCrypt passada) ***
                                autenticado = true // Marca com autenticat
                                Toast.makeText(requireContext(), "Inicio de sesión con éxito", Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireActivity(), MainActivity::class.java) // Crea Intent per a MainActivity
                                UserLogged.userId = usuarioValido.id // Estableix UserLogged ID
                                UserLogged.rolId = usuarioValido.idRol // Estableix UserLogged Rol ID

                                startActivity(intent) // Inicia MainActivity
                                requireActivity().finish() // Acaba l'Activity que hosteja aquest Fragment
                            } else {
                                // Usuari no trobat per correu O verificació BCrypt ha fallat
                                manejarRespuestaFinal() // Espera la resposta de Gestor abans de mostrar error final
                            }
                        } else {
                            // Error a la resposta de l'API (no 2xx)
                            Log.e("IniciarSesion", "Error a la resposta de Clients: ${response.code()}, Cos: ${response.errorBody()?.string()}")
                            manejarRespuestaFinal() // Espera la resposta de Gestor
                        }
                    }

                    override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                        // Fallada a nivell de xarxa
                        Log.e("IniciarSesion", "Error de xarxa obtenint Clients: ${t.message}", t)
                        Toast.makeText(requireContext(), "Error de connexió", Toast.LENGTH_SHORT).show() // Missatge de xarxa genèric
                        manejarRespuestaFinal() // Espera la resposta de Gestor
                    }
                })

                // Crida per obtenir llistat de Gestors (Rol 2)
                RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                    override fun onResponse(call: Call<List<Gestor>>, response: Response<List<Gestor>>) {
                        if (response.isSuccessful) {
                            val gestores = response.body()
                            // *** MODIFICACIÓ AQUÍ: Trobar usuari i verificar contrasenya utilitzant BCrypt.checkpw ***
                            val usuarioValido = gestores?.find {
                                it.correo == correo && // Compara el correu
                                        // *** Utilitza la funció de verificació de BCrypt que ja tens a la classe Usuario ***
                                        try {
                                            !it.contrasenaHash.isNullOrBlank() &&
                                                    Usuario.Companion.verificarContraseña(contrasena, it.contrasenaHash)
                                        } catch (e: Exception) {
                                            Log.e("Login", "Error verificant hash BCrypt per a l'usuari amb correu ${it.correo}", e)
                                            false // Considera que la verificació falla
                                        }
                            }

                            if (usuarioValido != null) {
                                // *** AUTENTICACIÓ EXITOSA COM A GESTOR (Verificació BCrypt passada) ***
                                autenticado = true // Marca com autenticat
                                Toast.makeText(requireContext(), "Inicio de sesión con éxito", Toast.LENGTH_SHORT).show()
                                val intent = Intent(requireActivity(), MainActivity::class.java) // Crea Intent per a MainActivity
                                UserLogged.userId = usuarioValido.id // Estableix UserLogged ID
                                UserLogged.rolId = usuarioValido.idRol // Estableix UserLogged Rol ID
                                startActivity(intent) // Inicia MainActivity
                                requireActivity().finish() // Acaba l'Activity que hosteja aquest Fragment
                            } else {
                                // Usuari no trobat per correu O verificació BCrypt ha fallat
                                manejarRespuestaFinal() // Com que aquesta és la segona resposta, si autenticado encara és false, es mostrarà l'error final
                            }
                        } else {
                            // Error a la resposta de l'API (no 2xx)
                            Log.e("IniciarSesion", "Error a la resposta de Gestors: ${response.code()}, Cos: ${response.errorBody()?.string()}")
                            manejarRespuestaFinal() // Com que aquesta és la segona resposta, si autenticado encara és false, es mostrarà l'error final
                        }
                    }

                    override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                        // Fallada a nivell de xarxa
                        Log.e("IniciarSesion", "Error de xarxa obtenint Gestors: ${t.message}", t)
                        Toast.makeText(requireContext(), "Error de connexió", Toast.LENGTH_SHORT).show() // Missatge de xarxa genèric
                        manejarRespuestaFinal() // Com que aquesta és la segona resposta, si autenticado encara és false, es mostrarà l'error final
                    }
                })

            } else {
                // Camps de correu o contrasenya buits
                edtCorreo.setBackgroundResource(R.drawable.rounded_edittext_error)
                edtContrasenya.setBackgroundResource(R.drawable.rounded_edittext_error)
                Toast.makeText(requireContext(), "Correo o contraseña vacíos", Toast.LENGTH_SHORT).show()
            }
        } // Fi de btnIniciar.setOnClickListener


        // ... (Listeners per a txtRegistrar i btnOlvidada) ...

        val txtRegistrar = view.findViewById<TextView>(R.id.txtRegistrar)
        val btnOlvidada = view.findViewById<TextView>(R.id.btnOlvidada)

        // Listener per al text "Registrar" - CANVIAR A LA PÀGINA DE REGISTRE AL VIEWPAGER
        txtRegistrar.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 1 // *** Assegura't que l'índex 1 és el Fragment de Registre ***
        }

        // Listener per al text "Olvidada" - BOTÓ PROVISIONAL PER ACCEDIR A LA RECUPERACIÓ
        btnOlvidada.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 4 // *** Assegura't que l'índex 4 és el Fragment de Recuperació ***
        }


    }



}