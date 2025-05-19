package com.example.culturabcn.ui.mensajes

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Chat
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.RutaImagenDto
import com.example.culturabcn.clases.UserLogged
import com.google.android.material.imageview.ShapeableImageView
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream

class AdapterChats(
    private val chatList: List<Chat>,
    private val id_user_original: Int,
    private val context: Context,
    private val screenListChats: LinearLayout,
    private val screenMessages: LinearLayout,
    private val recyclerMessages: RecyclerView,
    private val panelSendMessages: LinearLayout,
    private val DataChat: TextView
                  ): RecyclerView.Adapter<AdapterChats.ChatViewHolder>()  {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val avatar: ShapeableImageView = itemView.findViewById(R.id.avatar_user)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val element_chat : LinearLayout = itemView.findViewById(R.id.area_chat)
        val last_message : TextView = itemView.findViewById(R.id.last_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        var id_rol : Int
        var id_user_final : Int
        if (chat.idUsuario_1 == id_user_original){
            id_user_final = chat.idUsuario_2
        }else{
            id_user_final = chat.idUsuario_1
        }

        id_rol = RetrofitClient.apiService.getUsuariosRol(id_user_final)

        if (id_rol == 1){
            RetrofitClient.apiService.getUsuariosRol1().enqueue(object :
                                                                    Callback<List<Cliente>> {
                override fun onResponse(call: Call<List<Cliente>>, response: Response<List<Cliente>>) { // Tipus de resposta corregit a List<Cliente>
                    if (response.isSuccessful) {
                        val clientes = response.body()
                        // *** MODIFICACIÓ AQUÍ: Trobar usuari i verificar contrasenya utilitzant BCrypt.checkpw ***
                        val usuarioValido = clientes?.find {
                            it.id == UserLogged.userId }
                        holder.userName.text = usuarioValido!!.nombre
                        setFileImage(usuarioValido.foto!!,holder.avatar)
                        holder.last_message.text = usuarioValido!!.correo
                    }
                }
                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                    // Fallada a nivell de xarxa
                    Log.e("IniciarSesion", "Error de xarxa obtenint Clients: ${t.message}", t)
                    Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show() // Missatge de xarxa genèric
                }
            })
        }else{
            RetrofitClient.apiService.getUsuariosRol2().enqueue(object : Callback<List<Gestor>> {
                override fun onResponse(call: Call<List<Gestor>>, response: Response<List<Gestor>>) {
                    if (response.isSuccessful) {
                        val gestores = response.body()
                        // *** MODIFICACIÓ AQUÍ: Trobar usuari i verificar contrasenya utilitzant BCrypt.checkpw ***
                        val usuarioValido = gestores?.find {
                            it.id == UserLogged.userId  // Compara el correu }
                        }
                        holder.userName.text = usuarioValido!!.nombre
                        setFileImage(usuarioValido.foto!!,holder.avatar)
                        holder.last_message.text = usuarioValido!!.correo
                    }
                }
                override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                    // Fallada a nivell de xarxa
                    Log.e("IniciarSesion", "Error de xarxa obtenint Gestors: ${t.message}", t)
                    Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show() // Missatge de xarxa genèric
                    // Com que aquesta és la segona resposta, si autenticado encara és false, es mostrarà l'error final
                }
            })
        }
        holder.element_chat.setOnClickListener(){
            screenListChats.visibility = View.GONE
            screenMessages.visibility = View.VISIBLE
            panelSendMessages.visibility = View.VISIBLE
        }


    }
    private fun setFileImage(imageUrl: String, avatar: ShapeableImageView) {

        val rutaImagenDto = RutaImagenDto(Foto_url = imageUrl)

        RetrofitClient.apiService.postImagen(rutaImagenDto)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                                       ) {
                    val receivedUrlForLog = imageUrl
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            // *** TEST DE DIAGNOSI: Intentar decodificar manualment a Bitmap ***
                            var inputStream: InputStream? =
                                null // Declarar InputStream fora del try per al finally
                            try {
                                // Obtenim l'InputStream del cos de la resposta
                                inputStream = responseBody.byteStream()
                                // Intentem decodificar l'InputStream directament a un Bitmap
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                if (bitmap != null) {
                                    // *** Si la decodificació a Bitmap té èxit, establim el Bitmap a la ImageView ***
                                    // Aquí no utilitzem Glide per carregar el Bitmap, només el mostrem directament.
                                    avatar.setImageBitmap(bitmap)

                                } else {
                                    // *** Si BitmapFactory.decodeStream retorna nul, les dades no són un Bitmap vàlid ***
                                    // Això passa si les dades no corresponen a un format d'imatge suportat o estan corruptes.

                                    avatar.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                                }

                            } catch (e: IOException) {
                                // Captura errors d'entrada/sortida en llegir el stream

                                avatar.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                            } catch (e: Exception) {
                                // Captura altres possibles excepcions durant la decodificació

                                avatar.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                            } finally {
                                // *** MOLT IMPORTANT: Tancar l'InputStream ***
                                try {
                                    inputStream?.close()
                                } catch (e: IOException) {
                                }
                            }

                        } else {
                            // Resposta exitosa (codi 2xx) però cos nul

                           avatar.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                        }
                    } else {
                        // Crida API no exitosa (codi d'error)
                        val statusCode = response.code()
                        val errorBody = response.errorBody()?.string()
                        avatar.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    avatar.setImageResource(R.drawable.ic_menu_slideshow) // Imatge d'error
                }
            })
    }

}