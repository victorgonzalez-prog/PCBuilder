package com.example.pcbuilder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.pcbuilder.databinding.ActivityBuildSummaryBinding
import java.text.NumberFormat
import java.util.Locale

class BuildSummaryActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityBuildSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityBuildSummaryBinding.inflate(
                layoutInflater
            )

        setContentView(binding.root)

        mostrarResumo()

        binding.btnVoltarCatalogo.setOnClickListener {

            val intent =
                Intent(
                    this,
                    MainActivity::class.java
                )

            intent.flags =
                Intent.FLAG_ACTIVITY_CLEAR_TOP

            startActivity(intent)

            finish()
        }
    }

    private fun mostrarResumo() {

        val quantidade =
            BuildAtual.quantidadeCategoriasSelecionadas()

        val totalCategorias =
            BuildAtual.categoriasObrigatorias.size

        if (BuildAtual.estaCompleta()) {

            binding.tvStatusBuild.text =
                "Build completa ✓"

        } else {

            binding.tvStatusBuild.text =
                "Progresso: $quantidade de $totalCategorias categorias"
        }

        if (BuildAtual.componentes.isEmpty()) {

            binding.tvComponentesSelecionados.text =
                "Nenhum componente selecionado."

        } else {

            binding.tvComponentesSelecionados.text =
                BuildAtual.componentes.joinToString(
                    separator = "\n\n"
                ) { componente ->

                    "✓ ${componente.categoria}\n${componente.nome}"
                }
        }

        val faltantes =
            BuildAtual.categoriasFaltantes()

        binding.tvFaltantes.text =

            if (faltantes.isEmpty()) {

                "Nenhum componente faltando."

            } else {

                faltantes.joinToString(
                    separator = "\n"
                ) { categoria ->

                    "• $categoria"
                }
            }

        val formatoMoeda =
            NumberFormat.getCurrencyInstance(
                Locale("pt", "BR")
            )

        binding.tvTotal.text =
            "Total: ${formatoMoeda.format(BuildAtual.total())}"
    }
}