package com.example.culturabcn.clases

import java.util.Date

class Mensaje(
    val id_mensaje: Int,
    val id_chat: Int,
    val mensaje: String,
    val fechaEnvio: Date,
    val nombreUsuario: String
             )