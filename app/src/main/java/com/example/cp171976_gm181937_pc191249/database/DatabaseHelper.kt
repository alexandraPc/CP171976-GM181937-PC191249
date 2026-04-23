package com.example.cp171976_gm181937_pc191249.database

import com.example.cp171976_gm181937_pc191249.Transaccion
import com.example.cp171976_gm181937_pc191249.model.Meta
import com.example.cp171976_gm181937_pc191249.model.Suscripcion
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "Finanzas.db", null, 5) {

    override fun onCreate(db: SQLiteDatabase?) {
        // 1. Tabla de transacciones (Donde se guardan los gastos/ingresos)
        db?.execSQL("CREATE TABLE transacciones (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, tipo TEXT, monto REAL, categoria TEXT, fecha TEXT)")

        // 2. Tabla de categorías (Donde se guardan las etiquetas como "🍔 Antojos")
        db?.execSQL("CREATE TABLE categorias (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, tipo TEXT)")

        // 3. Tabla de suscripciones
        db?.execSQL("CREATE TABLE suscripciones (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, monto REAL, diaPago INTEGER, categoria TEXT)")

        // 4. Tabla de metas de ahorro
        db?.execSQL("CREATE TABLE metas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, monto_objetivo REAL, monto_actual REAL DEFAULT 0, categoria TEXT, fecha_objetivo TEXT, es_principal INTEGER DEFAULT 0)")

        // 5. Insertamos las categorías por defecto para que la app no empiece vacía
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
        if (oldVersion < 3) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS suscripciones (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, monto REAL, diaPago INTEGER, categoria TEXT)")
        }
        if (oldVersion < 5) {
            db?.execSQL("CREATE TABLE IF NOT EXISTS metas (id INTEGER PRIMARY KEY AUTOINCREMENT, nombre TEXT, descripcion TEXT, monto_objetivo REAL, monto_actual REAL DEFAULT 0, categoria TEXT, fecha_objetivo TEXT, es_principal INTEGER DEFAULT 0)")
        }
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
        return obtenerTransaccionesFiltradas(limit = 5)
    }

    fun obtenerTransaccionesFiltradas(
        query: String? = null,
        categoria: String? = null,
        fechaInicio: String? = null,
        fechaFin: String? = null,
        tipo: String? = null,
        limit: Int? = null
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
            "fecha DESC, id DESC",
            limit?.toString()
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

    // --- FUNCIONES PARA SUSCRIPCIONES ---

    fun insertarSuscripcion(nombre: String, monto: Double, diaPago: Int, categoria: String): Long {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("nombre", nombre)
            put("monto", monto)
            put("diaPago", diaPago)
            put("categoria", categoria)
        }
        return db.insert("suscripciones", null, v)
    }

    fun eliminarSuscripcion(id: Int) {
        this.writableDatabase.delete("suscripciones", "id = ?", arrayOf(id.toString()))
    }

    fun obtenerSuscripciones(): List<Suscripcion> {
        val lista = mutableListOf<Suscripcion>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM suscripciones", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Suscripcion(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("monto")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("diaPago")),
                    cursor.getString(cursor.getColumnIndexOrThrow("categoria"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun obtenerSumaSuscripciones(): Double {
        var total = 0.0
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(monto) FROM suscripciones", null)
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    // --- FUNCIONES PARA METAS DE AHORRO ---

    fun insertarMeta(nombre: String, descripcion: String, montoObjetivo: Double, categoria: String, fechaObjetivo: String, esPrincipal: Boolean): Long {
        val db = this.writableDatabase
        val v = ContentValues().apply {
            put("nombre", nombre)
            put("descripcion", descripcion)
            put("monto_objetivo", montoObjetivo)
            put("monto_actual", 0.0)
            put("categoria", categoria)
            put("fecha_objetivo", fechaObjetivo)
            put("es_principal", if (esPrincipal) 1 else 0)
        }
        return db.insert("metas", null, v)
    }

    fun obtenerMetas(): List<Meta> {
        val lista = mutableListOf<Meta>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM metas", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Meta(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("monto_objetivo")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("monto_actual")),
                    cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fecha_objetivo")),
                    cursor.getInt(cursor.getColumnIndexOrThrow("es_principal")) == 1
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun obtenerMetaPrincipal(): Meta? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM metas WHERE es_principal = 1 LIMIT 1", null)
        var meta: Meta? = null
        if (cursor.moveToFirst()) {
            meta = Meta(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("monto_objetivo")),
                cursor.getDouble(cursor.getColumnIndexOrThrow("monto_actual")),
                cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                cursor.getString(cursor.getColumnIndexOrThrow("fecha_objetivo")),
                true
            )
        }
        cursor.close()
        return meta
    }

    fun obtenerMetasSecundarias(): List<Meta> {
        val lista = mutableListOf<Meta>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM metas WHERE es_principal = 0", null)
        if (cursor.moveToFirst()) {
            do {
                lista.add(Meta(
                    cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("nombre")),
                    cursor.getString(cursor.getColumnIndexOrThrow("descripcion")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("monto_objetivo")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("monto_actual")),
                    cursor.getString(cursor.getColumnIndexOrThrow("categoria")),
                    cursor.getString(cursor.getColumnIndexOrThrow("fecha_objetivo")),
                    false
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return lista
    }

    fun agregarAhorroAMeta(id: Int, monto: Double) {
        val db = this.writableDatabase
        db.execSQL("UPDATE metas SET monto_actual = monto_actual + ? WHERE id = ?", arrayOf(monto, id))
    }

    fun obtenerTotalAhorrado(): Double {
        var total = 0.0
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(monto_actual) FROM metas", null)
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        return total
    }

    fun obtenerPorcentajeTotalMetas(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT SUM(monto_actual), SUM(monto_objetivo) FROM metas", null)
        var pct = 0
        if (cursor.moveToFirst()) {
            val actual = cursor.getDouble(0)
            val objetivo = cursor.getDouble(1)
            if (objetivo > 0) {
                pct = ((actual / objetivo) * 100).toInt().coerceIn(0, 100)
            }
        }
        cursor.close()
        return pct
    }
}
