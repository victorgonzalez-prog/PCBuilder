require("dotenv").config();
const persistencia = require("./persistencia");
const { exigirAdministrador, tokenAdministrativoValido } = require("./seguranca");

const express = require("express");

const aplicativo = express();

aplicativo.disable("x-powered-by");
aplicativo.use(express.json({ limit: "16kb" }));

const PORTA = Number(process.env.PORT || 3001);
const TEMPO_CACHE = 7 * 24 * 60 * 60 * 1000;
const LIMITE_MENSAL = 190;

function componente(
    id,
    nome,
    categoria,
    grupos,
    excluir = [],
    familia = false
) {
    return {
        catalogoId: id,
        nome,
        categoria,
        grupos,
        excluir,
        familia
    };
}

const catalogo = [

    componente(
        1, "Ryzen 5 9600X", "Processador",
        [["ryzen 5"], ["9600x"]]
    ),

    componente(
        8, "Ryzen 5 7600", "Processador",
        [["ryzen 5"], ["7600"]],
        ["7600x"]
    ),

    componente(
        10, "Ryzen 5 5600G", "Processador",
        [["ryzen 5"], ["5600g"]]
    ),

    componente(
        11, "Ryzen 5 5600", "Processador",
        [["ryzen 5"], ["5600"]],
        ["5600x", "5600g", "5600gt"]
    ),

    componente(
        12, "Ryzen 7 5700X", "Processador",
        [["ryzen 7"], ["5700x"]],
        ["5700x3d"]
    ),

    componente(
        13, "Ryzen 7 7700", "Processador",
        [["ryzen 7"], ["7700"]],
        ["7700x"]
    ),

    componente(
        14, "Ryzen 7 9800X3D", "Processador",
        [["ryzen 7"], ["9800x3d"]]
    ),

    componente(
        4, "ASUS TUF Gaming B650M", "Placa-mãe",
        [["asus"], ["tuf"], ["b650m"]],
        [], true
    ),

    componente(
        9, "ASUS PRIME A620M-K", "Placa-mãe",
        [["asus"], ["prime"], ["a620m k"]]
    ),

    componente(
        15, "ASUS PRIME B550M-A", "Placa-mãe",
        [["asus"], ["prime"], ["b550m a"]],
        [], true
    ),

    componente(
        16, "MSI B550M PRO-VDH WIFI", "Placa-mãe",
        [["msi"], ["b550m"], ["pro vdh"], ["wifi"]]
    ),

    componente(
        17, "ASUS TUF Gaming B850M-E", "Placa-mãe",
        [["asus"], ["tuf"], ["b850m e"]]
    ),

    componente(
        3, "Kingston Fury Beast 32 GB DDR5",
        "Memória RAM",
        [
            ["kingston"], ["fury"], ["beast"],
            ["32gb", "32 gb", "2x16gb", "2 x 16gb"],
            ["ddr5"]
        ],
        [], true
    ),

    componente(
        18, "Kingston Fury Beast 16 GB DDR4",
        "Memória RAM",
        [
            ["kingston"], ["fury"], ["beast"],
            ["16gb", "16 gb", "2x8gb", "2 x 8gb"],
            ["ddr4"]
        ],
        [], true
    ),

    componente(
        19, "Kingston Fury Beast 32 GB DDR4",
        "Memória RAM",
        [
            ["kingston"], ["fury"], ["beast"],
            ["32gb", "32 gb", "2x16gb", "2 x 16gb"],
            ["ddr4"]
        ],
        [], true
    ),

    componente(
        20, "Kingston Fury Beast 16 GB DDR5",
        "Memória RAM",
        [
            ["kingston"], ["fury"], ["beast"],
            ["16gb", "16 gb", "2x8gb", "2 x 8gb"],
            ["ddr5"]
        ],
        [], true
    ),

    componente(
        21, "Kingston Fury Beast 64 GB DDR5",
        "Memória RAM",
        [
            ["kingston"], ["fury"], ["beast"],
            ["64gb", "64 gb", "2x32gb", "2 x 32gb"],
            ["ddr5"]
        ],
        [], true
    ),

    componente(
        2, "Radeon RX 9070 XT", "Placa de vídeo",
        [
            ["rx 9070 xt", "rx9070xt", "rx 9070xt"],
            ["16gb", "16 gb"]
        ]
    ),

    componente(
        7, "Radeon RX 7600 8 GB", "Placa de vídeo",
        [
            ["rx 7600", "rx7600"],
            ["8gb", "8 gb"]
        ],
        ["rx 7600 xt", "rx7600xt", "rx 7600xt"]
    ),

    componente(
        22, "Radeon RX 6600 8 GB", "Placa de vídeo",
        [
            ["rx 6600", "rx6600"],
            ["8gb", "8 gb"]
        ],
        ["rx 6600 xt", "rx6600xt", "rx 6600xt"]
    ),

    componente(
        23, "GeForce RTX 4060 8 GB", "Placa de vídeo",
        [
            ["rtx 4060", "rtx4060"],
            ["8gb", "8 gb"]
        ],
        ["rtx 4060 ti", "rtx4060ti", "rtx 4060ti"]
    ),

    componente(
        24, "Radeon RX 9060 XT 16 GB",
        "Placa de vídeo",
        [
            ["rx 9060 xt", "rx9060xt", "rx 9060xt"],
            ["16gb", "16 gb"]
        ]
    ),

    componente(
        25, "GeForce RTX 4070 12 GB",
        "Placa de vídeo",
        [
            ["rtx 4070", "rtx4070"],
            ["12gb", "12 gb"]
        ],
        [
            "rtx 4070 ti", "rtx4070ti",
            "rtx 4070 super", "rtx4070super"
        ]
    ),

    componente(
        5, "Kingston NV3 1 TB", "SSD",
        [
            ["kingston"], ["nv3"],
            ["1tb", "1 tb", "1000gb", "1000g"]
        ],
        ["2tb", "2 tb", "500gb", "500 gb"]
    ),

    componente(
        26, "Kingston NV3 500 GB", "SSD",
        [
            ["kingston"], ["nv3"],
            ["500gb", "500 gb"]
        ]
    ),

    componente(
        27, "Kingston NV3 2 TB", "SSD",
        [
            ["kingston"], ["nv3"],
            ["2tb", "2 tb", "2000gb"]
        ]
    ),

    componente(
        28, "WD Blue SN580 1 TB", "SSD",
        [
            ["wd", "western digital"],
            ["blue"], ["sn580"],
            ["1tb", "1 tb", "1000gb"]
        ]
    ),

    componente(
        6, "Corsair RM750e", "Fonte",
        [["corsair"], ["rm750e"]]
    ),

    componente(
        29, "Corsair CX550", "Fonte",
        [["corsair"], ["cx550"]]
    ),

    componente(
        30, "MSI MAG A650BN", "Fonte",
        [["msi"], ["mag"], ["a650bn"]]
    ),

    componente(
        31, "Corsair RM850e", "Fonte",
        [["corsair"], ["rm850e"]]
    ),

    componente(
        32, "Montech Air 100 Lite", "Gabinete",
        [["montech"], ["air 100"], ["lite"]]
    ),

    componente(
        33, "DeepCool CC560", "Gabinete",
        [["deepcool"], ["cc560"]],
        [], true
    ),

    componente(
        34, "Corsair 4000D Airflow", "Gabinete",
        [["corsair"], ["4000d"], ["airflow"]]
    ),

    componente(
        35, "DeepCool AG400", "Refrigeração",
        [["deepcool"], ["ag400"]],
        [], true
    ),

    componente(
        36, "DeepCool AK400", "Refrigeração",
        [["deepcool"], ["ak400"]],
        [], true
    ),

    componente(
        37, "Thermalright Peerless Assassin 120 SE",
        "Refrigeração",
        [
            ["thermalright"], ["peerless"],
            ["assassin"], ["120"], ["se"]
        ],
        [], true
    )
];

const BUSCAS_ALTERNATIVAS = {
    4: "ASUS TUF B650M placa mae",
    28: "SSD WD Blue SN580 1TB",
    32: "Gabinete Montech Air 100 Lite",
    34: "Gabinete Corsair 4000D Airflow",
    35: "Cooler DeepCool AG400"
};

function normalizar(texto) {

    return String(texto || "")
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, " ")
        .trim();
}

function contem(texto, termo) {

    return (
        " " + normalizar(texto) + " "
    ).includes(
        " " + normalizar(termo) + " "
    );
}

function validarTitulo(produto, titulo) {

    const texto = normalizar(titulo);

    if (!texto) {
        return false;
    }

    const termosProibidos = [
        "notebook",
        "laptop",
        "pc gamer",
        "pc completo",
        "computador completo",
        "desktop gamer",
        "kit upgrade",
        "atualizacao de bios",
        "bios atualizada",
        "bios",
        "io shield",
        "i o shield",
        "espelho traseiro",
        "espelho da placa",
        "tampa do",
        "servico",
        "reparo",
        "somente caixa",
        "usado",
        "seminovo",
        "recondicionado",
        "refurbished",
        "open box",
        "defeito"
    ];

    if (termosProibidos.some(termo =>
        contem(texto, termo)
    )) {
        return false;
    }

    if (produto.excluir.some(termo =>
        contem(texto, termo)
    )) {
        return false;
    }

    const anuncioDeKit = ["kit", "combo", "bundle"].some(termo =>
        contem(texto, termo)
    );
    const incluiOutraPeca = /\+\s*(?:water\b|cooler\b|placa\b|motherboard\b|mainboard\b|mem[oó]ria\b|ram\b|ssd\b|fonte\b|processador\b|ryzen\b|intel\b|radeon\b|geforce\b)/i.test(String(titulo));
    const processadorComPlaca = produto.categoria === "Processador" &&
        ["placa mae", "motherboard", "mainboard"].some(termo => contem(texto, termo));

    if ((anuncioDeKit && produto.categoria !== "Memória RAM") ||
        incluiOutraPeca || processadorComPlaca) {
        return false;
    }

    const gruposValidos = produto.grupos.every(
        grupo => grupo.some(termo =>
            contem(texto, termo)
        )
    );

    if (!gruposValidos) {
        return false;
    }

    if (produto.categoria === "Processador") {

        const modelo = produto.nome.match(
            /ryzen\s+(\d)\s+([a-z0-9]+)/i
        );

        if (!modelo) {
            return false;
        }

        const nomeExato =
            `ryzen ${modelo[1]} ${modelo[2]}`;

        if (!contem(texto, nomeExato)) {
            return false;
        }
    }

    if (produto.categoria === "Placa-mãe") {

        const identificadores = [
            "placa mae",
            "motherboard",
            "mainboard"
        ];

        if (!identificadores.some(termo =>
            contem(texto, termo)
        )) {
            return false;
        }
    }

    if (produto.catalogoId === 25) {

        if (
            contem(texto, "super") ||
            contem(texto, "ti")
        ) {
            return false;
        }
    }

    if (produto.catalogoId === 34) {

        if (
            contem(texto, "frame") ||
            contem(texto, "vault")
        ) {
            return false;
        }
    }

    if (produto.catalogoId === 15) {

        if (
            contem(texto, "wifi") ||
            contem(texto, "wi fi") ||
            contem(texto, "b550m a ii")
        ) {
            return false;
        }
    }

    if (
        produto.catalogoId === 33 &&
        contem(texto, "v2")
    ) {
        return false;
    }

    if (
        produto.catalogoId === 37 &&
        contem(texto, "v3")
    ) {
        return false;
    }

    return true;
}

function validarFaixaDePreco(produto, preco) {

    if (
        !Number.isFinite(preco) ||
        preco <= 0
    ) {
        return false;
    }

    const limites = {
        "Processador": [200, 10000],
        "Placa-mãe": [250, 8000],
        "Memória RAM": [100, 10000],
        "Placa de vídeo": [500, 15000],
        "SSD": [100, 6000],
        "Fonte": [150, 4000],
        "Gabinete": [100, 3500],
        "Refrigeração": [50, 2500]
    };

    const faixa = limites[produto.categoria];

    if (!faixa) {
        return false;
    }

    return (
        preco >= faixa[0] &&
        preco <= faixa[1]
    );
}

let cache = {};

async function carregarCachePersistido() {
    cache = await persistencia.carregarCache();
}

async function salvarCache() {
    await persistencia.salvarCache(cache);
}
function precisaAtualizar(produto) {

    const registro =
        cache[produto.catalogoId];

    if (
        !registro ||
        !registro.checadoEm
    ) {
        return true;
    }

    const idade =
        Date.now() -
        new Date(registro.checadoEm).getTime();

    return (
        !Number.isFinite(idade) ||
        idade < 0 ||
        idade >= TEMPO_CACHE
    );
}

function formatarData(iso) {

    if (!iso) {
        return null;
    }

    const data = new Date(iso);

    if (!Number.isFinite(data.getTime())) {
        return null;
    }

    return data.toLocaleString(
        "pt-BR",
        {
            timeZone: "America/Sao_Paulo"
        }
    );
}

function obterChave() {

    const chave = process.env.SERPAPI_KEY;

    if (
        !chave ||
        chave.includes("COLOQUE") ||
        chave.includes("SUA_CHAVE")
    ) {
        throw new Error(
            "Configure SERPAPI_KEY no arquivo .env."
        );
    }

    return chave;
}

async function consultarJson(url) {

    const resposta = await fetch(
        url,
        {
            signal: AbortSignal.timeout(35000)
        }
    );

    if (!resposta.ok) {
        throw new Error(
            "Erro HTTP " + resposta.status
        );
    }

    const dados = await resposta.json();

    if (dados.error) {
        const mensagem = String(dados.error);
        if (/hasn.t returned any results|no results for this query/i.test(mensagem)) {
            throw new Error("no results for this query");
        }
        if (/quota|credit|limit|cota/i.test(mensagem)) {
            throw new Error("Limite mensal de pesquisas atingido.");
        }
        throw new Error("Falha no provedor de preços.");
    }

    return dados;
}

async function verificarCota() {

    const url = new URL(
        "https://serpapi.com/account.json"
    );

    url.searchParams.set(
        "api_key",
        obterChave()
    );

    const conta = await consultarJson(url);

    if (
        Number(conta.this_month_usage) >=
        LIMITE_MENSAL
    ) {
        throw new Error(
            "Limite mensal de pesquisas atingido."
        );
    }

    if (
        conta.total_searches_left != null &&
        Number(conta.total_searches_left) <= 10
    ) {
        throw new Error(
            "Poucos créditos restantes."
        );
    }
}

function extrairOferta(produto, resultado) {

    if (!validarTitulo(
        produto,
        resultado.title
    )) {
        return null;
    }

    if (resultado.second_hand_condition) {
        return null;
    }

    const preco = Number(
        resultado.extracted_price
    );

    if (!validarFaixaDePreco(
        produto,
        preco
    )) {
        return null;
    }

    const precoTexto = String(
        resultado.price || ""
    );

    if (!/(R\$|BRL)/i.test(precoTexto)) {
        return null;
    }

    if (
        /mensal|parcela|\/\s*m[eê]s/i
            .test(precoTexto)
    ) {
        return null;
    }

    const endereco =
        resultado.link ||
        resultado.product_link;

    if (
        !endereco ||
        !resultado.source
    ) {
        return null;
    }

    try {

        const url = new URL(endereco);

        if (url.protocol !== "https:") {
            return null;
        }

    } catch {
        return null;
    }

    return {
        loja: resultado.source,
        preco: preco,
        url: endereco,
        titulo: resultado.title
    };
}

async function pesquisarProduto(produto, forcar = false) {

    await verificarCota();

    const termoPesquisa =
        BUSCAS_ALTERNATIVAS[produto.catalogoId] ||
        produto.nome;

    console.log(
        `Pesquisando ${produto.catalogoId}: ${termoPesquisa}`
    );

    const url = new URL(
        "https://serpapi.com/search.json"
    );

    url.searchParams.set(
        "api_key",
        obterChave()
    );

    url.searchParams.set(
        "engine",
        "google_shopping"
    );

    url.searchParams.set(
        "q",
        termoPesquisa
    );

    url.searchParams.set("gl", "br");
    url.searchParams.set("hl", "pt");
    if (forcar) url.searchParams.set("no_cache", "true");

    let dados;

    try {

        dados = await consultarJson(url);

    } catch (erro) {

        if (
            /hasn.t returned any results|no results for this query/i
                .test(erro.message)
        ) {

            console.log(
                `Sem resultados: ${produto.nome}`
            );

            return [];
        }

        throw erro;
    }

    const encontrados = Array.isArray(
        dados.shopping_results
    )
        ? dados.shopping_results
        : [];

    const ofertas = encontrados
        .map(resultado =>
            extrairOferta(produto, resultado)
        )
        .filter(Boolean)
        .sort((a, b) => a.preco - b.preco);

    if (ofertas.length === 0) {

        console.log(
            `${produto.nome}: ${encontrados.length} resultados brutos`
        );

        encontrados.slice(0, 5).forEach(
            resultado => {

                console.log(
                    "Título:",
                    resultado.title
                );

                console.log(
                    "Preço:",
                    resultado.price
                );

                console.log(
                    "Loja:",
                    resultado.source
                );

                console.log("-----------------------");
            }
        );
    }

    console.log(
        `Ofertas aceitas: ${ofertas.length}`
    );

    return ofertas.slice(0, 10);
}

let executando = false;

let progresso = {
    total: 0,
    concluidos: 0,
    comOfertas: 0,
    erros: 0
};

function iniciarAtualizacao(produtos, forcar = false) {

    if (executando) {

        return {
            iniciada: false,
            mensagem:
                "Uma atualização já está em andamento."
        };
    }

    const pendentes = produtos.filter(
        produto => forcar || precisaAtualizar(produto)
    );

    if (pendentes.length === 0) {

        return {
            iniciada: false,
            mensagem:
                "As consultas ainda estão no prazo do cache."
        };
    }

    executando = true;

    progresso = {
        total: pendentes.length,
        concluidos: 0,
        comOfertas: 0,
        erros: 0
    };

    (async () => {

        try {

            for (const produto of pendentes) {

                try {

                    const ofertas =
                        await pesquisarProduto(produto, forcar);

                    const agora =
                        new Date().toISOString();

                    cache[produto.catalogoId] = {
                        checadoEm: agora,

                        coletadoEm:
                            ofertas.length > 0
                                ? agora
                                : null,

                        ofertas: ofertas,
                        erro: null
                    };

                    if (ofertas.length > 0) {
                        progresso.comOfertas++;
                    }

                } catch (erro) {

                    progresso.erros++;

                    console.error(
                        "Falha ao pesquisar componente",
                        produto.catalogoId
                    );

                    const anterior =
                        cache[produto.catalogoId] || {};

                    cache[produto.catalogoId] = {
                        ...anterior,

                        checadoEm:
                            new Date().toISOString(),

                        erro: "Falha na pesquisa de preços.",

                        ofertas:
                            anterior.ofertas || []
                    };

                    if (
                        /cota|limite mensal|créditos|credit|429|401|403/i
                            .test(erro.message)
                    ) {

                        progresso.concluidos++;
                        await salvarCache();

                        break;
                    }
                }

                progresso.concluidos++;

                await salvarCache();
            }

        } finally {
            executando = false;
        }

    })().catch(erro => {

        executando = false;

        console.error(
            "Não foi possível concluir a atualização de preços."
        );
    });

    return {
        iniciada: true,
        quantidade: pendentes.length,
        forcada: forcar
    };
}

function montarProduto(produto) {

    const registro =
        cache[produto.catalogoId];

    const expirado =
        precisaAtualizar(produto);

    const ofertasOriginais =
        Array.isArray(registro?.ofertas)
            ? registro.ofertas
            : [];

    const ofertasValidas = ofertasOriginais.filter(
        oferta => {

            const tituloCorreto =
                validarTitulo(
                    produto,
                    oferta.titulo
                );

            const precoAceitavel =
                validarFaixaDePreco(
                    produto,
                    Number(oferta.preco)
                );

            const enderecoValido =
                typeof oferta.url === "string" &&
                oferta.url.startsWith("https://");

            return (
                tituloCorreto &&
                precoAceitavel &&
                enderecoValido
            );
        }
    );

    ofertasValidas.sort(
        (a, b) => a.preco - b.preco
    );

    const ofertas =
        !expirado && !registro?.erro
            ? ofertasValidas
            : [];

    const rejeitadas =
        ofertasOriginais.length -
        ofertasValidas.length;

    let status;

    if (!registro) {

        status = "nao_consultado";

    } else if (registro.erro) {

        status = "erro";

    } else if (expirado) {

        status = "desatualizado";

    } else if (ofertas.length > 0) {

        status = "com_ofertas";

    } else if (rejeitadas > 0) {

        status = "em_revisao";

    } else {

        status = "sem_ofertas";
    }

    return {
        catalogoId: produto.catalogoId,
        produto: produto.nome,
        categoria: produto.categoria,
        familia: produto.familia,

        status: status,

        dataColeta: ofertas.length > 0
            ? formatarData(registro?.coletadoEm)
            : null,

        melhorOferta: ofertas[0] || null,

        ofertas: ofertas,

        ofertasRejeitadas: rejeitadas,

        erro: registro?.erro ? "Falha na pesquisa de preços." : null
    };
}

aplicativo.get("/", (requisicao, resposta) => {
    const quantidadeCotada = catalogo.map(montarProduto).filter(
        produto => produto.status === "com_ofertas"
    ).length;

    resposta.set("Cache-Control", "no-store");
    resposta.type("html").send(`<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>PCBuilder — API de Preços</title>
    <style>
        * { box-sizing: border-box; }
        body { margin: 0; background: #f1f5f9; color: #0f172a; font-family: system-ui, sans-serif; line-height: 1.6; }
        main { max-width: 760px; margin: 8vh auto; padding: 32px; background: #fff; border: 1px solid #e2e8f0; border-radius: 20px; }
        h1 { margin: 16px 0 8px; font-size: clamp(1.7rem, 5vw, 2.5rem); line-height: 1.2; }
        p { color: #475569; }
        .status { display: inline-block; padding: 4px 12px; border-radius: 24px; background: #dcfce7; color: #166534; font-weight: 600; }
        nav { display: grid; grid-template-columns: repeat(auto-fit, minmax(220px, 1fr)); gap: 12px; margin: 24px 0; }
        a { color: #1d4ed8; }
        nav a { padding: 14px 18px; border: 1px solid #cbd5e1; border-radius: 12px; text-decoration: none; font-weight: 600; }
        nav a:hover, nav a:focus-visible { background: #eff6ff; border-color: #1d4ed8; }
        footer { margin-top: 24px; padding-top: 16px; border-top: 1px solid #e2e8f0; }
        @media (max-width: 600px) { main { margin: 16px; padding: 24px; } }
    </style>
</head>
<body>
    <main>
        <span class="status">Serviço online</span>
        <h1>PCBuilder — API de Preços</h1>
        <p>Serviço de cotações de componentes de computador utilizado pelo aplicativo PCBuilder.</p>
        <p><strong>${catalogo.length} componentes</strong> no catálogo · <strong>${quantidadeCotada} com cotação disponível</strong>.</p>
        <nav aria-label="Consultas públicas">
            <a href="/api/health">Status do serviço</a>
            <a href="/api/catalogo">Catálogo de componentes</a>
            <a href="/api/precos">Consultar cotações</a>
            <a href="/api/status">Andamento das pesquisas</a>
        </nav>
        <p>As consultas acima retornam dados em JSON. As cotações refletem o momento da coleta; componentes sem oferta válida permanecem sem preço disponível.</p>
        <footer><a href="https://github.com/victorgonzalez-prog/PCBuilder/releases">Baixar o aplicativo Android</a></footer>
    </main>
</body>
</html>`);
});

aplicativo.get("/api/health", (requisicao, resposta) => {

    resposta.json({
        sucesso: true,
        aplicativo: "PCBuilder",
        status: "online",
        atualizacaoForcada: true
    });
});

aplicativo.get("/api/catalogo", (requisicao, resposta) => {

    resposta.json({
        sucesso: true,
        quantidade: catalogo.length,

        componentes: catalogo.map(item => ({
            catalogoId: item.catalogoId,
            nome: item.nome,
            categoria: item.categoria
        }))
    });
});

aplicativo.get("/api/precos", (requisicao, resposta) => {

    resposta.json({
        sucesso: true,

        consultadoEm:
            new Date().toISOString(),

        quantidade: catalogo.length,

        atualizando: executando,

        produtos: catalogo.map(
            montarProduto
        )
    });
});

aplicativo.get("/api/status", (requisicao, resposta) => {

    resposta.json({
        sucesso: true,
        executando: executando,
        ...progresso
    });
});

aplicativo.post("/api/atualizar", exigirAdministrador, (requisicao, resposta) => {

    if (!process.env.SERPAPI_KEY) {

        return resposta.status(503).json({
            sucesso: false,
            mensagem: "Configure SERPAPI_KEY."
        });
    }

    const resultado =
        iniciarAtualizacao(catalogo, requisicao.body?.forcar === true);

    resposta.status(
        resultado.iniciada ? 202 : 200
    ).json({
        sucesso: true,
        ...resultado
    });
});

aplicativo.post("/api/atualizar/:id", exigirAdministrador, (requisicao, resposta) => {

    const id = Number(requisicao.params.id);

    const produto = catalogo.find(
        item => item.catalogoId === id
    );

    if (!produto) {

        return resposta.status(404).json({
            sucesso: false,
            mensagem: "Componente não encontrado."
        });
    }

    if (!process.env.SERPAPI_KEY) {

        return resposta.status(503).json({
            sucesso: false,
            mensagem: "Configure SERPAPI_KEY."
        });
    }

    const resultado =
        iniciarAtualizacao([produto], requisicao.body?.forcar === true);

    resposta.status(
        resultado.iniciada ? 202 : 200
    ).json({
        sucesso: true,
        ...resultado
    });
});

aplicativo.use((erro, requisicao, resposta, proximo) => {
    resposta.status(erro.type === "entity.too.large" ? 413 : 400).json({
        sucesso: false,
        mensagem: "Requisição inválida."
    });
});

module.exports = { catalogo, validarTitulo };

if (require.main === module) {
    if (process.env.RENDER && !tokenAdministrativoValido()) {
        console.error("Configure uma senha administrativa própria com pelo menos 32 caracteres.");
        process.exit(1);
    }

    carregarCachePersistido()
        .then(() => {
            const servidor = aplicativo.listen(PORTA, "0.0.0.0", () => {
                console.log(
                    `API PCBuilder: http://localhost:${servidor.address().port}`
                );
                console.log(
                    `Componentes cadastrados: ${catalogo.length}`
                );
                console.log(
                    "Cache persistente inicializado."
                );
            });
        })
        .catch(erro => {
            console.error(
                "Não foi possível iniciar o armazenamento. Verifique a configuração do servidor."
            );
            process.exit(1);
        });
}
