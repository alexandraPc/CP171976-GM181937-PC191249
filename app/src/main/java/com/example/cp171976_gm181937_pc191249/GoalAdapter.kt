package com.example.cp171976_gm181937_pc191249

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cp171976_gm181937_pc191249.model.Meta

class GoalAdapter(
    private val metas: List<Meta>,
    private val onAddClick: (Meta) -> Unit
) : RecyclerView.Adapter<GoalAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGoalName: TextView = view.findViewById(R.id.tvGoalName)
        val tvGoalProgress: TextView = view.findViewById(R.id.tvGoalProgress)
        val progressGoal: ProgressBar = view.findViewById(R.id.progressGoal)
        val btnAddToGoal: Button = view.findViewById(R.id.btnAddToGoal)
        val tvGoalAchieved: TextView = view.findViewById(R.id.tvGoalAchieved)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal_mini, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val meta = metas[position]
        val pct = if (meta.montoObjetivo > 0) {
            ((meta.montoActual / meta.montoObjetivo) * 100).toInt().coerceIn(0, 100)
        } else 0

        holder.tvGoalName.text = meta.nombre
        holder.tvGoalProgress.text = "${pct}% COMPLETADO"
        holder.progressGoal.progress = pct

        if (pct >= 100) {
            holder.btnAddToGoal.visibility = View.GONE
            holder.tvGoalAchieved.visibility = View.VISIBLE
        } else {
            holder.btnAddToGoal.visibility = View.VISIBLE
            holder.tvGoalAchieved.visibility = View.GONE
            holder.btnAddToGoal.text = "Agregar a Meta"
            holder.btnAddToGoal.setOnClickListener { onAddClick(meta) }
        }
    }

    override fun getItemCount() = metas.size
}
