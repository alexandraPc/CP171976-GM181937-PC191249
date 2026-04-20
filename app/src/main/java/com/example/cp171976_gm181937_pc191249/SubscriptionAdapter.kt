package com.example.cp171976_gm181937_pc191249

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cp171976_gm181937_pc191249.model.Suscripcion
import java.util.*

class SubscriptionAdapter(
    private var subscriptions: List<Suscripcion>,
    private val onPaidClick: (Suscripcion) -> Unit,
    private val onDeleteClick: (Suscripcion) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.ivServiceIcon)
        val tvName: TextView = view.findViewById(R.id.tvServiceName)
        val tvPlan: TextView = view.findViewById(R.id.tvServicePlan)
        val tvNextPay: TextView = view.findViewById(R.id.tvNextPayDate)
        val tvPrice: TextView = view.findViewById(R.id.tvServicePrice)
        val btnPaid: ImageButton = view.findViewById(R.id.btnPaid)

        init {
            view.setOnLongClickListener {
                // Implement delete on long click if needed, or pass it
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscription, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sub = subscriptions[position]
        holder.tvName.text = sub.nombre
        holder.tvPlan.text = sub.categoria
        holder.tvPrice.text = "$${String.format("%.2f", sub.monto)}"
        holder.tvNextPay.text = "Día de pago: ${sub.diaPago}"

        holder.btnPaid.setOnClickListener { onPaidClick(sub) }
        
        holder.itemView.setOnLongClickListener {
            onDeleteClick(sub)
            true
        }
    }

    override fun getItemCount() = subscriptions.size

    fun updateData(newSubs: List<Suscripcion>) {
        subscriptions = newSubs
        notifyDataSetChanged()
    }
}
