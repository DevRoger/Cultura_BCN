package com.example.culturabcn.ui.mensajes

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.culturabcn.API.RetrofitClient
import com.example.culturabcn.R
import com.example.culturabcn.clases.Cliente
import com.example.culturabcn.clases.Gestor
import com.example.culturabcn.clases.UserLogged
import com.example.culturabcn.databinding.FragmentMensajesBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MensajesFragment : Fragment() {

    private var _binding: FragmentMensajesBinding? = null

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
        var list = RetrofitClient.apiService.getChatsByUserId(usuario!!)
        listChats.layoutManager = LinearLayoutManager(requireContext())
        val adapterData = AdapterChats(list,usuario!!,requireContext(),screenListChats,screenMessages,recyclerMessages,panelSendMessages,DataChat)
        listChats.adapter = adapterData


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