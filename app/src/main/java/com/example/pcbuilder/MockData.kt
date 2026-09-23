package com.example.pcbuilder

object MockData {

    val componentes = listOf(

        Componente(
            id = 1,
            nome = "Ryzen 5 9600X",
            categoria = "Processador",
            fabricante = "AMD",
            preco = 1599.90,
            descricao = "Processador AMD para computadores de alto desempenho.",
            especificacao = "6 núcleos • Socket AM5",
            imagemResId = R.drawable.ryzen_5
        ),

        Componente(
            id = 2,
            nome = "RX 9070 XT",
            categoria = "Placa de vídeo",
            fabricante = "AMD",
            preco = 4999.90,
            descricao = "Placa de vídeo indicada para jogos em alta resolução.",
            especificacao = "16 GB de memória",
            imagemResId = R.drawable.rx_9070
        ),

        Componente(
            id = 3,
            nome = "Kingston Fury Beast 32 GB",
            categoria = "Memória RAM",
            fabricante = "Kingston",
            preco = 699.90,
            descricao = "Kit de memória DDR5 para computadores de alto desempenho.",
            especificacao = "32 GB DDR5",
            imagemResId = R.drawable.kingston_fury
        ),

        Componente(
            id = 4,
            nome = "ASUS TUF Gaming B650M",
            categoria = "Placa-mãe",
            fabricante = "ASUS",
            preco = 1299.90,
            descricao = "Placa-mãe para processadores AMD com socket AM5.",
            especificacao = "Chipset B650 • Socket AM5",
            imagemResId = R.drawable.asus_tuf
        ),

        Componente(
            id = 5,
            nome = "Kingston NV3 1 TB",
            categoria = "SSD",
            fabricante = "Kingston",
            preco = 429.90,
            descricao = "SSD para computador gamer",
            especificacao = "1 TB • NVMe",
            imagemResId = R.drawable.kingston_nv3
        ),

        Componente(
            id = 6,
            nome = "Corsair RM750e",
            categoria = "Fonte",
            fabricante = "Corsair",
            preco = 749.90,
            descricao = "Fonte para computadores gamer e workstations.",
            especificacao = "750 W",
            imagemResId = R.drawable.corsair_rm750e
        )
    )

    fun buscarPorId(id: Int): Componente? {
        return componentes.find { it.id == id }
    }
}