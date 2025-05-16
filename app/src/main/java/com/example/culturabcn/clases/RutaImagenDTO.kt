package com.example.culturabcn.clases

import com.google.gson.annotations.SerializedName

// Classe de dades per al cos de la petició POST api/usuarios/imagen
data class RutaImagenDto(
    // Utilitza @SerializedName per assegurar que el nom del camp JSON coincideixi amb l'esperat per l'API
    @SerializedName("Foto_url") val Foto_url: String // El nom ha de coincidir amb el de l'API (case-sensitive JSON)
                        )