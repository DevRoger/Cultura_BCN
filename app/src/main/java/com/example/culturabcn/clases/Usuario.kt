package com.example.culturabcn.clases

import com.google.gson.annotations.SerializedName
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

abstract class Usuario(
    @SerializedName("id_usuario") val id: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    @SerializedName("contrasenya_hash") private var contrasenaHash: String,
    @SerializedName("fecha_nacimiento") val fechaNacimiento: Date,
    val edad: Int,
    val telefono: String,
    val chat: MutableList<Chat>,
    @SerializedName("foto_url") val foto: String?,
    @SerializedName("id_rol") val idRol: Int,
    val eventos: MutableList<Evento>
                      ) {

    // Metodo para establecer una contraseña segura
    fun establecerContrasena(contrasena: String) {
        contrasenaHash = generarHash(contrasena)
    }

    // Metodo para verificar una contraseña ingresada
    fun verificarContrasena(contrasena: String): Boolean {
        return verificarContraseña(contrasena, contrasenaHash)
    }

    companion object {
        fun generarHash(contrasena: String): String {
            return BCrypt.hashpw(contrasena, BCrypt.gensalt())
        }

        fun verificarContraseña(contrasena: String, hashAlmacenado: String): Boolean {
            return BCrypt.checkpw(contrasena, hashAlmacenado)
        }
    }
}