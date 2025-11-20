package com.ipvg.bioeventos

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.ipvg.bioeventos.model.Evento

class EventoAdapter(private val context: Context, private val eventos: List<Evento>) : BaseAdapter() {

    override fun getCount(): Int = eventos.size

    override fun getItem(position: Int): Any = eventos[position]

    override fun getItemId(position: Int): Long = eventos[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_evento, parent, false)

        val txtTitulo = view.findViewById<TextView>(R.id.txtTituloItem)
        val txtCupos = view.findViewById<TextView>(R.id.txtCuposItem)

        val evento = eventos[position]
        txtTitulo.text = evento.titulo
        txtCupos.text = "Cupos disponibles: ${evento.cuposDisponibles}"

        return view
    }
}
