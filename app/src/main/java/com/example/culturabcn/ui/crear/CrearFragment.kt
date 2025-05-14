package com.example.culturabcn.ui.crear

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import com.example.culturabcn.R
import java.util.Calendar

class CrearFragment : Fragment() {
    @SuppressLint("DefaultLocale")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View? {
        val view = inflater.inflate(R.layout.fragment_crear, container, false)


        val edtFecha = view.findViewById<EditText>(R.id.edtFecha)

        // Fecha
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        edtFecha.setOnClickListener {
            val datePickerDialog =
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate = String.format(
                        "%04d-%02d-%02d",
                        selectedYear,
                        selectedMonth + 1,
                        selectedDay
                                                    )
                    edtFecha.setText(selectedDate)
                }, year, month, day)
            datePickerDialog.show()
        }

        val edtHora = view.findViewById<EditText>(R.id.edtHora)

        // Hora
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        edtHora.setOnClickListener {
            val timePickerDialog =
                TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
                    // Formato de 24 horas
                    val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                    edtHora.setText(selectedTime)
                }, hour, minute, true)  // 'true' para formato de 24 horas
            timePickerDialog.show()
        }

        // Configuramos el spinner de las salas
        val edtSala = view.findViewById<TextView>(R.id.edtSala) // Cambia Spinner por TextView
        val salasDisponibles = arrayOf(
            "Restaurante", "Bar", "Cafetería", "Discoteca", "Librería",
            "Tienda de ropa", "Supermercado", "Tienda de electrónica",
            "Floristería", "Salón de belleza", "Gimnasio", "Hotel",
            "Centro de eventos", "Otro"
                                       )

        // Al hacer clic, muestra un AlertDialog con las opciones
        edtSala.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Selecciona una sala")
            builder.setItems(salasDisponibles) { _, which ->
                edtSala.text = salasDisponibles[which]
            }
            builder.show()
        }

        val checkBoxNumeradas = view.findViewById<CheckBox>(R.id.checkBoxNumeradas)
        val edtFilas = view.findViewById<EditText>(R.id.edtFilas)
        val edtColumnas = view.findViewById<EditText>(R.id.edtColumnas)
        val edtAforo = view.findViewById<EditText>(R.id.edtAforo)


        checkBoxNumeradas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                edtFilas.setBackgroundResource(R.drawable.rounded_edittext)
                edtColumnas.setBackgroundResource(R.drawable.rounded_edittext)
                edtFilas.isEnabled = true
                edtColumnas.isEnabled = true

                edtAforo.isEnabled = false
                edtAforo.setBackgroundResource(R.drawable.rounded_edittext_black)
            } else {
                edtFilas.isEnabled = false
                edtColumnas.isEnabled = false
                edtFilas.setBackgroundResource(R.drawable.rounded_edittext_black)
                edtColumnas.setBackgroundResource(R.drawable.rounded_edittext_black)

                edtAforo.isEnabled = true
                edtAforo.setBackgroundResource(R.drawable.rounded_edittext)
            }
        }

        return view
    }
}
