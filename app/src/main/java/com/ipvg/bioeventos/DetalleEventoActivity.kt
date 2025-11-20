package com.ipvg.bioeventos

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ipvg.bioeventos.data.DBHelper
import com.ipvg.bioeventos.model.Evento

class DetalleEventoActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper
    private var userId: Long = -1
    private var eventoId: Long = -1
    private var estaInscrito: Boolean = false
    private var evento: Evento? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_evento)

        dbHelper = DBHelper(this)
        userId = intent.getLongExtra("userId", -1)
        eventoId = intent.getLongExtra("eventoId", -1)

        val txtTitulo = findViewById<TextView>(R.id.txtTituloDetalle)
        val txtDescripcion = findViewById<TextView>(R.id.txtDescripcionDetalle)
        val txtLugar = findViewById<TextView>(R.id.txtLugarDetalle)
        val txtFechaHora = findViewById<TextView>(R.id.txtFechaHoraDetalle)
        val txtCupos = findViewById<TextView>(R.id.txtCuposDetalle)
        val btnAccion = findViewById<Button>(R.id.btnAccionEvento)

        evento = dbHelper.obtenerEventoPorId(eventoId)
        if (evento == null) {
            Toast.makeText(this, "Evento no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        txtTitulo.text = evento!!.titulo
        txtDescripcion.text = evento!!.descripcion
        txtLugar.text = "Lugar: ${evento!!.lugar}"
        txtFechaHora.text = "Fecha/Hora: ${evento!!.fechaHora}"
        txtCupos.text = "Cupos disponibles: ${evento!!.cuposDisponibles}"

        estaInscrito = dbHelper.usuarioEstaInscrito(userId, eventoId)
        actualizarTextoBoton(btnAccion)

        btnAccion.setOnClickListener {
            if (estaInscrito) {
                val ok = dbHelper.cancelarInscripcion(userId, eventoId)
                if (ok) {
                    Toast.makeText(this, "Inscripción cancelada", Toast.LENGTH_SHORT).show()
                    estaInscrito = false
                } else {
                    Toast.makeText(this, "No se pudo cancelar", Toast.LENGTH_SHORT).show()
                }
            } else {
                val ok = dbHelper.inscribirUsuarioEnEvento(userId, eventoId)
                if (ok) {
                    Toast.makeText(this, "Inscrito correctamente", Toast.LENGTH_SHORT).show()
                    estaInscrito = true
                } else {
                    Toast.makeText(this, "No hay cupos o error", Toast.LENGTH_SHORT).show()
                }
            }

            // refrescar cupos en pantalla
            evento = dbHelper.obtenerEventoPorId(eventoId)
            txtCupos.text = "Cupos disponibles: ${evento?.cuposDisponibles ?: 0}"
            actualizarTextoBoton(btnAccion)
        }
    }

    private fun actualizarTextoBoton(btn: Button) {
        btn.text = if (estaInscrito) "Darse de baja" else "Inscribirse"
    }
}
