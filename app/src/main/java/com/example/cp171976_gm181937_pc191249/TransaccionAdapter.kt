package com.example.cp171976_gm181937_pc191249

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransaccionAdapter(private val lista: List<Transaccion>) :
    RecyclerView.Adapter<TransaccionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCategoria: ImageView = view.findViewById(R.id.imgCategoria)
        val txtConcepto: TextView = view.findViewById(R.id.txtConcepto)
        val txtCategoria: TextView = view.findViewById(R.id.txtCategoria)
        val txtMonto: TextView = view.findViewById(R.id.txtMonto)
        val txtFecha: TextView = view.findViewById(R.id.txtFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        holder.txtConcepto.text = item.nombre.ifEmpty { item.categoria }
        holder.txtCategoria.text = item.categoria
        holder.txtFecha.text = item.fecha

        val montoFormateado = String.format("%.2f", item.monto)

        if (item.tipo == "INGRESO") {
            holder.txtMonto.text = "+$$montoFormateado"
            holder.txtMonto.setTextColor(Color.parseColor("#30D64A"))
            holder.imgCategoria.setColorFilter(Color.parseColor("#1B6D24"))
        } else {
            holder.txtMonto.text = "-$$montoFormateado"
            holder.txtMonto.setTextColor(Color.parseColor("#FF5252"))
            holder.imgCategoria.setColorFilter(Color.parseColor("#1A3B5D"))
        }
    }

    override fun getItemCount() = lista.size
}
