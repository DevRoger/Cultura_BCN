package com.example.culturabcn.clases

import com.google.gson.annotations.SerializedName

data class Sala(
    @SerializedName("id_sala") val id_sala: Int,
    val nombre: String,
    val direccion: String,
    val aforo: Int,
    )
