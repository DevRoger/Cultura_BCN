package com.example.culturabcn.API

import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Evento
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.Usuario
import com.example.culturabcn.clases.UsuarioRegistrat
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/Eventos")
    fun getEventos(): Call<List<Evento>>

    @GET("api/usuarios/rol/1")
    fun getUsuariosRol1(): Call<List<Cliente>>

    @GET("api/usuarios/rol/2")
    fun getUsuariosRol2(): Call<List<Gestor>>

    @GET("api/asientos/eventoasientoscounts/{id}")
    fun getAsientosCount(@Path("id") eventId: Int): Call<Int>

    @GET("api/eventos/byuser/{userId}/reserved")
    fun getReservasPorUsuario(@Path("userId") userId: Int): Call<List<Evento>>

    @Multipart
    @POST("api/usuarios")
    fun postUsuario(
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part("contrasena_hash") contrasenaHash: RequestBody, // *** El nom del camp a l'API és "contrasena_hash" ***
        @Part("fecha_nacimiento") fechaNacimiento: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("id_rol") idRol: RequestBody, // *** El nom del camp a l'API és "id_rol" ***
                   ): Call<UsuarioRegistrat> // *** Esperem un objecte UsuarioRegistrat com a resposta ***



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