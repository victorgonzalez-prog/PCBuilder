package com.example.pcbuilder

data class Componente(
    val id: Int,
    val nome: String,
    val categoria: String,
    val fabricante: String,
    val preco: Double,
    val descricao: String?,
    val especificacao: String?,
    val imagemResId: Int?
)