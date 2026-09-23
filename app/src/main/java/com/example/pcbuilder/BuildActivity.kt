package com.example.pcbuilder

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pcbuilder.databinding.ActivityBuildBinding
import java.text.NumberFormat
import java.util.Locale

class BuildActivity : AppCompatActivity() {

    private lateinit var binding:
            ActivityBuildBinding

    private lateinit var adapter:
            BuildAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding =
            ActivityBuildBinding.inflate(layoutInflater)

        setContentView(binding.root)

        adapter =
            BuildAdapter(
                BuildAtual.componentes
            ) { componente ->

                BuildAtual.remover(componente)

                atualizarTela()
            }

        binding.recyclerBuild.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerBuild.adapter =
            adapter

        binding.btnResumo.setOnClickListener {

            val intent = Intent(
                this,
                BuildSummaryActivity::class.java
            )

            startActivity(intent)
        }

        atualizarTela()
    }

    override fun onResume() {
        super.onResume()

        atualizarTela()
    }

    private fun atualizarTela() {

        val quantidade =
            BuildAtual.componentes.size

        binding.tvQuantidade.text =
            "$quantidade componentes selecionados"

        val formatoMoeda =
            NumberFormat.getCurrencyInstance(
                Locale("pt", "BR")
            )

        binding.tvTotal.text =
            "Total: ${formatoMoeda.format(BuildAtual.total())}"

        adapter.atualizarLista(
            BuildAtual.componentes
        )

        if (BuildAtual.componentes.isEmpty()) {

            binding.tvVazio.visibility =
                View.VISIBLE

            binding.recyclerBuild.visibility =
                View.GONE

            binding.btnResumo.isEnabled =
                false

        } else {

            binding.tvVazio.visibility =
                View.GONE

            binding.recyclerBuild.visibility =
                View.VISIBLE

            binding.btnResumo.isEnabled =
                true
        }
    }
}