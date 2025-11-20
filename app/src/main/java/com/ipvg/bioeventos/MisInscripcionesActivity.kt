package com.ipvg.bioeventos

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.ipvg.bioeventos.data.DBHelper
import com.ipvg.bioeventos.model.Evento

class MisInscripcionesActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private lateinit var listView: ListView
    private var userId: Long = -1
    private lateinit var eventos: List<Evento>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_inscripciones)

        dbHelper = DBHelper(this)
        userId = intent.getLongExtra("userId", -1)
        listView = findViewById(R.id.listMisEventos)

        cargarEventos()

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val evento = eventos[position]
            val intent = Intent(this, DetalleEventoActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("eventoId", evento.id)
            startActivity(intent)
        }
    }

    private fun cargarEventos() {
        eventos = dbHelper.obtenerEventosDeUsuarioInscrito(userId)
        val adapter = EventoAdapter(this, eventos)
        listView.adapter = adapter
    }
}
