package com.example.pcbuilder

object VerificadorCompatibilidade {

    fun verificar(componentes: List<Componente>): List<String> {

        val problemas = mutableListOf<String>()

        val processador = componentes.find {
            it.categoria == "Processador"
        }

        val placaMae = componentes.find {
            it.categoria == "Placa-mãe"
        }

        val memoria = componentes.find {
            it.categoria == "Memória RAM"
        }

        if (processador != null && placaMae != null) {

            if (processador.soquete.isNullOrBlank() ||
                placaMae.soquete.isNullOrBlank()
            ) {
                problemas.add(
                    "Não foi possível verificar o soquete."
                )

            } else if (!processador.soquete.equals(
                    placaMae.soquete,
                    ignoreCase = true
                )
            ) {
                problemas.add(
                    "Processador e placa-mãe possuem sockets incompatíveis."
                )
            }
        }

        if (placaMae != null && memoria != null) {

            if (placaMae.tipoMemoria.isNullOrBlank() ||
                memoria.tipoMemoria.isNullOrBlank()
            ) {
                problemas.add(
                    "Não foi possível verificar o tipo de memória."
                )

            } else if (!placaMae.tipoMemoria.equals(
                    memoria.tipoMemoria,
                    ignoreCase = true
                )
            ) {
                problemas.add(
                    "Memória RAM incompatível com a placa-mãe."
                )
            }
        }

        return problemas
    }
}
