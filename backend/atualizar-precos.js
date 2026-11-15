require("dotenv").config();

async function consultar(endereco, caminho) {
    const resposta = await fetch(new URL(caminho, endereco), {
        signal: AbortSignal.timeout(90000),
        redirect: "error"
    });
    if (!resposta.ok) throw new Error("Falha ao consultar o servidor.");
    return resposta.json();
}

async function acompanhar(endereco) {
    const limite = Date.now() + 15 * 60 * 1000;
    let anterior = "";
    while (Date.now() < limite) {
        const status = await consultar(endereco, "/api/status");
        const progresso = `${status.concluidos} de ${status.total} componentes verificados; ${status.erros} erro(s).`;
        if (progresso !== anterior) {
            console.log(progresso);
            anterior = progresso;
        }
        if (!status.executando) {
            const dados = await consultar(endereco, "/api/precos");
            const cotados = dados.produtos.filter(produto => produto.status === "com_ofertas");
            const datas = [...new Set(cotados.map(produto => produto.dataColeta?.split(",")[0]).filter(Boolean))];
            console.log(`${cotados.length} de ${dados.produtos.length} componentes com cotação aceita.`);
            console.log("Datas das cotações disponíveis: " + (datas.join(", ") || "nenhuma"));
            if (status.erros > 0 || status.concluidos < status.total) {
                console.error("Coleta com falhas ou incompleta. Confira /api/status e a cota do provedor antes de repetir.");
                process.exitCode = 1;
            }
            return;
        }
        await new Promise(resolver => setTimeout(resolver, 4000));
    }
    console.error("Tempo de acompanhamento encerrado. A pesquisa pode continuar; consulte /api/status antes de solicitar outra.");
    process.exitCode = 1;
}

async function executar() {
    const argumentos = process.argv.slice(2);
    if (argumentos.some(argumento => !["--forcar", "--acompanhar"].includes(argumento))) {
        console.error("Opções disponíveis: --forcar --acompanhar");
        process.exitCode = 1;
        return;
    }
    const forcar = argumentos.includes("--forcar");
    const endereco = new URL(process.env.PCBUILDER_API_URL || "http://127.0.0.1:3001");
    const local = ["127.0.0.1", "localhost", "[::1]"].includes(endereco.hostname);
    if ((endereco.protocol !== "https:" && !(local && endereco.protocol === "http:")) ||
        endereco.username || endereco.password || endereco.search || endereco.hash ||
        !["", "/"].includes(endereco.pathname)) {
        throw new Error("URL inválida.");
    }
    const token = process.env.ADMIN_API_TOKEN;
    if (!token || token.trim() !== token || token.length < 32) {
        throw new Error("Senha administrativa não configurada.");
    }
    if (forcar) {
        const saude = await consultar(endereco, "/api/health");
        if (!saude.atualizacaoForcada) {
            console.error("O servidor ainda não suporta coleta forçada. Atualize a publicação antes de repetir.");
            process.exitCode = 1;
            return;
        }
    }
    const resposta = await fetch(new URL("/api/atualizar", endereco), {
        method: "POST",
        headers: {
            Authorization: "Bearer " + token,
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ forcar }),
        signal: AbortSignal.timeout(90000),
        redirect: "error"
    });
    if (!resposta.ok) {
        console.error("Atualização não autorizada ou indisponível. HTTP " + resposta.status);
        process.exitCode = 1;
        return;
    }
    const dados = await resposta.json();
    console.log(dados.iniciada ?
        "Pesquisa solicitada. Acompanhe o progresso em /api/status." :
        "Nenhuma pesquisa iniciada: cache ainda válido ou atualização em andamento.");
    if (argumentos.includes("--acompanhar")) await acompanhar(endereco);
}

executar().catch(() => {
    console.error("Não foi possível solicitar ou acompanhar a atualização. Verifique a API e a configuração local; consulte /api/status antes de repetir.");
    process.exitCode = 1;
});
