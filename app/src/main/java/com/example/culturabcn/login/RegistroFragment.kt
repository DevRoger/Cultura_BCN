package com.example.culturabcn.login

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.culturabcn.R
import java.util.Calendar

class RegistroFragment: Fragment(R.layout.fragment_registro) {

    // Elements de la Fase 1
    private lateinit var tvNombreLabel: TextView
    private lateinit var edtNombre: EditText
    private lateinit var tvApellidosLabel: TextView
    private lateinit var edtApellidos: EditText
    private lateinit var tvCorreoLabel: TextView
    private lateinit var edtCorreo: EditText
    private lateinit var btnRegistrarse1: Button


    // Elements de la Fase 2
    private lateinit var tvFechaNacimientoLabel: TextView
    private lateinit var edtFechaNacimiento: EditText
    private lateinit var tvTelefonoLabel: TextView
    private lateinit var edtTelefono: EditText
    private lateinit var tvContrasenyaLabel: TextView
    private lateinit var edtContrasenya: EditText
    private lateinit var btnRegistrarse2: Button

    private var nombre: String = ""
    private var apellidos: String = ""
    private var correo: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgBack = view.findViewById<ImageView>(R.id.imgBack)
        val txtInicia = view.findViewById<TextView>(R.id.txtInicia)

        imgBack.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 2
        }

        txtInicia.setOnClickListener {
            val viewPager2 = requireActivity().findViewById<ViewPager2>(R.id.viewPager)  // Obtener el ViewPager2 de la actividad
            viewPager2.currentItem = 2
        }

        // 1. Obtenir referències als elements de la UI
        // Fase 1
        tvNombreLabel = view.findViewById(R.id.tvNombreLabel)
        edtNombre = view.findViewById(R.id.edtNombre)
        tvApellidosLabel = view.findViewById(R.id.tvApellidosLabel)
        edtApellidos = view.findViewById(R.id.edtApellidos)
        tvCorreoLabel = view.findViewById(R.id.tvCorreoLabel)
        edtCorreo = view.findViewById(R.id.edtCorreo)
        btnRegistrarse1 = view.findViewById(R.id.btnRegistrarse1)

        // Fase 2
        tvFechaNacimientoLabel = view.findViewById(R.id.tvFechaNacimientoLabel)
        edtFechaNacimiento = view.findViewById(R.id.edtFechaNacimiento)
        tvTelefonoLabel = view.findViewById(R.id.tvTelefonoLabel)
        edtTelefono = view.findViewById(R.id.edtTelefono)
        tvContrasenyaLabel = view.findViewById(R.id.tvContrasenyaLabel)
        edtContrasenya = view.findViewById(R.id.edtContrasenya)
        btnRegistrarse2 = view.findViewById(R.id.btnRegistrarse2)


        showPhase1()

        btnRegistrarse1.setOnClickListener {
            if (validatePhase1()) {
                // Si la validació de la Fase 1 passa, guardar les dades i passar a la Fase 2
                nombre = edtNombre.text.toString().trim() // Utilitza trim() per eliminar espais blancs innecessaris
                apellidos = edtApellidos.text.toString().trim()
                correo = edtCorreo.text.toString().trim()

                showPhase2() // Transició a la segona fase
            }
        }

        btnRegistrarse2.setOnClickListener {
            if (validatePhase2()) {
                // Si la validació de la Fase 2 passa, recollir totes les dades
                val fechaNacimiento = edtFechaNacimiento.text.toString().trim()
                val telefono = edtTelefono.text.toString().trim()
                val contrasenya = edtContrasenya.text.toString()


                // Exemple: Loguejar les dades recollides i mostrar un missatge simple
                Log.d("Registro", "Dades de registre completes:")
                Log.d("Registro", "Nom: $nombre")
                Log.d("Registro", "Cognoms: $apellidos")
                Log.d("Registro", "Correu: $correo")
                Log.d("Registro", "Data Naixement: $fechaNacimiento")
                Log.d("Registro", "Telèfon: $telefono")
                Log.d("Registro", "Contrasenya (NO loguejar en producció!): $contrasenya") // *** ADVERTÈNCIA: MAI loguejar contrasenyes en una app real! ***

                Toast.makeText(requireContext(), "Registre completat (crida API pendent)", Toast.LENGTH_LONG).show()

                // Després d'una crida a l'API de registre exitosa:
                // - Podries navegar a l'Activity de Login:
                // startActivity(Intent(this, LoginActivity::class.java)) // Substitueix LoginActivity per la teva classe
                // - Acabar aquesta Activity perquè l'usuari no hi pugui tornar enrere amb el botó Back:
                // finish()

            }
        }

        edtFechaNacimiento.setOnClickListener {
            showDatePickerDialog()
        }
    }

    // --- Funcions per controlar la visibilitat de les fases ---

    private fun showPhase1() {
        // Fer visibles els elements de la Fase 1
        tvNombreLabel.visibility = View.VISIBLE
        edtNombre.visibility = View.VISIBLE
        tvApellidosLabel.visibility = View.VISIBLE
        edtApellidos.visibility = View.VISIBLE
        tvCorreoLabel.visibility = View.VISIBLE
        edtCorreo.visibility = View.VISIBLE
        btnRegistrarse1.visibility = View.VISIBLE

        // Amagar els elements de la Fase 2
        tvFechaNacimientoLabel.visibility = View.GONE
        edtFechaNacimiento.visibility = View.GONE
        tvTelefonoLabel.visibility = View.GONE
        edtTelefono.visibility = View.GONE
        tvContrasenyaLabel.visibility = View.GONE
        edtContrasenya.visibility = View.GONE
        btnRegistrarse2.visibility = View.GONE

        // Opcional: Amagar els errors dels camps de la Fase 1 si tornes enrere
        edtNombre.error = null
        edtApellidos.error = null
        edtCorreo.error = null
    }

    private fun showPhase2() {
        // Amagar els elements de la Fase 1
        tvNombreLabel.visibility = View.GONE
        edtNombre.visibility = View.GONE
        tvApellidosLabel.visibility = View.GONE
        edtApellidos.visibility = View.GONE
        tvCorreoLabel.visibility = View.GONE
        edtCorreo.visibility = View.GONE
        btnRegistrarse1.visibility = View.GONE

        // Fer visibles els elements de la Fase 2
        tvFechaNacimientoLabel.visibility = View.VISIBLE
        edtFechaNacimiento.visibility = View.VISIBLE
        tvTelefonoLabel.visibility = View.VISIBLE
        edtTelefono.visibility = View.VISIBLE
        tvContrasenyaLabel.visibility = View.VISIBLE
        edtContrasenya.visibility = View.VISIBLE
        btnRegistrarse2.visibility = View.VISIBLE

        // Opcional: Amagar els errors dels camps de la Fase 2 si tornes enrere
        edtFechaNacimiento.error = null
        edtTelefono.error = null
        edtContrasenya.error = null
    }

    // --- Funcions de validació ---

    private fun validatePhase1(): Boolean {
        var isValid = true

        val nombreText = edtNombre.text.toString().trim()
        val apellidosText = edtApellidos.text.toString().trim()
        val correoText = edtCorreo.text.toString().trim()

        if (nombreText.isBlank()) {
            edtNombre.error = "El nombre no puede estar vacío"
            isValid = false
        } else {
            edtNombre.error = null // Netejar l'error si és vàlid
        }

        if (apellidosText.isBlank()) {
            edtApellidos.error = "Los apellidos no pueden estar vacíos"
            isValid = false
        } else {
            edtApellidos.error = null
        }

        if (correoText.isBlank()) {
            edtCorreo.error = "El correo no puede estar vacío"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoText).matches()) {
            // Validació bàsica de format de correu electrònic
            edtCorreo.error = "Introduce un correo electronico válido"
            isValid = false
        } else {
            edtCorreo.error = null
        }

        return isValid
    }

    private fun validatePhase2(): Boolean {
        var isValid = true

        val fechaNacimientoText = edtFechaNacimiento.text.toString().trim()
        val telefonoText = edtTelefono.text.toString().trim()
        val contrasenyaText = edtContrasenya.text.toString() // No fer trim() aquí normalment

        if (fechaNacimientoText.isBlank()) {
            edtFechaNacimiento.error = "La fecha de nacimiento no puede estar vacía"
            isValid = false
        } else {
            edtFechaNacimiento.error = null
        }

        if (telefonoText.isBlank()) {
            edtTelefono.error = "El telefono no puede estar vacío"
            isValid = false
        } else {
            // Validació bàsica: només dígits. Potser vols una validació més complexa.
            if (!telefonoText.all { it.isDigit() }) {
                edtTelefono.error = "Introduce un numero de telefono válido (solamente digitos)"
                isValid = false
            } else {
                edtTelefono.error = null
            }
        }


        if (contrasenyaText.isBlank()) {
            edtContrasenya.error = "La contraseña no puede estar vacía"
            isValid = false
        } else if (contrasenyaText.length < 8) {
            edtContrasenya.error = "La contraseña debe tener como mínimo 8 carácteres"
            isValid = false
        }
        else {
            edtContrasenya.error = null
        }

        return isValid
    }


    // --- Funció per mostrar el DatePickerDialog ---

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // selectedMonth és indexat des de 0, per això sumem 1
            val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            edtFechaNacimiento.setText(selectedDate)
        }, year, month, day)

        // Opcional: Estableix la data màxima a avui o una data en el passat (un usuari no pot haver nascut en el futur)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        // Opcional: Estableix la data mínima si cal

        datePickerDialog.show()
    }
}