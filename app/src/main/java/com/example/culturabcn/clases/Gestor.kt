package com.example.culturabcn.clases

import java.io.Serializable
import java.util.Date

class Gestor(
    id: Int,
    nombre: String,
    apellidos: String,
    correo: String,
    contrasena: String,
    fechaNacimiento: String,
    edad: Int,
    telefono: String,
    chat: MutableList<Chat>,
    foto: String?
            ) : Usuario(id, nombre, apellidos, correo, contrasena, fechaNacimiento, edad, telefono, chat, foto, idRol = 1, eventos = mutableListOf()), Serializable
