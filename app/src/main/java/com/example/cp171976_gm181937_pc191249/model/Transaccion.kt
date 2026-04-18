package com.example.cp171976_gm181937_pc191249

data class Transaccion(
    val id: Int,
    val monto: Double,
    val categoria: String,
    val fecha: String,
    val tipo: String // "INGRESO" o "GASTO"
)