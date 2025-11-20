package com.ipvg.bioeventos

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ipvg.bioeventos.data.DBHelper

class CrearEventoActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_evento)

        dbHelper = DBHelper(this)
        userId = intent.getLongExtra("userId", -1)

        val edtTitulo = findViewById<EditText>(R.id.edtTituloEvento)
        val edtDescripcion = findViewById<EditText>(R.id.edtDescripcionEvento)
        val edtFechaHora = findViewById<EditText>(R.id.edtFechaHoraEvento)
        val edtLugar = findViewById<EditText>(R.id.edtLugarEvento)
        val edtCupos = findViewById<EditText>(R.id.edtCuposEvento)
        val btnCrear = findViewById<Button>(R.id.btnCrearEvento)

        btnCrear.setOnClickListener {
            val titulo = edtTitulo.text.toString().trim()
            val descripcion = edtDescripcion.text.toString().trim()
            val fechaHora = edtFechaHora.text.toString().trim()
            val lugar = edtLugar.text.toString().trim()
            val cuposStr = edtCupos.text.toString().trim()

            if (titulo.isEmpty() || cuposStr.isEmpty()) {
                Toast.makeText(this, "Título y cupos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cupos = cuposStr.toIntOrNull()
            if (cupos == null || cupos <= 0) {
                Toast.makeText(this, "Cupos debe ser un número mayor que 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ok = dbHelper.crearEvento(titulo, descripcion, fechaHora, lugar, cupos, userId)
            if (ok) {
                Toast.makeText(this, "Evento creado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al crear evento", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
