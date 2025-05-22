package com.example.culturabcn.clases

import java.util.Date

class Mensaje(
    val id_mensaje: Int,
    val id_chat: Int,
    val texto: String,
    val fecha_envio: Date,
    val id_usuario: Int
             ) {
    // Constructor secundario sin id_mensaje
    constructor(id_chat: Int, texto: String, fecha_envio: Date, id_usuario: Int) :
            this(0, id_chat, texto, fecha_envio, id_usuario)
}