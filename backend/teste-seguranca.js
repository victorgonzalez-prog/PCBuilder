const { test: teste } = require("node:test");
const verificar = require("node:assert/strict");
const { spawn } = require("node:child_process");
const { randomBytes } = require("node:crypto");
const { catalogo, validarTitulo } = require("./servidor");

teste("kits com placa-mãe ou refrigeração não são preços de processador avulso", () => {
    const processador = catalogo.find(produto => produto.catalogoId === 1);
    for (const titulo of [
        "Kit Placa Mãe ASUS TUF GAMING B650EM-E WIFI AM5 + Processador AMD Ryzen 5 9600X",
        "Micro Center AMD Ryzen 5 9600X CPU Processor with MSI PRO B850-P WiFi Motherboard ATX",
        "Processador AMD Ryzen 5 9600X, AM5 + Water Co",
        "Combo AMD Ryzen 5 9600X"
    ]) {
        verificar.equal(validarTitulo(processador, titulo), false, titulo);
    }
});

teste("processadores avulsos e kits da capacidade correta de memória continuam aceitos", () => {
    const processador = catalogo.find(produto => produto.catalogoId === 1);
    verificar.equal(validarTitulo(processador, "Processador AMD Ryzen 5 9600X, 6-Cores, AM5, Sem Cooler"), true);
    verificar.equal(validarTitulo(processador, "Processador AMD Ryzen 5 9600X"), true);
    verificar.equal(validarTitulo(processador, "Processador AMD Ryzen 5 9600X3D"), false);
    const memoria = catalogo.find(produto => produto.catalogoId === 3);
    verificar.equal(validarTitulo(memoria, "Kit Memória Kingston Fury Beast 32GB (2x16GB) DDR5"), true);
    verificar.equal(validarTitulo(memoria, "Kit Memória Kingston Fury Beast 16GB DDR4"), false);
});

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

teste("página inicial e rotas de leitura são públicas e retornam 37 componentes", async contextoTeste => {
    const url = await iniciar(contextoTeste);
    const inicio = await fetch(url + "/");
    verificar.equal(inicio.status, 200);
    verificar.match(inicio.headers.get("content-type"), /text\/html.*charset=utf-8/i);
    const pagina = await inicio.text();
    verificar.match(pagina, /PCBuilder — API de Preços/);
    verificar.match(pagina, /37 componentes/);
    verificar.equal(pagina.includes("Cannot GET"), false);
    for (const rota of ["/api/health", "/api/catalogo", "/api/precos", "/api/status"]) {
        verificar.ok(pagina.includes('href="' + rota + '"'));
        verificar.equal((await fetch(url + rota)).status, 200);
    }
    const status = await fetch(url + "/api/status").then(resposta => resposta.json());
    verificar.equal(status.executando, false);
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
