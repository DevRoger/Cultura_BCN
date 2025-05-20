package com.example.culturabcn.clases

import java.io.Serializable

class Chat(
    val id_chat: Int,
    val mensaje: List<Mensaje>,
    val id_usuario_1: Int,
    val id_usuario_2: Int
          ) : Serializable