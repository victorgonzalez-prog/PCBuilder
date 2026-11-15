const { test: teste } = require("node:test");
const verificar = require("node:assert/strict");
const { spawn } = require("node:child_process");
const { randomBytes } = require("node:crypto");

async function iniciar(contextoTeste, token = "") {
    const filho = spawn(process.execPath, ["servidor.js"], {
        cwd: __dirname,
        env: {
            ...process.env, PORT: "0", ADMIN_API_TOKEN: token,
            SERPAPI_KEY: "", UPSTASH_REDIS_REST_URL: "",
            UPSTASH_REDIS_REST_TOKEN: "", RENDER: ""
        },
        stdio: ["ignore", "pipe", "pipe"]
    });
    contextoTeste.after(() => filho.kill());
    const porta = await new Promise((resolver, rejeitar) => {
        const limite = setTimeout(() => rejeitar(new Error("Servidor de teste não iniciou.")), 15000);
        let saida = "";
        filho.stdout.on("data", trecho => {
            saida += trecho.toString();
            const numero = saida.match(/API PCBuilder: http:\/\/localhost:(\d+)/);
            if (numero) { clearTimeout(limite); resolver(numero[1]); }
        });
        filho.once("error", () => { clearTimeout(limite); rejeitar(new Error("Falha ao iniciar servidor de teste.")); });
        filho.once("exit", () => { clearTimeout(limite); rejeitar(new Error("Servidor de teste encerrado.")); });
    });
    return "http://127.0.0.1:" + porta;
}

teste("rotas de leitura continuam públicas e retornam 37 componentes", async contextoTeste => {
    const url = await iniciar(contextoTeste);
    const saude = await fetch(url + "/api/health").then(resposta => resposta.json());
    verificar.equal(saude.sucesso, true);
    const catalogo = await fetch(url + "/api/catalogo").then(resposta => resposta.json());
    verificar.equal(catalogo.quantidade, 37);
    verificar.equal((await fetch(url + "/api/precos")).status, 200);
});

teste("sem configuração administrativa, as duas rotas de pesquisa ficam fechadas", async contextoTeste => {
    const url = await iniciar(contextoTeste);
    for (const rota of ["/api/atualizar", "/api/atualizar/1"]) {
        verificar.equal((await fetch(url + rota, { method: "POST" })).status, 503);
    }
});

teste("requisições sem senha ou com senha incorreta não iniciam pesquisas", async contextoTeste => {
    const token = randomBytes(32).toString("hex");
    const url = await iniciar(contextoTeste, token);
    for (const rota of ["/api/atualizar", "/api/atualizar/1", "/api/atualizar/999"]) {
        for (const autorizacao of ["", "Bearer " + randomBytes(32).toString("hex"), token]) {
            const resposta = await fetch(url + rota, { method: "POST", headers: { authorization: autorizacao } });
            verificar.equal(resposta.status, 401);
            verificar.equal(resposta.headers.get("cache-control"), "no-store");
            const texto = await resposta.text();
            verificar.equal(texto.includes(token), false);
        }
    }
    const status = await fetch(url + "/api/status").then(resposta => resposta.json());
    verificar.equal(status.executando, false);
});

teste("senha válida alcança as rotas; testes nunca têm chave SerpApi", async contextoTeste => {
    const token = randomBytes(32).toString("hex");
    const url = await iniciar(contextoTeste, token);
    const cabecalhos = { authorization: "Bearer " + token };
    verificar.equal((await fetch(url + "/api/atualizar/999", { method: "POST", headers: cabecalhos })).status, 404);
    for (const rota of ["/api/atualizar", "/api/atualizar/1"]) {
        verificar.equal((await fetch(url + rota, { method: "POST", headers: cabecalhos })).status, 503);
    }
});

teste("corpo JSON inválido não expõe seu conteúdo na resposta", async contextoTeste => {
    const url = await iniciar(contextoTeste, randomBytes(32).toString("hex"));
    const marcador = randomBytes(32).toString("hex");
    const resposta = await fetch(url + "/api/atualizar", {
        method: "POST", headers: { "content-type": "application/json" },
        body: '{"incompleto":"' + marcador
    });
    verificar.equal(resposta.status, 400);
    verificar.equal((await resposta.text()).includes(marcador), false);
});
