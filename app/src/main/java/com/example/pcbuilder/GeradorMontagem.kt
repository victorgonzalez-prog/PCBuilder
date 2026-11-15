package com.example.pcbuilder

object GeradorMontagem {

    fun gerar(
        orcamento: Double,
        finalidade: String,
        pecas: List<Componente> = CatalogoComponentes.componentes
    ): RecomendacaoMontagem? {

        if (!orcamento.isFinite() || orcamento <= 0) {
            return null
        }

        fun categoria(nome: String) =
            pecas.filter { it.categoria == nome && it.preco.isFinite() && it.preco > 0 }

        val processadores = categoria("Processador")
        val placasMae = categoria("Placa-mãe")
        val memorias = categoria("Memória RAM")
        val placasVideo = categoria("Placa de vídeo")
        val armazenamentos = categoria("SSD")
        val fontes = categoria("Fonte")
        val gabinetes = categoria("Gabinete")
        val refrigeradores = categoria("Refrigeração")

        val exigeVideo =
            finalidade == "Jogos" ||
                    finalidade == "Edição e criação"

        val opcoesVideo: List<Componente?> =
            if (exigeVideo) {
                placasVideo
            } else {
                listOf(null) + placasVideo
            }

        var melhor: List<Componente>? = null
        var melhorNota = Double.NEGATIVE_INFINITY
        var melhorPreco = Double.MAX_VALUE

        for (processador in processadores) {

            for (placa in placasMae) {

                if (
                    processador.soquete == null ||
                    processador.soquete != placa.soquete
                ) {
                    continue
                }

                for (memoria in memorias) {

                    if (
                        memoria.tipoMemoria == null ||
                        memoria.tipoMemoria != placa.tipoMemoria
                    ) {
                        continue
                    }

                    for (placaVideo in opcoesVideo) {

                        if (
                            placaVideo == null &&
                            !temVideoIntegrado(processador)
                        ) {
                            continue
                        }

                        for (armazenamento in armazenamentos) {

                            for (fonte in fontes) {

                                if (
                                    potenciaFonte(fonte) <
                                    potenciaMinima(placaVideo)
                                ) {
                                    continue
                                }

                                for (gabinete in gabinetes) {

                                    for (refrigerador in refrigeradores) {

                                        val escolhidos =
                                            listOfNotNull(
                                                processador,
                                                placa,
                                                memoria,
                                                placaVideo,
                                                armazenamento,
                                                fonte,
                                                gabinete,
                                                refrigerador
                                            )

                                        val total =
                                            escolhidos.sumOf {
                                                it.preco
                                            }

                                        if (total > orcamento) {
                                            continue
                                        }

                                        if (
                                            VerificadorCompatibilidade
                                                .verificar(escolhidos)
                                                .isNotEmpty()
                                        ) {
                                            continue
                                        }

                                        val nota = pontuar(
                                            processador = processador,
                                            placaVideo = placaVideo,
                                            memoria = memoria,
                                            armazenamento = armazenamento,
                                            finalidade = finalidade
                                        )

                                        if (
                                            nota > melhorNota ||
                                            (
                                                    nota == melhorNota &&
                                                            total < melhorPreco
                                                    )
                                        ) {

                                            melhor = escolhidos
                                            melhorNota = nota
                                            melhorPreco = total
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return melhor?.let {
            RecomendacaoMontagem(
                componentes = it,
                orcamento = orcamento,
                finalidade = finalidade
            )
        }
    }

    private fun temVideoIntegrado(
        processador: Componente
    ): Boolean {

        return processador.id in setOf(
            1, 8, 10, 13, 14
        )
    }

    private fun potenciaFonte(
        fonte: Componente
    ): Int {

        return when (fonte.id) {
            6 -> 750
            29 -> 550
            30 -> 650
            31 -> 850
            else -> 0
        }
    }

    private fun potenciaMinima(
        placaVideo: Componente?
    ): Int {

        return when (placaVideo?.id) {
            null -> 450
            2 -> 750
            25 -> 650
            else -> 550
        }
    }

    private fun pontuar(
        processador: Componente,
        placaVideo: Componente?,
        memoria: Componente,
        armazenamento: Componente,
        finalidade: String
    ): Double {

        val pontuacaoProcessador = when (processador.id) {
            14 -> 10.0
            13 -> 8.0
            1 -> 8.0
            8 -> 7.0
            12 -> 6.0
            11 -> 5.0
            10 -> 4.0
            else -> 0.0
        }

        val pontuacaoVideo = when (placaVideo?.id) {
            2 -> 10.0
            25 -> 8.0
            24 -> 7.0
            23 -> 6.0
            7 -> 5.0
            22 -> 4.0
            else -> 0.0
        }

        val pontuacaoMemoria = when (memoria.id) {
            21 -> 8.0
            3, 19 -> 5.0
            18, 20 -> 2.0
            else -> 0.0
        }

        val pontuacaoArmazenamento = when (armazenamento.id) {
            27 -> 6.0
            5, 28 -> 3.0
            26 -> 1.0
            else -> 0.0
        }

        return when (finalidade) {

            "Jogos" ->
                pontuacaoProcessador * 3 +
                        pontuacaoVideo * 6 +
                        pontuacaoMemoria * 1.5 +
                        pontuacaoArmazenamento

            "Edição e criação" ->
                pontuacaoProcessador * 4 +
                        pontuacaoVideo * 3 +
                        pontuacaoMemoria * 3 +
                        pontuacaoArmazenamento * 2

            "Trabalho e estudos" ->
                pontuacaoProcessador * 4 +
                        pontuacaoMemoria * 3 +
                        pontuacaoArmazenamento * 2 -
                        if (placaVideo != null) 15.0 else 0.0

            else ->
                pontuacaoProcessador * 2 +
                        pontuacaoMemoria * 2 +
                        pontuacaoArmazenamento * 2 -
                        if (placaVideo != null) 20.0 else 0.0
        }
    }
}
