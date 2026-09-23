package com.example.pcbuilder

object BuildAtual {

    val categoriasObrigatorias = listOf(
        "Processador",
        "Placa de vídeo",
        "Memória RAM",
        "Placa-mãe",
        "SSD",
        "Fonte"
    )

    val componentes = mutableListOf<Componente>()

    fun adicionar(componente: Componente) {

        val indiceExistente = componentes.indexOfFirst {
            it.categoria == componente.categoria
        }

        if (indiceExistente >= 0) {
            componentes[indiceExistente] = componente
        } else {
            componentes.add(componente)
        }
    }

    fun remover(componente: Componente) {
        componentes.removeAll {
            it.id == componente.id
        }
    }

    fun total(): Double {
        return componentes.sumOf {
            it.preco
        }
    }

    fun categoriasFaltantes(): List<String> {

        return categoriasObrigatorias.filter { categoria ->

            componentes.none {
                it.categoria == categoria
            }
        }
    }

    fun quantidadeCategoriasSelecionadas(): Int {
        return categoriasObrigatorias.size - categoriasFaltantes().size
    }

    fun estaCompleta(): Boolean {
        return categoriasFaltantes().isEmpty()
    }
}