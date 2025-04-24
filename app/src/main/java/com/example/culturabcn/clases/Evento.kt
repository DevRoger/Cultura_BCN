package com.example.culturabcn.clases

import java.sql.Time
import java.util.Date

data class Evento(
    val id_evento: Int,
    val nombre: String,
    val fecha: Date,
    val hora_inicio: Time,
    val hora_fin: Time,
    val descripcion: String,
    val lugar: String,
    val aforo: Int,
    val enumerada: Boolean,
    val precio: Float,
    val edad_minima: Int
                 )