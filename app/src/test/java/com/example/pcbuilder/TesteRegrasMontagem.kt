package com.example.pcbuilder

import org.junit.Assert.*
import org.junit.Test

class TesteRegrasMontagem {
    @Test fun catalogoTem37IdsUnicosESemPrecosInventados() {
        assertEquals(37, CatalogoComponentes.componentes.size)
        assertEquals(37, CatalogoComponentes.componentes.map { it.id }.toSet().size)
        assertTrue(CatalogoComponentes.componentes.all { it.preco.isNaN() })
    }

    @Test fun geradorRejeitaCatalogoSemCotacoes() {
        assertNull(GeradorMontagem.gerar(5000.0, "Jogos"))
        assertNull(GeradorMontagem.gerar(5000.0, "Uso geral", emptyList()))
    }

    @Test fun orcamentoAceitaFormatoBrasileiroERejeitaEntradasInvalidas() {
        assertEquals(5000.50, interpretarOrcamento("R$ 5.000,50")!!, 0.0)
        assertEquals(5000.50, interpretarOrcamento("5000.50")!!, 0.0)
        listOf("", "abc", "NaN", "Infinity", "-1", "0").forEach {
            assertNull(interpretarOrcamento(it))
        }
    }

    @Test fun compatibilidadeDetectaSocketEMemoriaDiferentes() {
        val componentes = listOf(10, 9, 18).map { requireNotNull(CatalogoComponentes.buscarPorId(it)) }
        assertEquals(2, VerificadorCompatibilidade.verificar(componentes).size)
    }

    @Test fun exportacaoSerializaEDesserializaDados() {
        val montagem = RecomendacaoMontagem(emptyList(), 5000.0, "Jogos")
        val documento = ExportadorMontagem.importar(ExportadorMontagem.exportar(montagem))
        assertEquals("Jogos", documento.finalidade)
        assertEquals(5000.0, documento.orcamento, 0.0)
        assertTrue(documento.componentes.isEmpty())
    }

    @Test fun cotacaoSemStatusAceitoOuValorValidoNaoTemPreco() {
        val invalida = OfertaFinal("Teste de valor ausente", Double.NaN, null, null)
        assertNull(CotacaoFinal(1, "com_ofertas", null, listOf(invalida)).preco)
        assertNull(CotacaoFinal(1, "em_revisao", null, emptyList()).preco)
    }
}
