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

async function iniciar(contextoTeste, token = "", simularPesquisa = false) {
    const argumentos = ["servidor.js"];
    if (simularPesquisa) {
        const preparacao = `
            import { createRequire } from "node:module";
            const exigir = createRequire(${JSON.stringify(__filename)});
            const persistencia = exigir("./persistencia");
            persistencia.carregarCache = async () => Object.fromEntries(
                Array.from({ length: 37 }, (_, indice) => [indice + 1, {
                    checadoEm: new Date().toISOString(), coletadoEm: null, ofertas: [], erro: null
                }])
            );
            persistencia.salvarCache = async () => {};
            globalThis.fetch = async endereco => {
                const url = new URL(endereco);
                if (url.hostname !== "serpapi.com") throw new Error("Rede externa bloqueada no teste.");
                if (url.pathname === "/account.json") {
                    return Response.json({ this_month_usage: 0, total_searches_left: 100 });
                }
                if (url.pathname === "/search.json" && url.searchParams.get("no_cache") === "true") {
                    return Response.json({ shopping_results: [] });
                }
                throw new Error("A nova coleta não ignorou o cache do provedor.");
            };
        `;
        argumentos.unshift("--import", "data:text/javascript," + encodeURIComponent(preparacao));
    }
    const filho = spawn(process.execPath, argumentos, {
        cwd: __dirname,
        env: {
            ...process.env, PORT: "0", ADMIN_API_TOKEN: token,
            SERPAPI_KEY: simularPesquisa ? "chave-invalida-exclusiva-do-teste" : "", UPSTASH_REDIS_REST_URL: "",
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
            const resposta = await fetch(url + rota, {
                method: "POST", headers: { authorization: autorizacao, "content-type": "application/json" },
                body: JSON.stringify({ forcar: true })
            });
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

teste("coleta forçada ignora cache recente e exige booleano verdadeiro", async contextoTeste => {
    const token = randomBytes(32).toString("hex");
    const url = await iniciar(contextoTeste, token, true);
    const cabecalhos = { authorization: "Bearer " + token, "content-type": "application/json" };
    for (const corpo of [{}, { forcar: "true" }]) {
        const resposta = await fetch(url + "/api/atualizar", {
            method: "POST", headers: cabecalhos, body: JSON.stringify(corpo)
        });
        verificar.equal(resposta.status, 200);
        verificar.equal((await resposta.json()).iniciada, false);
    }
    const resposta = await fetch(url + "/api/atualizar/1", {
        method: "POST", headers: cabecalhos, body: JSON.stringify({ forcar: true })
    });
    verificar.equal(resposta.status, 202);
    const inicio = await resposta.json();
    verificar.equal(inicio.forcada, true);
    verificar.equal(inicio.quantidade, 1);
    let status;
    for (let tentativa = 0; tentativa < 50; tentativa++) {
        status = await fetch(url + "/api/status").then(resposta => resposta.json());
        if (!status.executando) break;
        await new Promise(resolver => setTimeout(resolver, 20));
    }
    verificar.equal(status.executando, false);
    verificar.equal(status.concluidos, 1);
    verificar.equal(status.erros, 0);
});

teste("comando administrativo força e acompanha os 37 componentes sem rede externa", async contextoTeste => {
    const token = randomBytes(32).toString("hex");
    const url = await iniciar(contextoTeste, token, true);
    const comando = spawn(process.execPath, ["atualizar-precos.js", "--forcar", "--acompanhar"], {
        cwd: __dirname,
        env: {
            ...process.env, ADMIN_API_TOKEN: token, PCBUILDER_API_URL: url,
            SERPAPI_KEY: "", UPSTASH_REDIS_REST_URL: "", UPSTASH_REDIS_REST_TOKEN: ""
        },
        stdio: ["ignore", "pipe", "pipe"]
    });
    contextoTeste.after(() => comando.kill());
    let saida = "";
    let erros = "";
    comando.stdout.on("data", trecho => { saida += trecho.toString(); });
    comando.stderr.on("data", trecho => { erros += trecho.toString(); });
    const codigo = await new Promise((resolver, rejeitar) => {
        const limite = setTimeout(() => rejeitar(new Error("Comando administrativo não terminou.")), 20000);
        comando.once("error", () => { clearTimeout(limite); rejeitar(new Error("Falha ao iniciar comando.")); });
        comando.once("exit", codigo => { clearTimeout(limite); resolver(codigo); });
    });
    verificar.equal(codigo, 0, erros);
    verificar.match(saida, /37 de 37 componentes verificados/);
    verificar.match(saida, /0 de 37 componentes com cotação aceita/);
    verificar.equal(saida.includes(token) || erros.includes(token), false);
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
