package com.example.culturabcn.API

import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.Usuario
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/Eventos")
    fun getEventos(): Call<List<Evento>>

    @GET("api/usuarios/rol/1")
    fun getUsuariosRol1(): Call<List<Cliente>>

    @GET("api/usuarios/rol/2")
    fun getUsuariosRol2(): Call<List<Gestor>>

    /*
    @GET("api/Local")
    suspend fun getLocales(): List<Local>

    @GET("api/Evento")
    suspend fun getEventos(): List<Evento>

    @GET("api/Musico")
    fun getMusicos(): Call<List<Musico>>

    @GET("api/Musico")
    fun getMusicoByCorreo(@Query("correo") correo: String): Call<Musico>

    @GET("api/Local")
    fun getLocalByCorreo(@Query("correo") correo: String): Call<Local>

    @POST("api/Musico")
    fun postMusico(@Body musico: Musico): Call<Boolean>

    @POST("api/Local")
    fun postLocal(@Body local: Local): Call<Boolean>

    @POST("api/Evento")
    fun postEvento(@Body evento: Evento): Call<Boolean>

    @PUT("api/Musico/{id}")
    suspend fun actualizarMusico(
        @Path("id") id: Int,
        @Body musico: DataTransferObjectUsuario): Response<Unit>

    @DELETE("api/Evento/{id}")
    fun deleteEvento(@Path("id") id: Int): Call<Evento>*/
}