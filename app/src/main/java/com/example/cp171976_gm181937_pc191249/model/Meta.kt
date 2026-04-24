package com.example.cp171976_gm181937_pc191249.model

data class Meta(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val montoObjetivo: Double,
    val montoActual: Double,
    val categoria: String,
    val fechaObjetivo: String,
    val esPrincipal: Boolean
)
