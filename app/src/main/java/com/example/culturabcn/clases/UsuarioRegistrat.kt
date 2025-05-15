package com.example.culturabcn.clases // Assegura't que el paquet sigui correcte

import com.google.gson.annotations.SerializedName
import java.util.Date // Potser necessites Date o String aquí, depèn de com l'API serialitzi fecha_nacimiento
// import com.example.culturabcn.clases.Chat // Importa si les llistes Chat o Evento es retornen al POST
// import com.example.culturabcn.clases.Evento // Importa si les llistes Chat o Evento es retornen al POST


// Aquesta classe de dades servirà per parsejar la resposta JSON de l'API quan el POST sigui exitós.
// Hauria de coincidir amb les propietats de l'objecte 'usuarios' que retorna la teva API.
data class UsuarioRegistrat(
    @SerializedName("id_usuario") val id: Int,
    val nombre: String,
    val apellidos: String,
    val correo: String,
    @SerializedName("contrasena_hash") val contrasenaHash: String, // L'API retorna el hash que ha rebut
    // Adapta el tipus de 'fechaNacimiento' si l'API retorna un format diferent a String o Date
    @SerializedName("fecha_nacimiento") val fechaNacimiento: String, // O Date
    // Adapta el tipus de 'edad' si l'API retorna un tipus anul·lable o diferent
    val edad: Int, // Int com a la teva classe abstracta Usuario
    val telefono: String,
    // Adapta si les llistes de chat o eventos es retornen al POST (sovint no ho fan o estan buides)
    // Si es retornen, podrien ser llistes buides o null, així que Int?, List<Chat>?, List<Evento>? podrien ser més segurs.
    // Aquí les posem com a exemple, però verifica la resposta real de l'API.
    val chat: List<Chat>?,
    @SerializedName("foto_url") val foto_url: String?, // Coincideix amb el nom del camp a l'API i a la teva classe Usuario
    @SerializedName("id_rol") val idRol: Int,
    val eventos: List<Evento>?
                           )