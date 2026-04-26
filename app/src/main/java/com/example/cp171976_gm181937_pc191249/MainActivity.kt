package com.example.cp171976_gm181937_pc191249

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cp171976_gm181937_pc191249.database.DatabaseHelper
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.parseColor("#091929")
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)
        val btnAdd = findViewById<android.widget.ImageButton>(R.id.btnAddTransaction)
        btnAdd.setOnClickListener {
            // Esto abrirá tu pantalla de registro
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        findViewById<TextView>(R.id.btnVerTodo).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        val bottomNavigation = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.nav_home
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    true
                }
                R.id.nav_subscriptions -> {
                    startActivity(Intent(this, SubscriptionsActivity::class.java))
                    true
                }
                R.id.nav_goals -> {
                    startActivity(Intent(this, GoalsActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarDashboard()
        cargarHistorial()
    }

    private fun actualizarDashboard() {
        val ingresos = dbHelper.obtenerSumaPorTipo("INGRESO")
        val gastos = dbHelper.obtenerSumaPorTipo("GASTO")
        val suscripciones = dbHelper.obtenerSumaSuscripciones()
        val netWorth = ingresos - gastos
        val freeMoney = netWorth - suscripciones
        val income = ingresos.coerceAtLeast(1.0)
        val freePercent = ((freeMoney / income) * 100).toInt().coerceIn(0, 100)

        // Total Net Worth
        findViewById<TextView>(R.id.tvNetWorth).text = "$${String.format("%,.2f", netWorth)}"

        // Overall Status
        val tvStatusLabel = findViewById<TextView>(R.id.tvStatusLabel)
        val tvStatusDesc = findViewById<TextView>(R.id.tvStatusDescription)
        val tvStatusGreen = findViewById<TextView>(R.id.tvStatusGreen)
        when {
            netWorth > 0 -> {
                tvStatusLabel.text = "Estable y Óptimo"
                tvStatusLabel.setTextColor(Color.parseColor("#30D64A"))
                tvStatusDesc.text = "Tu balance es positivo. Estás en buena posición para alcanzar tus metas financieras."
                tvStatusGreen.setBackgroundResource(R.drawable.bg_status_green)
            }
            netWorth < 0 -> {
                tvStatusLabel.text = "En Riesgo"
                tvStatusLabel.setTextColor(Color.parseColor("#FF5252"))
                tvStatusDesc.text = "Tus gastos superan tus ingresos. Considera revisar tu gasto para mejorar tu balance."
                tvStatusGreen.setBackgroundResource(R.drawable.bg_status_red)
            }
            else -> {
                tvStatusLabel.text = "Neutral"
                tvStatusLabel.setTextColor(Color.parseColor("#E29100"))
                tvStatusDesc.text = "Registra tus ingresos y gastos para ver tu resumen financiero."
                tvStatusGreen.setBackgroundResource(R.drawable.bg_status_amber)
            }
        }

        // Daily Average (expenses / days elapsed this month)
        val calendar = Calendar.getInstance()
        val daysElapsed = calendar.get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
        val dailyAvg = gastos / daysElapsed
        findViewById<TextView>(R.id.tvDailyAverage).text = "$${String.format("%.2f", dailyAvg)}"

        // Monthly Commitments (subscriptions)
        findViewById<TextView>(R.id.tvMonthlyCommitments).text = "$${String.format("%,.2f", suscripciones)}"

        // Free Money
        val tvFreeMoney = findViewById<TextView>(R.id.tvFreeMoney)
        tvFreeMoney.text = "$${String.format("%,.2f", freeMoney.coerceAtLeast(0.0))}"

        // Progress bar
        findViewById<ProgressBar>(R.id.progressFreeMoney).progress = freePercent
        findViewById<TextView>(R.id.tvProgressPercent).text = "$freePercent% DEL\nPRESUPUESTO\nMENSUAL"
        findViewById<TextView>(R.id.tvProgressSpent).text = "$${String.format("%,.2f", gastos)} Gastado"
        findViewById<TextView>(R.id.tvProgressTarget).text = "$${String.format("%,.2f", ingresos)} Objetivo"
    }

    private fun cargarHistorial() {
        val lista = dbHelper.obtenerUltimosGastos()
        val recyclerView = findViewById<RecyclerView>(R.id.rvTransacciones)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = TransaccionAdapter(lista)
    }
}
