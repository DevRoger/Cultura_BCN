package com.example.culturabcn.sockets

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.culturabcn.clases.Mensaje
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.Socket
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

public class ChatClient(
    private val serverAddress: String,
    private val serverPort: Int,
    private val senderId: Int,
    private val chatId: Int
                ) {
    private lateinit var socket: Socket
    private lateinit var writer: BufferedWriter
    private lateinit var reader: BufferedReader

    var onMessagesReceived: ((List<Mensaje>) -> Unit)? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun connect() {
        Thread {
            try {
                socket = Socket(serverAddress, serverPort)
                writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
                reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Enviar JSON inicial
                val initJson = JSONObject()
                initJson.put("sender_id", senderId)
                initJson.put("chat_id", chatId)
                sendMessage(initJson.toString())

                // Bucle per escoltar missatges rebuts
                CoroutineScope(Dispatchers.IO).launch {
                    listenForMessages()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private suspend fun listenForMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var line: String?
                while (true) {
                    line = reader.readLine()
                    val response = line ?: continue

                    try {
                        val mensajes = mutableListOf<Mensaje>()

                        if (response.trim().startsWith("[")) {
                            // Es un array de mensajes
                            val jsonArray = JSONArray(response)
                            for (i in 0 until jsonArray.length()) {
                                mensajes.add(parseMensajeFromJson(jsonArray.getJSONObject(i)))
                            }
                        } else {
                            // Es un único mensaje
                            val jsonObject = JSONObject(response)
                            mensajes.add(parseMensajeFromJson(jsonObject))
                        }

                        withContext(Dispatchers.Main) {
                            onMessagesReceived?.invoke(mensajes)
                        }

                    } catch (ex: Exception) {
                        Log.e("ChatClient", "Error al procesar mensaje: ${ex.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatClient", "Error de lectura en escucha: ${e.message}")
            }
        }
    }
    private fun parseMensajeFromJson(json: JSONObject): Mensaje {
        val idMensaje = json.getInt("id_mensaje")
        val idChat = json.getInt("id_chat")
        val mensaje = json.getString("texto")
        val fechaStr = json.getString("fecha_envio")
        val idUsuario = json.getInt("id_usuario")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val fechaEnvio = try {
            dateFormat.parse(fechaStr)
        } catch (e: Exception) {
            Date()
        }

        return Mensaje(idMensaje, idChat, mensaje, fechaEnvio ?: Date(), idUsuario)
    }

    fun sendMessage(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                writer.write(message)
                writer.newLine()
                writer.flush()
            } catch (e: IOException) {
                Log.e("ChatClient", "Error al enviar mensaje: ${e.message}")
            }
        }
    }

    fun close() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                writer.close()
                reader.close()
                socket.close()
            } catch (e: IOException) {
                Log.e("ChatClient", "Error al cerrar conexión: ${e.message}")
            }
        }
    }
}
