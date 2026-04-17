package com.example.cp171976_gm181937_pc191249

import com.example.cp171976_gm181937_pc191249.database.DatabaseHelper
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var tipoSeleccionado = "GASTO"
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var fechaParaDB: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        dbHelper = DatabaseHelper(this)

        val btnGasto = findViewById<Button>(R.id.btnGasto)
        val btnIngreso = findViewById<Button>(R.id.btnIngreso)
        val etMonto = findViewById<EditText>(R.id.etMonto)
        val tvFecha = findViewById<TextView>(R.id.tvFechaSeleccionada)
        val groupCategorias = findViewById<ChipGroup>(R.id.groupCategorias)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmar)

        // --- 1. FUNCIÓN DE CARGA DESDE BASE DE DATOS ---
        fun cargarCategorias() {
            // Limpiamos los chips actuales (excepto el botón "+ Nueva")
            while (groupCategorias.childCount > 1) {
                groupCategorias.removeViewAt(0)
            }

            // Pedimos las categorías a la DB según el tipo (GASTO o INGRESO)
            val lista = dbHelper.obtenerCategorias(tipoSeleccionado)

            for (nombre in lista) {
                val nuevoChip = Chip(this)
                nuevoChip.text = nombre
                aplicarEstiloChip(nuevoChip)

                // Configurar el borrado permanente (Long Click)
                nuevoChip.setOnLongClickListener {
                    android.app.AlertDialog.Builder(this)
                        .setTitle("Eliminar categoría")
                        .setMessage("¿Quieres eliminar permanentemente '$nombre'?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            dbHelper.eliminarCategoria(nombre) // Borra de la DB
                            cargarCategorias() // Recarga la vista automáticamente
                        }
                        .setNegativeButton("Cancelar", null).show()
                    true
                }
                groupCategorias.addView(nuevoChip, groupCategorias.childCount - 1)
            }
        }

        // --- 2. CONFIGURACIÓN DE FECHA ---
        val calendario = Calendar.getInstance()
        val formatoVista = SimpleDateFormat("d 'de' MMMM", Locale("es", "ES"))
        val formatoDB = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        fechaParaDB = formatoDB.format(calendario.time)
        tvFecha.text = formatoVista.format(calendario.time)

        tvFecha.setOnClickListener {
            val dpd = DatePickerDialog(this, { _, y, m, d ->
                calendario.set(y, m, d)
                tvFecha.text = formatoVista.format(calendario.time)
                fechaParaDB = formatoDB.format(calendario.time)
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH))
            dpd.show()
        }

        // Carga inicial al abrir la app
        cargarCategorias()

        // --- 3. BOTONES GASTO / INGRESO ---
        btnGasto.setOnClickListener {
            tipoSeleccionado = "GASTO"
            cargarCategorias()
            btnGasto.setBackgroundColor(android.graphics.Color.BLACK)
            btnGasto.setTextColor(android.graphics.Color.WHITE)
            btnIngreso.setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"))
            btnIngreso.setTextColor(android.graphics.Color.BLACK)
        }

        btnIngreso.setOnClickListener {
            tipoSeleccionado = "INGRESO"
            cargarCategorias()
            btnIngreso.setBackgroundColor(android.graphics.Color.BLACK)
            btnIngreso.setTextColor(android.graphics.Color.WHITE)
            btnGasto.setBackgroundColor(android.graphics.Color.parseColor("#F0F0F0"))
            btnGasto.setTextColor(android.graphics.Color.BLACK)
        }

        // --- 4. FORMATEO DECIMAL (Samsung Fix) ---
        etMonto.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                formatearMonto(etMonto)
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etMonto.windowToken, 0)
                true
            } else false
        }
        etMonto.setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) formatearMonto(etMonto) }

        // --- 5. BOTÓN "+" NUEVA CATEGORÍA ---
        findViewById<Chip>(R.id.chipNueva).setOnClickListener {
            val input = EditText(this)
            val lp = android.widget.LinearLayout.LayoutParams(-1, -2)
            val margin = (24 * resources.displayMetrics.density).toInt()
            lp.setMargins(margin, 20, margin, 0)
            input.layoutParams = lp
            input.hint = "Ej: 🕹️ Gaming"

            android.app.AlertDialog.Builder(this)
                .setTitle("Nueva Categoría")
                .setView(input)
                .setPositiveButton("AÑADIR") { _, _ ->
                    val nombre = input.text.toString().trim()
                    if (nombre.isNotEmpty()) {
                        val insertado = dbHelper.insertarNuevaCategoria(nombre, tipoSeleccionado)

                        if (insertado) {
                            cargarCategorias() // Esta función recarga los chips en pantalla
                            Toast.makeText(this, "✅ Categoría añadida", Toast.LENGTH_SHORT).show()
                        } else {
                            // ESTO ES LO QUE TE AVISA SI YA EXISTE
                            Toast.makeText(this, "⚠️ Ya existe una categoría similar", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "⚠️ El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("CANCELAR", null).show()
        }

        // --- 6. BOTÓN CONFIRMAR ---
        btnConfirmar.setOnClickListener {
            val montoTexto = etMonto.text.toString()
            val idChip = groupCategorias.checkedChipId
            if (idChip == -1) {
                Toast.makeText(this, "⚠️ Selecciona una categoría", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (montoTexto.isNotEmpty()) {
                try {
                    val montoNum = montoTexto.toDouble()
                    if (montoNum > 0) {
                        val cat = findViewById<Chip>(idChip).text.toString()
                        dbHelper.insertarTransaccion(tipoSeleccionado, montoNum, cat, fechaParaDB)
                        Toast.makeText(this, "✅ Registro guardado", Toast.LENGTH_SHORT).show()
                        etMonto.text.clear()
                        groupCategorias.clearCheck()
                    } else {
                        Toast.makeText(this, "⚠️ El monto debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, "❌ Formato no válido", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "⚠️ Ingresa un monto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun aplicarEstiloChip(chip: Chip) {
        chip.isCheckable = true
        chip.isClickable = true
        chip.chipStrokeWidth = 0f
        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))
        val colorsPill = intArrayOf(android.graphics.Color.parseColor("#D9CFE9"), android.graphics.Color.parseColor("#E7E0E8"))
        chip.chipBackgroundColor = android.content.res.ColorStateList(states, colorsPill)
        val colorsText = intArrayOf(android.graphics.Color.parseColor("#6750A4"), android.graphics.Color.parseColor("#49454F"))
        chip.setTextColor(android.content.res.ColorStateList(states, colorsText))
        chip.typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
        chip.textSize = 14f
        chip.includeFontPadding = false
        chip.shapeAppearanceModel = chip.shapeAppearanceModel.toBuilder().setAllCornerSizes(50f).build()
    }

    private fun formatearMonto(et: EditText) {
        val t = et.text.toString()
        if (t.isNotEmpty()) {
            try {
                et.setText(String.format("%.2f", t.toDouble()))
            } catch (e: Exception) {}
        }
    }
}