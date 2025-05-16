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
        @Part("contrasena_hash") contrasenaHash: RequestBody,
        @Part("fecha_nacimiento") fechaNacimiento: RequestBody,
        @Part("telefono") telefono: RequestBody,
        @Part("id_rol") idRol: RequestBody,
        @Part photo: MultipartBody.Part
                   ): Call<UsuarioRegistrat>

    @Multipart // *** Indica que és una petició multipart ***
    // La ruta base és 'api/usuarios'. L'API llegeix l'ID del formulari.
    @PUT("api/usuarios") // *** Utilitza la ruta base de l'endpoint PUT ***
    fun putUsuario(
        // *** Totes les dades s'envien com a parts del formulari multipart ***
        @Part("id_usuario") idUsuario: RequestBody, // L'API llegeix id_usuario del formulari
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("correo") correo: RequestBody,
        @Part("foto_url") fotoUrl: RequestBody, // L'API llegeix foto_url del formulari (url ACTUAL de la foto)
        @Part("contrasena_hash") contrasenaHash: RequestBody, // L'API llegeix contrasena_hash del formulari (HASH ACTUAL)
        @Part("fecha_nacimiento") fechaNacimiento: RequestBody, // L'API llegeix fecha_nacimiento del formulari
        @Part("telefono") telefono: RequestBody, // L'API llegeix telefono del formulari
        @Part("id_rol") idRol: RequestBody, // L'API llegeix id_rol del formulari
        @Part photo: MultipartBody.Part // *** La part del fitxer "photo" és obligatòria segons l'API ***
                  ): Call<Boolean> // L'API retorna Ok(true), que es mapeja a Boolean



}