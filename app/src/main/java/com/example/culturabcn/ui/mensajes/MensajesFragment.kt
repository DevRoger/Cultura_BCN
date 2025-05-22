package com.example.culturabcn.ui.mensajes


import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.Mensaje
import com.example.culturabcn.clases.UserLogged
import com.example.culturabcn.databinding.FragmentMensajesBinding
import com.example.culturabcn.sockets.ChatClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale

class MensajesFragment : Fragment() {

    private var _binding: FragmentMensajesBinding? = null
    private var chatClient : ChatClient? = null
    private var listOfMessage: MutableList<Mensaje>? = mutableListOf()
    private var chatClientConnected = false
    private var id_chat = 0
    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var listChats = view.findViewById<RecyclerView>(R.id.recyclerViewChats)
        var search = view.findViewById<EditText>(R.id.searcher)
        var buttonSearch = view.findViewById<Button>(R.id.button_search)
        var screenListChats = view.findViewById<LinearLayout>(R.id.pantalla_lista_chat)
        var screenMessages = view.findViewById<LinearLayout>(R.id.chat_panel)
        var recyclerMessages = view.findViewById<RecyclerView>(R.id.recyclerViewMessages)
        var panelSendMessages = view.findViewById<LinearLayout>(R.id.panelMessage)
        var DataChat = view.findViewById<TextView>(R.id.user_chat_data)
        var textMessage = view.findViewById<EditText>(R.id.text)
        var buttonSendMessage = view.findViewById<ImageView>(R.id.button_send_message)


        val rol = UserLogged.rolId

        if (rol == 1){
            RetrofitClient.apiService.getUsuariosRol1().enqueue(object : Callback<List<Cliente>> {
                override fun onResponse(call: Call<List<Cliente>>, response: Response<List<Cliente>>) { // Tipus de resposta corregit a List<Cliente>
                    if (response.isSuccessful) {
                        val clientes = response.body()
                        // *** MODIFICACIÓ AQUÍ: Trobar usuari i verificar contrasenya utilitzant BCrypt.checkpw ***
                        val usuarioValido = clientes?.find {
                            it.id == UserLogged.userId }
                        updateData(usuarioValido!!.id,listChats,screenListChats,screenMessages,recyclerMessages,panelSendMessages,DataChat)
                    }
                }
                override fun onFailure(call: Call<List<Cliente>>, t: Throwable) {
                    // Fallada a nivell de xarxa
                    Log.e("IniciarSesion", "Error de xarxa obtenint Clients: ${t.message}", t)
                    Toast.makeText(requireContext(), "Error de connexió", Toast.LENGTH_SHORT).show() // Missatge de xarxa genèric
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
                        updateData(
                            usuarioValido!!.id,
                            listChats,
                            screenListChats,
                            screenMessages,
                            recyclerMessages,
                            panelSendMessages,
                            DataChat
                                  )
                    }
                }
                override fun onFailure(call: Call<List<Gestor>>, t: Throwable) {
                    // Fallada a nivell de xarxa
                    Log.e("IniciarSesion", "Error de xarxa obtenint Gestors: ${t.message}", t)
                    Toast.makeText(requireContext(), "Error de connexió", Toast.LENGTH_SHORT).show() // Missatge de xarxa genèric
                    // Com que aquesta és la segona resposta, si autenticado encara és false, es mostrarà l'error final
                }
            })
        }
        screenMessages.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (v.visibility == View.VISIBLE && !chatClientConnected) {
                val data_other_user = DataChat.text.toString()
                id_chat = data_other_user.toInt()
                chatClient = ChatClient("10.0.3.141", 5000, senderId = UserLogged.userId, chatId = id_chat)
                chatClient!!.connect()
                chatClientConnected= true
                chatClient!!.onMessagesReceived = { listaMensajes ->
                    lifecycleScope.launch {
                        if(listaMensajes.size==1){
                            listOfMessage!!.add(listaMensajes[0])
                            recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
                            var adapter = AdapterMensajes(listOfMessage!!,requireContext(),UserLogged.userId,lifecycleScope)
                            recyclerMessages.adapter = adapter
                            recyclerMessages.scrollToPosition(listaMensajes.size - 1)
                        }else{
                            listOfMessage = listaMensajes.toMutableList()
                            recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
                            var adapter = AdapterMensajes(listOfMessage!!,requireContext(),UserLogged.userId,lifecycleScope)
                            recyclerMessages.adapter = adapter
                            recyclerMessages.scrollToPosition(listaMensajes.size - 1)
                        }
                    }
                }
            }
        }
        buttonSendMessage.setOnClickListener(){
            val mensajeTexto = textMessage.text.toString()

            if (mensajeTexto.isNotEmpty()) {
                val json = JSONObject()
                json.put("sender_id", UserLogged.userId)
                json.put("chat_id", id_chat)
                json.put("texto", mensajeTexto)
                val now = Date()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val formatted = dateFormat.format(now)
                val parsedDate = dateFormat.parse(formatted)
                chatClient!!.sendMessage(json.toString())
                textMessage.setText("") // Limpiar campo
                listOfMessage?.add(Mensaje(id_chat,mensajeTexto,parsedDate,UserLogged.userId))
                recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
                var adapter = AdapterMensajes(listOfMessage!!,requireContext(),UserLogged.userId,lifecycleScope)
                recyclerMessages.adapter = adapter
                recyclerMessages.scrollToPosition(listOfMessage!!.size - 1)
            }
        }



    }
    fun updateData(
        usuario: Int?,
        listChats: RecyclerView,
        screenListChats: LinearLayout,
        screenMessages: LinearLayout,
        recyclerMessages: RecyclerView,
        panelSendMessages: LinearLayout,
        DataChat: TextView,
                  ) {
        lifecycleScope.launch{
            var list = RetrofitClient.apiService.getChatsByUserId(usuario!!)
            listChats.layoutManager = LinearLayoutManager(requireContext())
            val adapterData = AdapterChats(list,usuario!!,requireContext(),screenListChats,screenMessages,recyclerMessages,panelSendMessages,DataChat,lifecycleScope)
            listChats.adapter = adapterData

        }
    }
    private fun anadirMensaje(mensaje: Mensaje, recyclerMessages: RecyclerView) {
        listOfMessage?.add(mensaje)
        recyclerMessages.layoutManager = LinearLayoutManager(requireContext())
        var adapter = AdapterMensajes(listOfMessage!!,requireContext(),UserLogged.userId,lifecycleScope)
        recyclerMessages.adapter = adapter
        recyclerMessages.scrollToPosition(listOfMessage!!.size - 1)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mensajes, container, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}