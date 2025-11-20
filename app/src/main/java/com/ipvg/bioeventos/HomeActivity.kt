package com.ipvg.bioeventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private var userId: Long = -1
    private var userName: String = ""
    private var userRolId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        userId = intent.getLongExtra("userId", -1)
        userName = intent.getStringExtra("userName") ?: ""
        userRolId = intent.getLongExtra("userRolId", -1)

        val txtBienvenida = findViewById<TextView>(R.id.txtBienvenida)
        val btnMisInscripciones = findViewById<Button>(R.id.btnMisInscripciones)
        val btnExplorar = findViewById<Button>(R.id.btnExplorarEventos)
        val btnCrear = findViewById<Button>(R.id.btnCrearEvento)

        txtBienvenida.text = "Hola, $userName"

        btnExplorar.setOnClickListener {
            val intent = Intent(this, EventosActivosActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        btnMisInscripciones.setOnClickListener {
            val intent = Intent(this, MisInscripcionesActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        btnCrear.setOnClickListener {
            val intent = Intent(this, CrearEventoActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }
    }
}
