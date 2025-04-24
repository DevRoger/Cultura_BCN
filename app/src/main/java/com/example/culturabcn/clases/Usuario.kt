package com.example.culturabcn.clases

import org.mindrot.jbcrypt.BCrypt
import java.util.Date

abstract class Usuario(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    private var contrasenaHash: String,
    val fechaNacimiento: Date,
    val edad: Int,
    val telefono: Int,
    val chat: MutableList<Chat>,
    val foto: String?,
    val idRol: Int,
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