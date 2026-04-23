package com.example.cp171976_gm181937_pc191249

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cp171976_gm181937_pc191249.database.DatabaseHelper
import com.example.cp171976_gm181937_pc191249.model.Meta
import com.google.android.material.bottomnavigation.BottomNavigationView

class GoalsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.parseColor("#F7F9FB")
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
        setContentView(R.layout.activity_goals)

        dbHelper = DatabaseHelper(this)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_goals
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_goals -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    false
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    false
                }
                R.id.nav_subscriptions -> {
                    startActivity(Intent(this, SubscriptionsActivity::class.java))
                    finish()
                    false
                }
                else -> false
            }
        }

        findViewById<View>(R.id.llNewGoal).setOnClickListener {
            showNewGoalDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUI()
    }

    private fun refreshUI() {
        // Total savings
        val totalSaved = dbHelper.obtenerTotalAhorrado()
        val pctTotal = dbHelper.obtenerPorcentajeTotalMetas()
        findViewById<TextView>(R.id.tvTotalSaved).text = "$${String.format("%,.2f", totalSaved)}"
        findViewById<ProgressBar>(R.id.progressTotalSavings).progress = pctTotal
        findViewById<TextView>(R.id.tvSavingsPercent).text = "$pctTotal% del total de metas"

        // Featured goal
        val featuredCard = findViewById<View>(R.id.cardFeaturedGoal)
        val metaPrincipal = dbHelper.obtenerMetaPrincipal()
        if (metaPrincipal == null) {
            featuredCard.visibility = View.GONE
        } else {
            featuredCard.visibility = View.VISIBLE
            val pct = if (metaPrincipal.montoObjetivo > 0) {
                ((metaPrincipal.montoActual / metaPrincipal.montoObjetivo) * 100).toInt().coerceIn(0, 100)
            } else 0
            findViewById<TextView>(R.id.tvFeaturedGoalName).text = metaPrincipal.nombre
            findViewById<TextView>(R.id.tvFeaturedGoalSubtitle).text = "${metaPrincipal.fechaObjetivo} • ${metaPrincipal.categoria}"
            findViewById<TextView>(R.id.tvFeaturedProgress).text =
                "$${String.format("%,.2f", metaPrincipal.montoActual)} / $${String.format("%,.2f", metaPrincipal.montoObjetivo)} — $pct%"
            findViewById<Button>(R.id.btnAddToFeaturedGoal).setOnClickListener {
                showAddSavingsDialog(metaPrincipal)
            }
        }

        // Velocity (simple calculation based on total saved)
        val metas = dbHelper.obtenerMetas()
        val avgSavings = if (metas.isNotEmpty()) totalSaved / metas.size else 0.0
        findViewById<TextView>(R.id.tvVelocityAmount).text = "+$${String.format("%,.0f", avgSavings)} prom/mes"
        findViewById<TextView>(R.id.tvVelocityDesc).text = "Basado en tu actividad de ahorro (${metas.size} meta(s))"

        // Secondary goals
        val secundarias = dbHelper.obtenerMetasSecundarias()
        val rvGoals = findViewById<RecyclerView>(R.id.rvGoals)
        rvGoals.layoutManager = LinearLayoutManager(this)
        rvGoals.adapter = GoalAdapter(secundarias) { meta -> showAddSavingsDialog(meta) }
    }

    private fun showAddSavingsDialog(meta: Meta) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_savings, null)
        dialogView.findViewById<TextView>(R.id.tvGoalNameTitle).text = meta.nombre
        dialogView.findViewById<TextView>(R.id.tvCurrentProgress).text =
            "$${String.format("%,.2f", meta.montoActual)} de $${String.format("%,.2f", meta.montoObjetivo)} ahorrado"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnConfirmSavings).setOnClickListener {
            val amountStr = dialogView.findViewById<EditText>(R.id.etSavingsAmount).text.toString()
            val amount = amountStr.toDoubleOrNull()
            if (amount != null && amount > 0) {
                dbHelper.agregarAhorroAMeta(meta.id, amount)
                dialog.dismiss()
                refreshUI()
            } else {
                Toast.makeText(this, "Por favor ingresa un monto válido", Toast.LENGTH_SHORT).show()
            }
        }

        dialogView.findViewById<Button>(R.id.btnCancelSavings).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showNewGoalDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_goal, null)
        val categories = arrayOf("Hogar", "Vehículo", "Emergencia", "Viaje", "Educación", "Otro")
        val spinner = dialogView.findViewById<Spinner>(R.id.spinnerGoalCategory)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<Button>(R.id.btnCreateGoal).setOnClickListener {
            val nombre = dialogView.findViewById<EditText>(R.id.etGoalName).text.toString().trim()
            val descripcion = dialogView.findViewById<EditText>(R.id.etGoalDescription).text.toString().trim()
            val targetStr = dialogView.findViewById<EditText>(R.id.etGoalTarget).text.toString()
            val fecha = dialogView.findViewById<EditText>(R.id.etGoalDate).text.toString().trim()
            val categoria = spinner.selectedItem.toString()
            val esPrincipal = dialogView.findViewById<CheckBox>(R.id.cbEsPrincipal).isChecked
            val target = targetStr.toDoubleOrNull()

            if (nombre.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el nombre de la meta", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (target == null || target <= 0) {
                Toast.makeText(this, "Por favor ingresa un monto objetivo válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dbHelper.insertarMeta(nombre, descripcion, target, categoria, fecha, esPrincipal)
            dialog.dismiss()
            refreshUI()
        }

        dialogView.findViewById<Button>(R.id.btnCancelGoal).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
