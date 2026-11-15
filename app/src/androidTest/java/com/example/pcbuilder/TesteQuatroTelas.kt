package com.example.pcbuilder

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class TesteQuatroTelas {
    @get:Rule val composicao = createAndroidComposeRule<ComponentActivity>()
    private val armazenamentoModelos = ViewModelStore()

    private fun abrir(fonte: FonteCotacoes = object : FonteCotacoes {
        override suspend fun buscarCotacoes() = emptyMap<Int, CotacaoFinal>()
    }): ModeloPcBuilder {
        lateinit var modelo: ModeloPcBuilder
        composicao.runOnUiThread {
            modelo = ModeloPcBuilder(fonte)
            armazenamentoModelos.put("teste", modelo)
        }
        composicao.setContent { MaterialTheme { AplicativoPcBuilder(modelo) } }
        return modelo
    }

    @After fun encerrar() { composicao.runOnUiThread { armazenamentoModelos.clear() } }

    @Test fun catalogoSelecionaRemoveEAlternaEntreQuatroTelas() {
        abrir()
        composicao.onNodeWithText("Catálogo de componentes").assertIsDisplayed()
        composicao.onNodeWithText("Placa-mãe").performClick()
        composicao.onNodeWithText("ASUS TUF Gaming B650M").assertIsDisplayed()
        composicao.onNodeWithText("Voltar às categorias").performClick()
        composicao.onNodeWithText("Processador").performClick()
        composicao.onAllNodesWithText("Adicionar à montagem")[0].performClick()
        composicao.onNodeWithText("Minha Montagem").performClick()
        composicao.onNodeWithText("Ryzen 5 9600X").assertIsDisplayed()
        composicao.onNodeWithText("Total indisponível: 1 componente(s) sem cotação.").assertIsDisplayed()
        composicao.onNodeWithText("Remover").performClick()
        composicao.onNodeWithText("Nenhum componente adicionado.").assertIsDisplayed()
        composicao.onNodeWithText("Orçamento").performClick()
        composicao.onNodeWithText("Montagem Inteligente").assertIsDisplayed()
        composicao.onNodeWithText("Preços").performClick()
        composicao.onNodeWithText("Comparador de preços").assertIsDisplayed()
        composicao.onAllNodesWithText("Sem cotação disponível")[0].assertIsDisplayed()
        composicao.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composicao.onNodeWithText("Processador").assertIsDisplayed()
        composicao.onNodeWithText("Voltar às categorias").performClick()
        composicao.onNodeWithText("Catálogo de componentes").assertIsDisplayed()
    }

    @Test fun orcamentoInvalidoMostraErroESemPrecosNaoGeraMontagem() {
        abrir()
        composicao.onNodeWithText("Orçamento").performClick()
        composicao.onNodeWithText("Orçamento em R$").performTextClearance()
        composicao.onNodeWithText("Orçamento em R$").performTextInput("abc")
        composicao.onNodeWithText("Gerar configuração").performScrollTo().performClick()
        composicao.onNodeWithText("Informe um orçamento válido maior que zero.").assertExists()
        composicao.onNodeWithText("Orçamento em R$").performTextClearance()
        composicao.onNodeWithText("Orçamento em R$").performTextInput("5.000,00")
        composicao.onNodeWithText("Gerar configuração").performScrollTo().performClick()
        composicao.waitUntil(10000) {
            composicao.onAllNodesWithText("Não há uma montagem completa compatível e com todos os preços disponíveis nesse orçamento.")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composicao.onNodeWithText("Aplicar à Minha Montagem").assertDoesNotExist()
    }

    @Test fun carregamentoErroENovaConsultaTemFeedback() {
        val resposta = CompletableDeferred<Map<Int, CotacaoFinal>>()
        var falhar = true
        val fonte = object : FonteCotacoes {
            override suspend fun buscarCotacoes(): Map<Int, CotacaoFinal> {
                resposta.await()
                if (falhar) throw IOException("Conexão indisponível")
                return emptyMap()
            }
        }
        val modelo = abrir(fonte)
        composicao.onNodeWithText("Consultando...").assertIsDisplayed().assertIsNotEnabled()
        resposta.complete(emptyMap())
        composicao.waitUntil(10000) {
            composicao.onAllNodesWithText("Não foi possível consultar a API: Conexão indisponível")
                .fetchSemanticsNodes().isNotEmpty()
        }
        falhar = false
        composicao.runOnUiThread { modelo.atualizar() }
        composicao.onNodeWithText("Nenhuma cotação disponível no momento.").assertExists()
        composicao.onNodeWithText("Preços").performClick()
        composicao.onNodeWithText("Nenhuma cotação disponível no momento.").assertIsDisplayed()
    }

    @Test fun cotacoesReaisGeramMontagemAplicamEExportamJson() {
        val contexto = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().context
        val documento = org.json.JSONObject(
            contexto.assets.open("cotacoes-reais.json").bufferedReader().use { it.readText() }
        )
        val produtos = documento.getJSONArray("produtos")
        val cotacoes = mutableMapOf<Int, CotacaoFinal>()
        for (i in 0 until produtos.length()) {
            val produto = produtos.getJSONObject(i)
            val ofertasJson = produto.getJSONArray("ofertas")
            val ofertas = (0 until ofertasJson.length()).map { j ->
                val oferta = ofertasJson.getJSONObject(j)
                OfertaFinal(oferta.getString("loja"), oferta.getDouble("preco"),
                    oferta.optString("url"), oferta.optString("titulo"))
            }
            val id = produto.getInt("catalogoId")
            cotacoes[id] = CotacaoFinal(id, produto.getString("status"), null, ofertas)
        }
        org.junit.Assert.assertTrue(cotacoes.values.any { it.preco != null })
        val modelo = abrir(object : FonteCotacoes {
            override suspend fun buscarCotacoes() = cotacoes.toMap()
        })
        composicao.onNodeWithText("Orçamento").performClick()
        composicao.onNodeWithText("Orçamento em R$").performTextClearance()
        composicao.onNodeWithText("Orçamento em R$").performTextInput("30000")
        composicao.onNodeWithText("Gerar configuração").performScrollTo().performClick()
        composicao.waitUntil(15000) { modelo.recomendacao.value != null }
        val montagem = requireNotNull(modelo.recomendacao.value)
        org.junit.Assert.assertTrue(montagem.precoTotal <= 30000)
        org.junit.Assert.assertTrue(montagem.componentes.all { it.preco.isFinite() && it.preco > 0 })
        org.junit.Assert.assertTrue(VerificadorCompatibilidade.verificar(montagem.componentes).isEmpty())
        composicao.onNodeWithText("Copiar JSON da configuração").performScrollTo().performClick()
        composicao.runOnUiThread {
            val areaTransferencia = composicao.activity.getSystemService(android.content.ClipboardManager::class.java)
            val exportada = ExportadorMontagem.importar(areaTransferencia.primaryClip!!.getItemAt(0).text.toString())
            org.junit.Assert.assertEquals(montagem.componentes.size, exportada.componentes.size)
            org.junit.Assert.assertEquals(montagem.precoTotal, exportada.precoTotal, 0.001)
        }
        composicao.onNodeWithText("Aplicar à Minha Montagem").performScrollTo().performClick()
        composicao.onNodeWithText("Minha Montagem").performClick()
        composicao.onNodeWithText("Limpar montagem").assertIsDisplayed().performClick()
        composicao.onNodeWithText("Nenhum componente adicionado.").assertIsDisplayed()
        composicao.onNodeWithText("Preços").performClick()
        composicao.onAllNodesWithText("Abrir página da oferta")[0].assertIsDisplayed()
        composicao.onNodeWithText("Montagem gerada usando somente preços consultados.").assertDoesNotExist()
    }
}
