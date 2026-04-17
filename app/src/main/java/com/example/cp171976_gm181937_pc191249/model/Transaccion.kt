package com.example.cp171976_gm181937_pc191249.model

data class Transaccion (
    val id: Int? = null,           // El ID lo genera SQLite solo
    val tipo: String,              // "Gasto" o "Ingreso"
    val monto: Double,             // Cantidad de dinero
    val categoria: String,         // Ej: "Alimentación", "Transporte"
    val fecha: String              // Guardaremos la fecha como texto (YYYY-MM-DD)
)