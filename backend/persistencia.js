const arquivos = require("fs");
const caminhos = require("path");

const ARQUIVO = caminhos.join(
    __dirname,
    "cache-precos.json"
);

const CHAVE = "pcbuilder:precos:v1";

const REDIS_URL = process.env.UPSTASH_REDIS_REST_URL;
const REDIS_TOKEN = process.env.UPSTASH_REDIS_REST_TOKEN;

function possuiRedis() {
    return Boolean(REDIS_URL && REDIS_TOKEN);
}

async function comandoRedis(comando) {

    const resposta = await fetch(REDIS_URL, {
        method: "POST",
        headers: {
            "Authorization": `Bearer ${REDIS_TOKEN}`,
            "Content-Type": "application/json"
        },
        body: JSON.stringify(comando),
        signal: AbortSignal.timeout(20000)
    });

    if (!resposta.ok) {
        throw new Error(
            `Redis respondeu ${resposta.status}`
        );
    }

    const dados = await resposta.json();

    if (dados.error) {
        throw new Error("Falha no armazenamento Redis.");
    }

    return dados.result;
}

function lerArquivoLocal() {

    try {
        const texto = arquivos.readFileSync(
            ARQUIVO,
            "utf-8"
        );

        return JSON.parse(texto);

    } catch {
        return {};
    }
}

async function carregarCache() {

    if (possuiRedis()) {

        const texto = await comandoRedis([
            "GET",
            CHAVE
        ]);

        if (texto) {
            console.log(
                "Preços carregados do Redis."
            );

            return JSON.parse(texto);
        }

        return {};
    }

    if (process.env.RENDER) {
        throw new Error(
            "Configure o Upstash Redis antes de publicar."
        );
    }

    console.log("Utilizando cache local.");

    return lerArquivoLocal();
}

async function salvarCache(cache) {

    if (possuiRedis()) {

        await comandoRedis([
            "SET",
            CHAVE,
            JSON.stringify(cache)
        ]);

        return;
    }

    if (process.env.RENDER) {
        throw new Error(
            "Redis não configurado."
        );
    }

    const temporario = ARQUIVO + ".tmp";

    arquivos.writeFileSync(
        temporario,
        JSON.stringify(cache, null, 2),
        "utf-8"
    );

    arquivos.renameSync(temporario, ARQUIVO);
}

module.exports = {
    carregarCache,
    salvarCache
};
