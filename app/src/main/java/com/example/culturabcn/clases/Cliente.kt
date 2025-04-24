package com.example.culturabcn.clases

import java.util.Date

class Cliente(
    idUsuario: Int,
    nombre: String,
    apellidos: String,
    correo: String,
    contrasena: String,
    fechaNacimiento: Date,
    edad: Int,
    telefono: Int,
    chat: MutableList<Chat>,
    foto: String?
             ) : Usuario(idUsuario, nombre, apellidos, correo, contrasena, fechaNacimiento, edad, telefono, chat, foto, idRol = 2, eventos = mutableListOf())
