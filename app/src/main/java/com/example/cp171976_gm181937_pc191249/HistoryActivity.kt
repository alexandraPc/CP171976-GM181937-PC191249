package com.example.cp171976_gm181937_pc191249

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cp171976_gm181937_pc191249.database.DatabaseHelper
import com.example.cp171976_gm181937_pc191249.databinding.ActivityHistoryBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var dbHelper: DatabaseHelper
    private var filterQuery: String? = null
    private var filterType: String? = "Todos"
    private var filterCategory: String? = "Todas"
    private var filterStartDate: String? = null
    private var filterEndDate: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = DatabaseHelper(this)
        setupNavigation()
        setupFilters()
        
        binding.tvClearFilters.setOnClickListener {
            clearFilters()
        }
        
        cargarHistorial()
    }


    private fun clearFilters() {
        filterQuery = null
        filterType = "Todos"
        filterCategory = "Todas"
        filterStartDate = null
        filterEndDate = null
        
        binding.etSearch.text = null
        binding.chipAll.isChecked = true
        binding.spinnerCategory.setSelection(0)
        binding.btnDateRange.text = "Rango de fecha"
        
        cargarHistorial()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_history
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_history -> true
                R.id.nav_subscriptions -> {
                    startActivity(Intent(this, SubscriptionsActivity::class.java))
                    finish()
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

    private fun setupFilters() {
        // Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterQuery = s?.toString()
                cargarHistorial()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Chips
        binding.chipGroupType.setOnCheckedChangeListener { _, checkedId ->
            filterType = when (checkedId) {
                R.id.chipIncome -> "INGRESO"
                R.id.chipExpense -> "GASTO"
                else -> "Todos"
            }
            cargarHistorial()
        }

        // Category Spinner
        val categories = mutableListOf("Todas")
        categories.addAll(dbHelper.obtenerCategorias("INGRESO"))
        categories.addAll(dbHelper.obtenerCategorias("GASTO"))
        val distinctCategories = categories.distinct()
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, distinctCategories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
        
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterCategory = distinctCategories[position]
                cargarHistorial()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Date Picker
        binding.btnDateRange.setOnClickListener {
            val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Selecciona rango de fechas")
                .build()

            dateRangePicker.addOnPositiveButtonClickListener { selection ->
                val sdfDB = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val sdfUI = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                
                filterStartDate = sdfDB.format(Date(selection.first))
                filterEndDate = sdfDB.format(Date(selection.second))
                
                binding.btnDateRange.text = "${sdfUI.format(Date(selection.first))} - ${sdfUI.format(Date(selection.second))}"
                cargarHistorial()
            }
            dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
        }
    }

    private fun cargarHistorial() {
        val transacciones = dbHelper.obtenerTransaccionesFiltradas(
            query = filterQuery,
            categoria = filterCategory,
            fechaInicio = filterStartDate,
            fechaFin = filterEndDate,
            tipo = filterType
        )

        binding.tvClearFilters.visibility = if (isAnyFilterActive()) View.VISIBLE else View.GONE

        val items = mutableListOf<HistoryItem>()
        var lastDate = ""

        for (t in transacciones) {
            if (t.fecha != lastDate) {
                items.add(HistoryItem.Header(t.fecha))
                lastDate = t.fecha
            }
            items.add(HistoryItem.Transaction(t))
        }

        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = HistorySectionAdapter(items)
    }

    private fun isAnyFilterActive(): Boolean {
        return !filterQuery.isNullOrEmpty() || 
               filterType != "Todos" || 
               filterCategory != "Todas" || 
               filterStartDate != null
    }
}

sealed class HistoryItem {
    data class Header(val date: String) : HistoryItem()
    data class Transaction(val transaccion: Transaccion) : HistoryItem()
}
