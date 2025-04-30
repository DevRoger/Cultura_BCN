package com.example.culturabcn.clases

import java.util.Date

class Cliente(
    id: Int,
    nombre: String,
    apellidos: String,
    correo: String,
    contrasena: String,
    fechaNacimiento: Date,
    edad: Int,
    telefono: String,
    chat: MutableList<Chat>,
    foto: String?
             ) : Usuario(id, nombre, apellidos, correo, contrasena, fechaNacimiento, edad, telefono, chat, foto, idRol = 2, eventos = mutableListOf())
