package com.ipvg.bioeventos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ipvg.bioeventos.data.DBHelper

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // usamos el layout XML que creaste recién
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this)

        val edtNombre = findViewById<EditText>(R.id.edtNombre)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnIniciar = findViewById<Button>(R.id.btnIniciarSesion)
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrarse)

        // REGISTRAR USUARIO (rol asistente por defecto)
        btnRegistrar.setOnClickListener {
            val nombre = edtNombre.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val pass = edtPassword.text.toString()

            if (nombre.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos para registrarte", Toast.LENGTH_SHORT).show()
            } else {
                val ok = dbHelper.registrarUsuario(nombre, email, pass, esOrganizador = false)
                if (ok) {
                    Toast.makeText(this, "Usuario registrado. Ahora inicia sesión.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al registrar (¿email duplicado?)", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // LOGIN
        btnIniciar.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val pass = edtPassword.text.toString()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Ingresa email y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                val usuario = dbHelper.loginUsuario(email, pass)
                if (usuario != null) {
                    Toast.makeText(this, "Bienvenido ${usuario.nombre}", Toast.LENGTH_SHORT).show()
                    // Pasamos a la pantalla principal
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("userId", usuario.id)
                    intent.putExtra("userName", usuario.nombre)
                    intent.putExtra("userRolId", usuario.idRol)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
