package com.example.pcbuilder

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as itensGrade
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

private val URL_API = BuildConfig.PCBUILDER_API_URL

private fun moeda(valor: Double): String {
    return NumberFormat.getCurrencyInstance(
        Locale.forLanguageTag("pt-BR")
    ).format(valor)
}

data class OfertaFinal(
    val loja: String,
    val preco: Double,
    val url: String?,
    val titulo: String?
)

data class CotacaoFinal(
    val id: Int,
    val status: String,
    val data: String?,
    val ofertas: List<OfertaFinal>
) {
    val melhor: OfertaFinal?
        get() = ofertas.filter { it.preco.isFinite() && it.preco > 0 }
            .minByOrNull { it.preco }

    val preco: Double?
        get() = if (status == "com_ofertas") {
            melhor?.preco
        } else {
            null
        }
}

interface FonteCotacoes {
    suspend fun buscarCotacoes(): Map<Int, CotacaoFinal>
}

private object ServicoFinal : FonteCotacoes {

    suspend fun requisicao(
        caminho: String
    ): JSONObject = withContext(Dispatchers.IO) {

        if (URL_API.isBlank()) {
            throw Exception("Servidor de preços não configurado.")
        }

        val endereco = URL(
            URL_API.trimEnd('/') + caminho
        )

        val conexao =
            endereco.openConnection() as HttpURLConnection

        try {

            conexao.requestMethod = "GET"
            conexao.connectTimeout = 30000
            conexao.readTimeout = 90000

            val codigo = conexao.responseCode

            val entrada = if (codigo in 200..299) {
                conexao.inputStream
            } else {
                conexao.errorStream
            }

            val texto = entrada
                ?.bufferedReader()
                ?.use { it.readText() }
                ?: ""

            val json = JSONObject(texto)

            if (codigo !in 200..299) {
                throw Exception(
                    json.optString(
                        "mensagem",
                        "Servidor respondeu $codigo"
                    )
                )
            }

            json

        } finally {
            conexao.disconnect()
        }
    }

    override suspend fun buscarCotacoes(): Map<Int, CotacaoFinal> {

        val json = requisicao("/api/precos")

        if (!json.optBoolean("sucesso")) {
            throw Exception("Falha na consulta de preços.")
        }

        val lista =
            json.optJSONArray("produtos") ?: JSONArray()

        val resultado = mutableMapOf<Int, CotacaoFinal>()

        for (i in 0 until lista.length()) {

            val produto = lista.getJSONObject(i)

            if (produto.isNull("catalogoId")) {
                continue
            }

            val id = produto.getInt("catalogoId")

            val ofertasJson =
                produto.optJSONArray("ofertas") ?: JSONArray()

            val ofertas = mutableListOf<OfertaFinal>()

            for (j in 0 until ofertasJson.length()) {

                val oferta = ofertasJson.getJSONObject(j)

                val preco =
                    oferta.optDouble("preco", Double.NaN)

                if (!preco.isFinite() || preco <= 0) {
                    continue
                }

                ofertas.add(
                    OfertaFinal(
                        loja = oferta.optString("loja"),
                        preco = preco,
                        url = oferta.optString("url")
                            .takeIf {
                                it.startsWith("https://")
                            },
                        titulo = oferta.optString("titulo")
                            .takeIf { it.isNotBlank() }
                    )
                )
            }

            resultado[id] = CotacaoFinal(
                id = id,
                status = produto.optString("status"),
                data = produto.optString("dataColeta")
                    .takeIf {
                        it.isNotBlank() && it != "null"
                    },
                ofertas = ofertas
            )
        }

        return resultado
    }

}

class ModeloPcBuilder(
    private val fonte: FonteCotacoes = ServicoFinal
) : ViewModel() {

    private val _cotacoes =
        MutableStateFlow<Map<Int, CotacaoFinal>>(emptyMap())

    private val _selecionados =
        MutableStateFlow<List<Int>>(emptyList())

    private val _carregando =
        MutableStateFlow(false)

    private val _status =
        MutableStateFlow("Consultando o servidor...")

    private val _recomendacao =
        MutableStateFlow<RecomendacaoMontagem?>(null)

    private val _gerando =
        MutableStateFlow(false)

    val selecionados = _selecionados.asStateFlow()
    val carregando = _carregando.asStateFlow()
    val status = _status.asStateFlow()
    private val _statusGeracao = MutableStateFlow("")
    val statusGeracao = _statusGeracao.asStateFlow()
    val recomendacao = _recomendacao.asStateFlow()
    val gerando = _gerando.asStateFlow()
    val cotacoes = _cotacoes.asStateFlow()

    init {
        consultar()
    }

    fun preco(id: Int): Double? {
        return _cotacoes.value[id]?.preco
    }

    fun adicionar(componente: Componente) {

        val idsDaCategoria =
            CatalogoComponentes.componentes
                .filter {
                    it.categoria == componente.categoria
                }
                .map { it.id }
                .toSet()

        _selecionados.value =
            _selecionados.value.filterNot {
                it in idsDaCategoria
            } + componente.id

        _recomendacao.value = null
    }

    fun remover(id: Int) {
        _selecionados.value =
            _selecionados.value.filterNot { it == id }

        _recomendacao.value = null
    }

    fun limpar() {
        _selecionados.value = emptyList()
        _recomendacao.value = null
    }

    fun aplicar(montagem: RecomendacaoMontagem) {
        _selecionados.value =
            montagem.componentes.map { it.id }
    }

    fun consultar() {

        if (_carregando.value) return

        viewModelScope.launch {

            _carregando.value = true

            try {

                _cotacoes.value =
                    fonte.buscarCotacoes()

                val quantidade =
                    _cotacoes.value.values.count {
                        it.preco != null
                    }

                _status.value =
                    if (quantidade == 0) {
                        "Nenhuma cotação disponível no momento."
                    } else {
                        "$quantidade de ${CatalogoComponentes.componentes.size} componentes com cotação disponível."
                    }

            } catch (erro: CancellationException) {
                throw erro

            } catch (erro: Exception) {

                _cotacoes.value = emptyMap()

                _status.value =
                    "Não foi possível consultar a API: " +
                    (erro.message ?: "erro desconhecido")
            } finally {
                _carregando.value = false
            }
        }
    }

    fun atualizar() {
        if (_carregando.value) return
        _statusGeracao.value = ""
        _recomendacao.value = null
        consultar()
    }

    fun gerar(orcamento: Double, finalidade: String) {

        if (_gerando.value || _carregando.value) {
            return
        }

        viewModelScope.launch {

            _gerando.value = true

            try {

                val componentesComPreco =
                    CatalogoComponentes.componentes.mapNotNull {
                        componente ->

                        val precoAtual =
                            preco(componente.id)

                        if (precoAtual != null) {
                            componente.copy(preco = precoAtual)
                        } else {
                            null
                        }
                    }

                val resultado = withContext(
                    Dispatchers.Default
                ) {
                    GeradorMontagem.gerar(
                        orcamento = orcamento,
                        finalidade = finalidade,
                        pecas = componentesComPreco
                    )
                }

                _recomendacao.value = resultado

                _statusGeracao.value = if (resultado == null) {
                    "Não há uma montagem completa compatível " +
                    "e com todos os preços disponíveis nesse orçamento."
                } else {
                    "Montagem gerada usando somente preços consultados."
                }

            } catch (erro: CancellationException) {
                throw erro

            } catch (erro: Exception) {

                _recomendacao.value = null

                _statusGeracao.value =
                    "Erro ao gerar configuração: " +
                    (erro.message ?: "erro desconhecido")

            } finally {
                _gerando.value = false
            }
        }
    }
}

@Serializable
sealed interface RotaFinal : NavKey {

    @Serializable
    data object Catalogo : RotaFinal

    @Serializable
    data object MinhaMontagem : RotaFinal

    @Serializable
    data object Orcamento : RotaFinal

    @Serializable
    data object Precos : RotaFinal
}

@Composable
fun AplicativoPcBuilder(modelo: ModeloPcBuilder) {

    val historico = remember {
        mutableStateListOf<RotaFinal>(RotaFinal.Catalogo)
    }

    val abas = listOf(
        RotaFinal.Catalogo to "Catálogo",
        RotaFinal.MinhaMontagem to "Minha Montagem",
        RotaFinal.Orcamento to "Orçamento",
        RotaFinal.Precos to "Preços"
    )

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {

            NavigationBar {

                abas.forEachIndexed { indice, aba ->

                    NavigationBarItem(
                        selected =
                            historico.lastOrNull() == aba.first,

                        onClick = {
                            if (historico.lastOrNull() != aba.first) {
                                historico.clear()
                                historico.add(RotaFinal.Catalogo)
                                if (aba.first != RotaFinal.Catalogo) historico.add(aba.first)
                            }
                        },

                        icon = {
                            Text(
                                listOf("C", "M", "O", "P")[indice]
                            )
                        },

                        label = {
                            Text(aba.second)
                        }
                    )
                }
            }
        }
    ) { espacamento ->

        NavDisplay(
            backStack = historico,

            modifier = Modifier
                .fillMaxSize()
                .padding(espacamento)
                .consumeWindowInsets(espacamento),

            onBack = {
                if (historico.size > 1) {
                    historico.removeAt(historico.lastIndex)
                }
            },

            entryProvider = entryProvider {

                entry<RotaFinal.Catalogo> {
                    TelaCatalogoPrincipal(modelo)
                }

                entry<RotaFinal.MinhaMontagem> {
                    TelaMontagemPrincipal(modelo)
                }

                entry<RotaFinal.Orcamento> {
                    TelaOrcamentoPrincipal(modelo)
                }

                entry<RotaFinal.Precos> {
                    TelaPrecosPrincipal(modelo)
                }
            }
        )
    }
}

@Composable
private fun TelaCatalogoPrincipal(modelo: ModeloPcBuilder) {

    val cotacoes by modelo.cotacoes.collectAsState()
    val carregando by modelo.carregando.collectAsState()
    val status by modelo.status.collectAsState()
    val selecionados by modelo.selecionados.collectAsState()

    var categoriaAberta by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val categorias = listOf(
        "Processador",
        "Placa-mãe",
        "Memória RAM",
        "Placa de vídeo",
        "SSD",
        "Fonte",
        "Gabinete",
        "Refrigeração"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        if (categoriaAberta == null) {

            Text(
                "Catálogo de componentes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                status,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { modelo.atualizar() },
                enabled = !carregando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (carregando) "Consultando..."
                    else "Consultar preços"
                )
            }

            Spacer(Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
                horizontalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                itensGrade(categorias) { categoria ->

                    OutlinedCard(
                        onClick = {
                            categoriaAberta = categoria
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                    ) {

                        Column(
                            modifier = Modifier.padding(14.dp)
                        ) {

                            Text(
                                categoria,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(12.dp))

                            val quantidade =
                                CatalogoComponentes.componentes.count {
                                    it.categoria == categoria
                                }

                            Text("$quantidade componentes")
                        }
                    }
                }
            }

        } else {

            val categoria = categoriaAberta!!

            TextButton(
                onClick = {
                    categoriaAberta = null
                }
            ) {
                Text("Voltar às categorias")
            }

            Text(
                categoria,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp)
            ) {

                items(
                    CatalogoComponentes.componentes.filter {
                        it.categoria == categoria
                    }
                ) { componente ->

                    val cotacao = cotacoes[componente.id]
                    val preco = cotacao?.preco

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                componente.nome,
                                style =
                                    MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                preco?.let { moeda(it) }
                                    ?: "Sem cotação disponível",
                                style =
                                    MaterialTheme.typography.titleMedium,
                                color = if (preco == null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )

                            if (cotacao?.data != null) {
                                Text(
                                    "Coleta: ${cotacao.data}",
                                    style =
                                        MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    modelo.adicionar(componente)
                                },
                                enabled = componente.id !in selecionados,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (componente.id in selecionados) {
                                        "Adicionado"
                                    } else {
                                        "Adicionar à montagem"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelaMontagemPrincipal(modelo: ModeloPcBuilder) {

    val selecionados by modelo.selecionados.collectAsState()
    val cotacoes by modelo.cotacoes.collectAsState()

    val componentes = selecionados.mapNotNull {
        CatalogoComponentes.buscarPorId(it)
    }

    val semCotacao = componentes.filter {
        cotacoes[it.id]?.preco == null
    }

    val total = if (semCotacao.isEmpty()) {
        componentes.sumOf { requireNotNull(cotacoes[it.id]?.preco) }
    } else {
        null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Minha Montagem",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        if (componentes.isEmpty()) {

            Text("Nenhum componente adicionado.")

        } else {

            val avisos =
                VerificadorCompatibilidade.verificar(componentes)

            avisos.forEach {
                Text(
                    "Compatibilidade: $it",
                    color = MaterialTheme.colorScheme.error
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(8.dp)
            ) {

                items(componentes) { componente ->

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {

                            Text(
                                componente.nome,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                cotacoes[componente.id]?.preco
                                    ?.let { moeda(it) }
                                    ?: "Sem cotação disponível"
                            )

                            TextButton(
                                onClick = {
                                    modelo.remover(componente.id)
                                }
                            ) {
                                Text("Remover")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            if (total != null) {

                Text(
                    "Total: ${moeda(total)}",
                    style = MaterialTheme.typography.titleLarge
                )

            } else {

                Text(
                    "Total indisponível: " +
                    "${semCotacao.size} componente(s) sem cotação.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { modelo.limpar() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Limpar montagem")
            }
        }
    }
}

@Composable
private fun TelaOrcamentoPrincipal(modelo: ModeloPcBuilder) {

    val recomendacao by modelo.recomendacao.collectAsState()
    val gerando by modelo.gerando.collectAsState()
    val carregando by modelo.carregando.collectAsState()
    val statusCotacoes by modelo.status.collectAsState()
    val statusGeracao by modelo.statusGeracao.collectAsState()
    val status = statusGeracao.ifBlank { statusCotacoes }

    val contexto = LocalContext.current

    var valor by rememberSaveable {
        mutableStateOf("5000")
    }

    var erroOrcamento by rememberSaveable { mutableStateOf<String?>(null) }

    var finalidade by rememberSaveable {
        mutableStateOf("Jogos")
    }

    val finalidades = listOf(
        "Jogos",
        "Trabalho e estudos",
        "Edição e criação",
        "Uso geral"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            "Montagem Inteligente",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "O gerador utiliza somente componentes com " +
            "cotações disponíveis na API."
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it; erroOrcamento = null },
            isError = erroOrcamento != null,
            supportingText = { erroOrcamento?.let { Text(it) } },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            ),
            label = { Text("Orçamento em R$") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Finalidade",
            style = MaterialTheme.typography.titleMedium
        )

        finalidades.forEach { opcao ->

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                RadioButton(
                    selected = finalidade == opcao,
                    onClick = {
                        finalidade = opcao
                    }
                )

                Text(opcao)
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {

                val orcamento = interpretarOrcamento(valor)

                if (
                    orcamento != null &&
                    orcamento.isFinite() &&
                    orcamento > 0
                ) {
                    erroOrcamento = null
                    modelo.gerar(orcamento, finalidade)
                } else {
                    erroOrcamento = "Informe um orçamento válido maior que zero."
                }
            },
            enabled = !gerando && !carregando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (gerando) "Gerando..."
                else "Gerar configuração"
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            status,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(16.dp))

        val montagem = recomendacao

        if (montagem != null) {

            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        "Configuração sugerida",
                        style =
                            MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(12.dp))

                    montagem.componentes.forEach { componente ->

                        Text(
                            componente.nome,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            moeda(componente.preco)
                        )

                        Spacer(Modifier.height(8.dp))
                    }

                    HorizontalDivider()

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Total: ${moeda(montagem.precoTotal)}",
                        style =
                            MaterialTheme.typography.titleLarge
                    )

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            modelo.aplicar(montagem)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aplicar à Minha Montagem")
                    }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {

                            val conteudo =
                                ExportadorMontagem.exportar(montagem)

                            val gerenciador =
                                contexto.getSystemService(
                                    Context.CLIPBOARD_SERVICE
                                ) as ClipboardManager

                            gerenciador.setPrimaryClip(
                                ClipData.newPlainText(
                                    "PCBuilder",
                                    conteudo
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copiar JSON da configuração")
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Compatibilidade preliminar. Confirme BIOS, " +
                        "dimensões, conexões e refrigeração antes da compra.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        } else if (!gerando) {

            Text(
                "Uma recomendação só poderá ser apresentada " +
                "quando houver preços para todas as peças escolhidas."
            )
        }
    }
}

@Composable
private fun TelaPrecosPrincipal(modelo: ModeloPcBuilder) {

    val cotacoes by modelo.cotacoes.collectAsState()
    val carregando by modelo.carregando.collectAsState()
    val status by modelo.status.collectAsState()

    val abridorEnderecos = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "Comparador de preços",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.height(8.dp))

        Text(status)

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                modelo.atualizar()
            },
            enabled = !carregando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (carregando) "Consultando..."
                else "Consultar preços"
            )
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            items(CatalogoComponentes.componentes) { componente ->

                val cotacao = cotacoes[componente.id]

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            componente.nome,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(6.dp))

                        val preco = cotacao?.preco

                        Text(
                            preco?.let { moeda(it) }
                                ?: "Sem cotação disponível",
                            color = if (preco == null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )

                        if (cotacao?.data != null) {
                            Text(
                                "Coleta: ${cotacao.data}",
                                style =
                                    MaterialTheme.typography.bodySmall
                            )
                        }

                        cotacao?.ofertas?.forEach { oferta ->

                            Spacer(Modifier.height(8.dp))

                            HorizontalDivider()

                            Text(oferta.loja)

                            Text(moeda(oferta.preco))

                            if (oferta.url != null) {

                                TextButton(
                                    onClick = {
                                        abridorEnderecos.openUri(
                                            oferta.url
                                        )
                                    }
                                ) {
                                    Text("Abrir página da oferta")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
