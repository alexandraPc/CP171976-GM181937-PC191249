package com.example.cp171976_gm181937_pc191249

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cp171976_gm181937_pc191249.databinding.ItemDateHeaderBinding
import com.example.cp171976_gm181937_pc191249.databinding.ItemTransaccionBinding
import java.text.SimpleDateFormat
import java.util.*

class HistorySectionAdapter(private val items: List<HistoryItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TRANSACTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HistoryItem.Header -> TYPE_HEADER
            is HistoryItem.Transaction -> TYPE_TRANSACTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemTransaccionBinding.inflate(inflater, parent, false)
                TransactionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HistoryItem.Header -> (holder as HeaderViewHolder).bind(item.date)
            is HistoryItem.Transaction -> (holder as TransactionViewHolder).bind(item.transaccion)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(private val binding: ItemDateHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dateStr: String) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                binding.tvHeaderDate.text = date?.let { outputFormat.format(it).uppercase() } ?: dateStr
            } catch (e: Exception) {
                binding.tvHeaderDate.text = dateStr
            }
        }
    }

    class TransactionViewHolder(private val binding: ItemTransaccionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Transaccion) {
            binding.txtConcepto.text = if (item.nombre.isNotEmpty()) item.nombre else item.categoria
            binding.txtCategoria.text = item.categoria
            
            val montoFormateado = String.format("%.2f", item.monto)
            if (item.tipo == "INGRESO") {
                binding.txtMonto.text = "+$${montoFormateado}"
                binding.txtMonto.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            } else {
                binding.txtMonto.text = "-$${montoFormateado}"
                binding.txtMonto.setTextColor(android.graphics.Color.parseColor("#C62828"))
            }
        }
    }
}
