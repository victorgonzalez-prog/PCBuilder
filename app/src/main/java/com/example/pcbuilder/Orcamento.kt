package com.example.pcbuilder

fun interpretarOrcamento(texto: String): Double? {
    val limpo = texto.trim().removePrefix("R$").trim()
    val normalizado = if (',' in limpo) limpo.replace(".", "").replace(',', '.') else limpo
    return normalizado.toDoubleOrNull()?.takeIf { it.isFinite() && it > 0 }
}
