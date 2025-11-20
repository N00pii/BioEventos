package com.ipvg.bioeventos.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.ipvg.bioeventos.model.Evento
import com.ipvg.bioeventos.model.Usuario

class DBHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tabla ROL
        db.execSQL(
            """
            CREATE TABLE $TABLE_ROL (
                $COL_ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_ROL_NOMBRE TEXT NOT NULL
            )
            """
        )

        // Tabla USUARIO
        db.execSQL(
            """
            CREATE TABLE $TABLE_USUARIO (
                $COL_USUARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USUARIO_NOMBRE TEXT NOT NULL,
                $COL_USUARIO_EMAIL TEXT NOT NULL UNIQUE,
                $COL_USUARIO_PASS TEXT NOT NULL,
                $COL_USUARIO_ID_ROL INTEGER NOT NULL,
                FOREIGN KEY($COL_USUARIO_ID_ROL) REFERENCES $TABLE_ROL($COL_ROL_ID)
            )
            """
        )

        // Tabla EVENTO
        db.execSQL(
            """
            CREATE TABLE $TABLE_EVENTO (
                $COL_EVENTO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EVENTO_TITULO TEXT NOT NULL,
                $COL_EVENTO_DESCRIPCION TEXT,
                $COL_EVENTO_FECHAHORA TEXT,
                $COL_EVENTO_LUGAR TEXT,
                $COL_EVENTO_CUPOS_TOTALES INTEGER NOT NULL,
                $COL_EVENTO_CUPOS_DISP INTEGER NOT NULL,
                $COL_EVENTO_ID_CREADOR INTEGER NOT NULL,
                FOREIGN KEY($COL_EVENTO_ID_CREADOR) REFERENCES $TABLE_USUARIO($COL_USUARIO_ID)
            )
            """
        )

        // Tabla INSCRIPCION
        db.execSQL(
            """
            CREATE TABLE $TABLE_INSCRIPCION (
                $COL_INSCRIPCION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_INSCRIPCION_ID_USUARIO INTEGER NOT NULL,
                $COL_INSCRIPCION_ID_EVENTO INTEGER NOT NULL,
                FOREIGN KEY($COL_INSCRIPCION_ID_USUARIO) REFERENCES $TABLE_USUARIO($COL_USUARIO_ID),
                FOREIGN KEY($COL_INSCRIPCION_ID_EVENTO) REFERENCES $TABLE_EVENTO($COL_EVENTO_ID)
            )
            """
        )

        insertarRolInicial(db, "Organizador")
        insertarRolInicial(db, "Asistente")

        Log.d(TAG, "onCreate: Base de datos creada con tablas ROL, USUARIO, EVENTO, INSCRIPCION")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_INSCRIPCION")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENTO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIO")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ROL")
        onCreate(db)
    }

    private fun insertarRolInicial(db: SQLiteDatabase, nombreRol: String) {
        val values = ContentValues().apply {
            put(COL_ROL_NOMBRE, nombreRol)
        }
        db.insert(TABLE_ROL, null, values)
    }

    // -------- USUARIOS --------

    fun registrarUsuario(nombre: String, email: String, password: String, esOrganizador: Boolean = false): Boolean {
        val db = writableDatabase

        val rolNombre = if (esOrganizador) "Organizador" else "Asistente"
        val cursor = db.query(
            TABLE_ROL,
            arrayOf(COL_ROL_ID),
            "$COL_ROL_NOMBRE = ?",
            arrayOf(rolNombre),
            null, null, null
        )

        var idRol = -1L
        if (cursor.moveToFirst()) {
            idRol = cursor.getLong(0)
        }
        cursor.close()
        if (idRol == -1L) return false

        val values = ContentValues().apply {
            put(COL_USUARIO_NOMBRE, nombre)
            put(COL_USUARIO_EMAIL, email)
            put(COL_USUARIO_PASS, password) // para el ramo basta así
            put(COL_USUARIO_ID_ROL, idRol)
        }

        val result = db.insert(TABLE_USUARIO, null, values)
        Log.d(TAG, "registrarUsuario: nombre=$nombre, email=$email, result=$result")
        return result != -1L
    }

    fun loginUsuario(email: String, password: String): Usuario? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USUARIO,
            arrayOf(COL_USUARIO_ID, COL_USUARIO_NOMBRE, COL_USUARIO_EMAIL, COL_USUARIO_ID_ROL),
            "$COL_USUARIO_EMAIL = ? AND $COL_USUARIO_PASS = ?",
            arrayOf(email, password),
            null, null, null
        )

        var usuario: Usuario? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(0)
            val nombre = cursor.getString(1)
            val emailDb = cursor.getString(2)
            val idRol = cursor.getLong(3)
            usuario = Usuario(id, nombre, emailDb, idRol)
        }
        cursor.close()

        Log.d(TAG, "loginUsuario: email=$email, encontrado=${usuario != null}")
        return usuario
    }

    // -------- EVENTOS --------

    fun crearEvento(
        titulo: String,
        descripcion: String,
        fechaHora: String,
        lugar: String,
        cuposTotales: Int,
        idCreador: Long
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_EVENTO_TITULO, titulo)
            put(COL_EVENTO_DESCRIPCION, descripcion)
            put(COL_EVENTO_FECHAHORA, fechaHora)
            put(COL_EVENTO_LUGAR, lugar)
            put(COL_EVENTO_CUPOS_TOTALES, cuposTotales)
            put(COL_EVENTO_CUPOS_DISP, cuposTotales)
            put(COL_EVENTO_ID_CREADOR, idCreador)
        }
        val result = db.insert(TABLE_EVENTO, null, values)
        Log.d(TAG, "crearEvento: titulo=$titulo, cupos=$cuposTotales, result=$result")
        return result != -1L
    }

    fun obtenerEventosActivos(): List<Evento> {
        val db = readableDatabase
        val lista = mutableListOf<Evento>()

        val cursor = db.query(
            TABLE_EVENTO,
            null,
            null, null, null, null,
            "$COL_EVENTO_FECHAHORA ASC"
        )

        while (cursor.moveToNext()) {
            val evento = Evento(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENTO_ID)),
                titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_TITULO)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_DESCRIPCION)),
                fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_FECHAHORA)),
                lugar = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_LUGAR)),
                cuposTotales = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENTO_CUPOS_TOTALES)),
                cuposDisponibles = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENTO_CUPOS_DISP)),
                idCreador = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENTO_ID_CREADOR))
            )
            lista.add(evento)
        }
        cursor.close()

        Log.d(TAG, "obtenerEventosActivos: se obtuvieron ${lista.size} eventos")
        return lista
    }

    fun obtenerEventoPorId(idEvento: Long): Evento? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EVENTO,
            null,
            "$COL_EVENTO_ID = ?",
            arrayOf(idEvento.toString()),
            null, null, null
        )

        var evento: Evento? = null
        if (cursor.moveToFirst()) {
            evento = Evento(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENTO_ID)),
                titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_TITULO)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_DESCRIPCION)),
                fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_FECHAHORA)),
                lugar = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_LUGAR)),
                cuposTotales = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENTO_CUPOS_TOTALES)),
                cuposDisponibles = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENTO_CUPOS_DISP)),
                idCreador = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENTO_ID_CREADOR))
            )
        }
        cursor.close()
        return evento
    }

    // -------- INSCRIPCIONES --------

    fun usuarioEstaInscrito(idUsuario: Long, idEvento: Long): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_INSCRIPCION,
            arrayOf(COL_INSCRIPCION_ID),
            "$COL_INSCRIPCION_ID_USUARIO = ? AND $COL_INSCRIPCION_ID_EVENTO = ?",
            arrayOf(idUsuario.toString(), idEvento.toString()),
            null, null, null
        )
        val existe = cursor.moveToFirst()
        cursor.close()
        return existe
    }

    fun inscribirUsuarioEnEvento(idUsuario: Long, idEvento: Long): Boolean {
        val db = writableDatabase

        val evento = obtenerEventoPorId(idEvento) ?: return false
        if (evento.cuposDisponibles <= 0) {
            Log.d(TAG, "inscribirUsuarioEnEvento: SIN CUPOS usuario=$idUsuario evento=$idEvento")
            return false
        }

        val values = ContentValues().apply {
            put(COL_INSCRIPCION_ID_USUARIO, idUsuario)
            put(COL_INSCRIPCION_ID_EVENTO, idEvento)
        }
        val result = db.insert(TABLE_INSCRIPCION, null, values)
        if (result == -1L) return false

        val nuevosCupos = evento.cuposDisponibles - 1
        val valuesUpdate = ContentValues().apply {
            put(COL_EVENTO_CUPOS_DISP, nuevosCupos)
        }
        db.update(
            TABLE_EVENTO,
            valuesUpdate,
            "$COL_EVENTO_ID = ?",
            arrayOf(idEvento.toString())
        )

        Log.d(TAG, "inscribirUsuarioEnEvento: usuario=$idUsuario evento=$idEvento OK")
        return true
    }

    fun cancelarInscripcion(idUsuario: Long, idEvento: Long): Boolean {
        val db = writableDatabase
        val evento = obtenerEventoPorId(idEvento) ?: return false

        val filas = db.delete(
            TABLE_INSCRIPCION,
            "$COL_INSCRIPCION_ID_USUARIO = ? AND $COL_INSCRIPCION_ID_EVENTO = ?",
            arrayOf(idUsuario.toString(), idEvento.toString())
        )
        if (filas <= 0) return false

        val nuevosCupos = evento.cuposDisponibles + 1
        val valuesUpdate = ContentValues().apply {
            put(COL_EVENTO_CUPOS_DISP, nuevosCupos)
        }
        db.update(
            TABLE_EVENTO,
            valuesUpdate,
            "$COL_EVENTO_ID = ?",
            arrayOf(idEvento.toString())
        )

        Log.d(TAG, "cancelarInscripcion: usuario=$idUsuario evento=$idEvento OK")
        return true
    }

    fun obtenerEventosDeUsuarioInscrito(idUsuario: Long): List<Evento> {
        val db = readableDatabase
        val lista = mutableListOf<Evento>()

        val query = """
            SELECT e.*
            FROM $TABLE_EVENTO e
            INNER JOIN $TABLE_INSCRIPCION i
            ON e.$COL_EVENTO_ID = i.$COL_INSCRIPCION_ID_EVENTO
            WHERE i.$COL_INSCRIPCION_ID_USUARIO = ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString()))
        while (cursor.moveToNext()) {
            val evento = Evento(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENTO_ID)),
                titulo = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_TITULO)),
                descripcion = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_DESCRIPCION)),
                fechaHora = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_FECHAHORA)),
                lugar = cursor.getString(cursor.getColumnIndexOrThrow(COL_EVENTO_LUGAR)),
                cuposTotales = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENTO_CUPOS_TOTALES)),
                cuposDisponibles = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EVENTO_CUPOS_DISP)),
                idCreador = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EVENTO_ID_CREADOR))
            )
            lista.add(evento)
        }
        cursor.close()

        Log.d(TAG, "obtenerEventosDeUsuarioInscrito: usuario=$idUsuario eventos=${lista.size}")
        return lista
    }

    companion object {
        const val DATABASE_NAME = "bioeventos.db"
        const val DATABASE_VERSION = 1

        const val TAG = "BioEventosDB"

        const val TABLE_ROL = "ROL"
        const val COL_ROL_ID = "ID_Rol"
        const val COL_ROL_NOMBRE = "NombreRol"

        const val TABLE_USUARIO = "USUARIO"
        const val COL_USUARIO_ID = "ID_Usuario"
        const val COL_USUARIO_NOMBRE = "Nombre"
        const val COL_USUARIO_EMAIL = "Email"
        const val COL_USUARIO_PASS = "Contrasena"
        const val COL_USUARIO_ID_ROL = "ID_Rol"

        const val TABLE_EVENTO = "EVENTO"
        const val COL_EVENTO_ID = "ID_Evento"
        const val COL_EVENTO_TITULO = "Titulo"
        const val COL_EVENTO_DESCRIPCION = "Descripcion"
        const val COL_EVENTO_FECHAHORA = "FechaHora"
        const val COL_EVENTO_LUGAR = "Lugar"
        const val COL_EVENTO_CUPOS_TOTALES = "NumeroCuposTotales"
        const val COL_EVENTO_CUPOS_DISP = "CuposDisponibles"
        const val COL_EVENTO_ID_CREADOR = "ID_Creador"

        const val TABLE_INSCRIPCION = "INSCRIPCION"
        const val COL_INSCRIPCION_ID = "ID_Inscripcion"
        const val COL_INSCRIPCION_ID_USUARIO = "ID_Usuario"
        const val COL_INSCRIPCION_ID_EVENTO = "ID_Evento"
    }
}
