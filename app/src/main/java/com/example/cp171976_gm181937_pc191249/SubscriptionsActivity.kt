package com.example.cp171976_gm181937_pc191249

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cp171976_gm181937_pc191249.database.DatabaseHelper
import com.example.cp171976_gm181937_pc191249.model.Suscripcion
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class SubscriptionsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: SubscriptionAdapter
    private lateinit var tvTotalSpend: TextView
    private lateinit var tvNextBilling: TextView
    private lateinit var tvActiveServices: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        dbHelper = DatabaseHelper(this)

        tvTotalSpend = findViewById(R.id.tvTotalSpend)
        tvNextBilling = findViewById(R.id.tvNextBilling)
        tvActiveServices = findViewById(R.id.tvActiveServices)

        setupRecyclerView()
        setupBottomNavigation()

        findViewById<FloatingActionButton>(R.id.fabAddSubscription).setOnClickListener {
            mostrarDialogoNuevaSuscripcion()
        }

        actualizarUI()
    }

    private fun setupRecyclerView() {
        val rv = findViewById<RecyclerView>(R.id.rvSubscriptions)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = SubscriptionAdapter(
            emptyList(),
            onPaidClick = { sub -> registrarPagoRapido(sub) },
            onDeleteClick = { sub -> confirmarEliminacion(sub) }
        )
        rv.adapter = adapter
    }

    private fun actualizarUI() {
        val subs = dbHelper.obtenerSuscripciones()
        adapter.updateData(subs)

        val total = dbHelper.obtenerSumaSuscripciones()
        tvTotalSpend.text = "$${String.format("%.2f", total)}"
        tvActiveServices.text = subs.size.toString()

        if (subs.isNotEmpty()) {
            val hoy = Calendar.getInstance()
            val diaHoy = hoy.get(Calendar.DAY_OF_MONTH)
            
            // Buscar el próximo día de pago
            val proximo = subs.minByOrNull { sub ->
                if (sub.diaPago >= diaHoy) sub.diaPago - diaHoy
                else (30 - diaHoy) + sub.diaPago
            }
            
            proximo?.let {
                val mes = if (it.diaPago >= diaHoy) {
                    hoy.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                } else {
                    val proxMes = (Calendar.getInstance()).apply { add(Calendar.MONTH, 1) }
                    proxMes.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                }
                tvNextBilling.text = "$mes ${it.diaPago}"
            }
        } else {
            tvNextBilling.text = "---"
        }
    }

    private fun registrarPagoRapido(sub: Suscripcion) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fecha = sdf.format(Date())
        
        dbHelper.insertarTransaccion(
            nombre = sub.nombre,
            tipo = "GASTO",
            monto = sub.monto,
            categoria = "📺 Suscrip.",
            fecha = fecha
        )
        
        Toast.makeText(this, "Pago de ${sub.nombre} registrado", Toast.LENGTH_SHORT).show()
    }

    private fun confirmarEliminacion(sub: Suscripcion) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar suscripción")
            .setMessage("¿Estás seguro de que quieres eliminar ${sub.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                dbHelper.eliminarSuscripcion(sub.id)
                actualizarUI()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoNuevaSuscripcion() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_add_subscription, null)
        builder.setView(dialogView)

        val etNombre = dialogView.findViewById<EditText>(R.id.etSubNombre)
        val etMonto = dialogView.findViewById<EditText>(R.id.etSubMonto)
        val etDia = dialogView.findViewById<EditText>(R.id.etSubDia)

        builder.setTitle("Nueva Suscripción")
            .setPositiveButton("Guardar") { _, _ ->
                val nombre = etNombre.text.toString()
                val monto = etMonto.text.toString().toDoubleOrNull() ?: 0.0
                val dia = etDia.text.toString().toIntOrNull() ?: 1

                if (nombre.isNotEmpty()) {
                    dbHelper.insertarSuscripcion(nombre, monto, dia, "Suscripción")
                    actualizarUI()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupBottomNavigation() {
        val nav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        nav.selectedItemId = R.id.nav_subscriptions
        nav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_subscriptions -> true
                else -> false
            }
        }
    }
}
