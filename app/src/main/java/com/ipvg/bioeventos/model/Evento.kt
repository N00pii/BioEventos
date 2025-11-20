package com.ipvg.bioeventos.model

data class Evento(
    val id: Long,
    val titulo: String,
    val descripcion: String,
    val fechaHora: String,
    val lugar: String,
    val cuposTotales: Int,
    val cuposDisponibles: Int,
    val idCreador: Long
)
