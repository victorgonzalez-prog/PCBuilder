package com.example.pcbuilder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pcbuilder.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()

        binding.btnVerBuild.setOnClickListener {

            val intent = Intent(
                this,
                BuildActivity::class.java
            )

            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.btnVerBuild.text =
            "Ver Minha Build (${BuildAtual.componentes.size})"
    }

    private fun configurarRecyclerView() {

        binding.recyclerComponentes.layoutManager =
            LinearLayoutManager(this)

        binding.recyclerComponentes.adapter =
            ComponenteAdapter(MockData.componentes) { componente ->

                abrirDetalhes(componente)
            }
    }

    private fun abrirDetalhes(componente: Componente) {

        val intent = Intent(
            this,
            ComponentDetailActivity::class.java
        )

        intent.putExtra(
            "COMPONENT_ID",
            componente.id
        )

        startActivity(intent)
    }
}