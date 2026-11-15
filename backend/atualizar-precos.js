require("dotenv").config();

async function executar() {
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
    const resposta = await fetch(new URL("/api/atualizar", endereco), {
        method: "POST",
        headers: { Authorization: "Bearer " + token },
        signal: AbortSignal.timeout(90000)
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
}

executar().catch(() => {
    console.error("Não foi possível solicitar a atualização. Verifique a URL e a configuração local.");
    process.exitCode = 1;
});
