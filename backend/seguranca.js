const { createHash, timingSafeEqual } = require("node:crypto");

function tokenAdministrativoValido() {
    const token = process.env.ADMIN_API_TOKEN;
    return typeof token === "string" && token.trim() === token &&
        token.length >= 32 &&
        token !== process.env.SERPAPI_KEY &&
        token !== process.env.UPSTASH_REDIS_REST_TOKEN;
}

function exigirAdministrador(requisicao, resposta, proximo) {
    resposta.set("Cache-Control", "no-store");
    if (!tokenAdministrativoValido()) {
        return resposta.status(503).json({
            sucesso: false,
            mensagem: "Atualização administrativa não configurada."
        });
    }
    const cabecalho = requisicao.get("Authorization") || "";
    const recebido = cabecalho.startsWith("Bearer ") ? cabecalho.slice(7) : "";
    const resumo = valor => createHash("sha256").update(valor).digest();
    if (!recebido || !timingSafeEqual(resumo(recebido), resumo(process.env.ADMIN_API_TOKEN))) {
        return resposta.status(401).json({ sucesso: false, mensagem: "Acesso não autorizado." });
    }
    proximo();
}

module.exports = { exigirAdministrador, tokenAdministrativoValido };
