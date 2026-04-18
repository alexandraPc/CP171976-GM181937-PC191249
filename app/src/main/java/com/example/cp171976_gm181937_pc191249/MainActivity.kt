package com.example.cp171976_gm181937_pc191249

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager // IMPORTANTE
import androidx.recyclerview.widget.RecyclerView    // IMPORTANTE
import com.example.cp171976_gm181937_pc191249.database.DatabaseHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DatabaseHelper(this)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarTotales()
        // LLAMADA CLAVE: Esto hará que la lista aparezca al abrir la app y al volver de registrar
        cargarHistorial()
    }

    private fun actualizarTotales() {
        val ingresos = dbHelper.obtenerSumaPorTipo("INGRESO")
        val gastos = dbHelper.obtenerSumaPorTipo("GASTO")
        val saldo = ingresos - gastos

        findViewById<TextView>(R.id.tvIngresosHome).text = "$${String.format("%.2f", ingresos)}"
        findViewById<TextView>(R.id.tvGastosHome).text = "$${String.format("%.2f", gastos)}"
        findViewById<TextView>(R.id.tvDineroLibre).text = "$${String.format("%.2f", saldo)}"

        val cardSemaforo = findViewById<CardView>(R.id.cardSemaforo)
        if (saldo < 0.0) {
            cardSemaforo.setCardBackgroundColor(Color.parseColor("#C62828"))
        } else {
            cardSemaforo.setCardBackgroundColor(Color.parseColor("#4CAF50"))
        }
    }

    private fun cargarHistorial() {
        // Asegúrate de que esta función en tu dbHelper retorne una List<Transaccion>
        val listaActualizada = dbHelper.obtenerUltimosGastos()

        val recyclerView = findViewById<RecyclerView>(R.id.rvTransacciones)

        // Configuramos el "cómo" se ve (lista vertical)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Le pasamos el Adapter que creamos antes
        val adapter = TransaccionAdapter(listaActualizada)
        recyclerView.adapter = adapter
    }
}