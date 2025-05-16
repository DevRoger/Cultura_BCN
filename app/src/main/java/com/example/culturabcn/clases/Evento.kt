package com.example.culturabcn.clases

import com.google.gson.annotations.SerializedName
import java.sql.Time
import java.util.Date

data class Evento(
    val id_evento: Int,
    val nombre: String,
    val fecha: String,
    val hora_inicio: String,
    val hora_fin: String,
    val descripcion: String,
    val lugar: String,
    val aforo: Int,
    val enumerada: Boolean,
    val precio: Float,
    val edad_minima: Int,
    @SerializedName("foto_url") val foto_url: String?
                 )