package com.example.pcbuilder

data class RecomendacaoMontagem(

    val componentes: List<Componente>,

    val orcamento: Double,

    val finalidade: String,

    val categoriasFaltantes: List<String> = emptyList()

) {

    val precoTotal: Double
        get() = componentes.sumOf { it.preco }

    val dentroDoOrcamento: Boolean
        get() = precoTotal <= orcamento

    val categoriasCompletas: Boolean
        get() = categoriasFaltantes.isEmpty()
}
