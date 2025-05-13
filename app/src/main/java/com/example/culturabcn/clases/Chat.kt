package com.example.culturabcn.clases

import java.io.Serializable

class Chat(
    val id_chat: Int,
    val mensaje: List<Mensaje>,
    val idUsuario_1: Int,
    val idUsuario_2: Int
          ) : Serializable