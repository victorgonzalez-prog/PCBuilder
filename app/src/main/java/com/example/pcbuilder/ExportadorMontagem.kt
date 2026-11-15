package com.example.pcbuilder

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ComponenteExportado(
    val id: Int,
    val nome: String,
    val categoria: String,
    val preco: Double
)

@Serializable
data class MontagemExportada(
    val finalidade: String,
    val orcamento: Double,
    val precoTotal: Double,
    val componentes: List<ComponenteExportado>
)

object ExportadorMontagem {

    private val json = Json {
        prettyPrint = true
    }

    fun exportar(montagem: RecomendacaoMontagem): String {

        val componentes = montagem.componentes.map { componente ->

            ComponenteExportado(
                id = componente.id,
                nome = componente.nome,
                categoria = componente.categoria,
                preco = componente.preco
            )
        }

        val documento = MontagemExportada(
            finalidade = montagem.finalidade,
            orcamento = montagem.orcamento,
            precoTotal = montagem.precoTotal,
            componentes = componentes
        )

        return json.encodeToString(documento)
    }

    fun importar(conteudo: String): MontagemExportada {
        return json.decodeFromString<MontagemExportada>(conteudo)
    }
}
