package com.example.cp171976_gm181937_pc191249


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransaccionAdapter(private val lista: List<Transaccion>) :
    RecyclerView.Adapter<TransaccionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtConcepto: TextView = view.findViewById(R.id.txtConcepto)
        val txtMonto: TextView = view.findViewById(R.id.txtMonto)
        val txtCategoria: TextView = view.findViewById(R.id.txtCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaccion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]

        // Si el nombre está vacío, mostramos la categoría como título principal
        if (item.nombre.isNotEmpty()) {
            holder.txtConcepto.text = item.nombre
            holder.txtCategoria.text = "${item.categoria} • ${item.fecha}"
        } else {
            holder.txtConcepto.text = item.categoria
            holder.txtCategoria.text = item.fecha
        }

        val montoFormateado = String.format("%.2f", item.monto)

        // Aquí está la lógica que pedías: depende de lo que guardaste
        if (item.tipo == "INGRESO") {
            holder.txtMonto.text = "+$${montoFormateado}"
            holder.txtMonto.setTextColor(android.graphics.Color.parseColor("#2E7D32")) // Verde
        } else {
            holder.txtMonto.text = "-$${montoFormateado}"
            holder.txtMonto.setTextColor(android.graphics.Color.parseColor("#C62828")) // Rojo
        }
    }

    override fun getItemCount() = lista.size
}