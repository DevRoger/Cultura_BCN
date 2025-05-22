package com.example.culturabcn.ui.mensajes

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Chat
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.Mensaje
import com.example.culturabcn.clases.RutaImagenDto
import com.example.culturabcn.ui.mensajes.AdapterChats.ChatViewHolder
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.io.InputStream

class AdapterMensajes(
    private val messageList: List<Mensaje>,
    private val context: Context,
    private val id_user_original: Int,
    private val lifecycleScope: LifecycleCoroutineScope

                     ): RecyclerView.Adapter<AdapterMensajes.AdapterViewHolder>() {

    class AdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var body = itemView.findViewById<LinearLayout>(R.id.message_body)
        var avatar = itemView.findViewById<ShapeableImageView>(R.id.avatar)
        var titleNameUser = itemView.findViewById<TextView>(R.id.user_name)
        var message = itemView.findViewById<TextView>(R.id.message_data)
        var date = itemView.findViewById<TextView>(R.id.date)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
                                   ): AdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.element_message, parent, false)
        return AdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdapterMensajes.AdapterViewHolder, position: Int) {
        var message = messageList[position]

        var id_rol : Int
        if (message.id_usuario == id_user_original){
            holder.body.background = holder.itemView.context.getDrawable(R.drawable.degradado_button)
            holder.titleNameUser.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.date.setTextColor(ContextCompat.getColor(context, R.color.white))
            holder.message.setTextColor(ContextCompat.getColor(context, R.color.white))
            val params = holder.body.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.END
            params.marginStart = 385 // Margen izquierdo para mensajes enviados
            params.marginEnd = 0  // Margen derecho reducido
            holder.body.layoutParams = params
        }

        lifecycleScope.launch {
            id_rol = -1
            try {
                val response = RetrofitClient.apiService.getUsuariosRol(message.id_usuario)
                if (response.isSuccessful) {
                    id_rol = response.body()!!
                    if (id_rol != null) {
                        // Usa el id_rol aquí
                    } else {
                        Log.e("Error", "Respuesta vacía")
                    }
                } else {
                    Log.e("Error", "Error de red: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("Error", "Excepción: ${e.message}")
            }


            if (id_rol == 1){
                RetrofitClient.apiService.getUsuariosRol1().enqueue(object :
                                                                        Callback<List<Cliente>> {
                    override fun onResponse(call: Call<List<Cliente>>, response: Response<List<Cliente>>) { // Tipus de resposta corregit a List<Cliente>
                        if (response.isSuccessful) {
                            val clientes = response.body()
                            // *** MODIFICACIÓ AQUÍ: Trobar usuari i verificar contrasenya utilitzant BCrypt.checkpw ***
                            val usuarioValido = clientes?.find {
                                it.id == message.id_usuario }
                            holder.titleNameUser.text = usuarioValido!!.nombre + usuarioValido.apellidos
                            setFileImage(usuarioValido.foto!!,holder.avatar)
                            holder.message.text = message.texto
                            holder.date.text = message.fecha_envio.toString()
                        }
                    }
                    override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                        // Fallada a nivell de xarxa
                        Log.e("IniciarSesion", "Error de xarxa obtenint Clients: ${t.message}", t)
                        Toast.makeText(context, "Error de connexió", Toast.LENGTH_SHORT).show() // Missatge de xarxa genèric
                    }
                })
            }else{
                RetrofitClient.apiService.getUsuariosRol2().enqueue(object :
                                                                        Callback<List<Gestor>> {
                    override fun onResponse(call: Call<List<Gestor>>, response: Response<List<Gestor>>) {
                        if (response.isSuccessful) {
                            val gestores = response.body()
                            // *** MODIFICACIÓ AQUÍ: Trobar usuari i verificar contrasenya utilitzant BCrypt.checkpw ***
                            val usuarioValido = gestores?.find {
                                it.id == message.id_usuario  // Compara el correu }
                            }
                            holder.titleNameUser.text = usuarioValido!!.nombre + usuarioValido.apellidos
                            setFileImage(usuarioValido.foto!!,holder.avatar)
                            holder.message.text = message.texto
                            holder.date.text = message.fecha_envio.toString()

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

        }

    }

    override fun getItemCount(): Int {
        return messageList.size
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