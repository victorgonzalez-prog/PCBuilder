package com.example.pcbuilder

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.pcbuilder.databinding.ActivityComponentDetailBinding
import java.text.NumberFormat
import java.util.Locale

class ComponentDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityComponentDetailBinding

    private var componenteAtual: Componente? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityComponentDetailBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val componentId =
            intent.getIntExtra("COMPONENT_ID", -1)

        componenteAtual =
            MockData.buscarPorId(componentId)

        componenteAtual?.let { componente ->

            mostrarComponente(componente)

            val jaAdicionado =
                BuildAtual.componentes.any {
                    it.id == componente.id
                }

            if (jaAdicionado) {
                mostrarComoAdicionado()
            }
        }

        binding.btnAdicionar.setOnClickListener {

            componenteAtual?.let { componente ->

                BuildAtual.adicionar(componente)

                mostrarComoAdicionado()
            }
        }

        binding.btnVerBuild.setOnClickListener {

            val intent = Intent(
                this,
                BuildActivity::class.java
            )

            startActivity(intent)
        }
    }

    private fun mostrarComoAdicionado() {

        binding.btnAdicionar.text =
            "Adicionado à Build"

        binding.btnAdicionar.isEnabled =
            false

        binding.tvStatus.visibility =
            View.VISIBLE
    }

    private fun mostrarComponente(
        componente: Componente
    ) {

        val formatoMoeda =
            NumberFormat.getCurrencyInstance(
                Locale("pt", "BR")
            )

        binding.tvNome.text =
            componente.nome

        binding.tvCategoria.text =
            componente.categoria

        binding.tvPreco.text =
            formatoMoeda.format(componente.preco)

        binding.tvFabricante.text =
            "Fabricante: ${componente.fabricante}"

        binding.tvDescricao.text =
            componente.descricao
                ?: "Descrição não disponível."

        binding.tvEspecificacao.text =
            componente.especificacao
                ?: "Especificação não disponível."

        if (componente.imagemResId != null) {

            binding.imgComponente.setImageResource(
                componente.imagemResId
            )

        } else {

            binding.imgComponente.setImageResource(
                android.R.drawable.ic_menu_manage
            )
        }
    }
}