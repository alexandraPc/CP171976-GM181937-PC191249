package com.example.cp171976_gm181937_pc191249.database

import com.example.cp171976_gm181937_pc191249.Transaccion
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "Finanzas.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        // 1. Tabla de transacciones (Donde se guardan los gastos/ingresos)
        db?.execSQL("CREATE TABLE transacciones (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, tipo TEXT, monto REAL, categoria TEXT, fecha TEXT)")

        // 2. Tabla de categorías (Donde se guardan las etiquetas como "🍔 Antojos")
        db?.execSQL("CREATE TABLE categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, tipo TEXT)")

        // 3. Insertamos las categorías por defecto para que la app no empiece vacía
        insertarCategoriasBase(db)
    }

    private fun insertarCategoriasBase(db: SQLiteDatabase?) {
        val categorias = listOf(
            "🍔 Antojos" to "GASTO", "🎉 Fiesta" to "GASTO", "🚌 Transporte" to "GASTO", "📺 Suscrip." to "GASTO", "🏥 Salud" to "GASTO",
            "💰 Salario" to "INGRESO", "⏱️ Horas Extra" to "INGRESO", "🎁 Regalo" to "INGRESO", "📈 Inversión" to "INGRESO"
        )
        for (cat in categorias) {
            val v = ContentValues().apply {
                put("nombre", cat.first)
                put("tipo", cat.second)
            }
            db?.insert("categorias", null, v)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS transacciones")
        db?.execSQL("DROP TABLE IF EXISTS categorias")
        onCreate(db)
    }

    // --- FUNCIONES QUE LLAMA TU MAIN ACTIVITY ---

    fun insertarTransaccion(nombre: String, tipo: String, monto: Double, categoria: String, fecha: String): Long {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("nombre", nombre); put("tipo", tipo); put("monto", monto); put("categoria", categoria); put("fecha", fecha)
        }
        return db.insert("transacciones", null, v)
    }

    fun obtenerCategorias(tipo: String): MutableList<String> {
        val lista = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT nombre FROM categorias WHERE tipo = ?", arrayOf(tipo))
        while (cursor.moveToNext()) {
            lista.add(cursor.getString(0))
        }
        cursor.close()
        return lista
    }

    fun insertarNuevaCategoria(nombre: String, tipo: String): Boolean {
        val db = this.writableDatabase

        // 1. Obtener todas las categorías actuales del mismo tipo
        val cursor = db.rawQuery("SELECT nombre FROM categorias WHERE tipo = ?", arrayOf(tipo))

        // 2. Función interna para limpiar texto (quita emojis y espacios raros)
        fun limpiar(texto: String): String {
            return texto.replace(Regex("[^\\p{L}\\p{N}]"), "").lowercase()
        }

        val nombreNuevoLimpio = limpiar(nombre)
        var existe = false

        // 3. Comparar el nuevo nombre con los que ya existen
        while (cursor.moveToNext()) {
            val nombreExistenteLimpio = limpiar(cursor.getString(0))
            if (nombreNuevoLimpio == nombreExistenteLimpio) {
                existe = true
                break
            }
        }
        cursor.close()

        // 4. Si no existe, insertar el nombre tal cual lo escribió el usuario
        if (!existe) {
            val v = ContentValues().apply {
                put("nombre", nombre)
                put("tipo", tipo)
            }
            db.insert("categorias", null, v)
            return true
        }

        return false
    }

    fun eliminarCategoria(nombre: String) {
        this.writableDatabase.delete("categorias", "nombre = ?", arrayOf(nombre))
    }

    fun obtenerSumaPorTipo(tipo: String): Double {
        var total = 0.0
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(monto) FROM transacciones WHERE tipo = ?", arrayOf(tipo))
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }
    fun obtenerUltimosGastos(): List<Transaccion> {
        return obtenerTransaccionesFiltradas()
    }

    fun obtenerTransaccionesFiltradas(
        query: String? = null,
        categoria: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null,
        tipo: String? = null
    ): List<Transaccion> {
        val lista = mutableListOf<Transaccion>()
        val db = this.readableDatabase

        var selection = ""
        val selectionArgs = mutableListOf<String>()

        if (!query.isNullOrEmpty()) {
            selection += "nombre LIKE ?"
            selectionArgs.add("%$query%")
        }

        if (!categoria.isNullOrEmpty() && categoria != "Todas") {
            if (selection.isNotEmpty()) selection += " AND "
            selection += "categoria = ?"
            selectionArgs.add(categoria)
        }

        if (!fechaInicio.isNullOrEmpty() && !fechaFin.isNullOrEmpty()) {
            if (selection.isNotEmpty()) selection += " AND "
            selection += "fecha BETWEEN ? AND ?"
            selectionArgs.add(fechaInicio)
            selectionArgs.add(fechaFin)
        }

        if (!tipo.isNullOrEmpty() && tipo != "Todos") {
            if (selection.isNotEmpty()) selection += " AND "
            selection += "tipo = ?"
            selectionArgs.add(tipo)
        }

        val cursor = db.query(
            "transacciones",
            null,
            if (selection.isEmpty()) null else selection,
            if (selectionArgs.isEmpty()) null else selectionArgs.toTypedArray(),
            null,
            null,
            "fecha DESC, id DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                lista.add(Transaccion(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("monto")),
                    cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fecha")),
                    cursor.getString(cursor.getColumnIndexOrThrow("tipo"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }
}