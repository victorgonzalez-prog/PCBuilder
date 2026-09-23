package com.example.pcbuilder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pcbuilder.databinding.ItemBuildBinding
import java.text.NumberFormat
import java.util.Locale

class BuildAdapter(
    componentesIniciais: List<Componente>,
    private val onRemover: (Componente) -> Unit
) : RecyclerView.Adapter<BuildAdapter.BuildViewHolder>() {

    private var componentes =
        componentesIniciais.toList()

    class BuildViewHolder(
        val binding: ItemBuildBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BuildViewHolder {

        val binding =
            ItemBuildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

        return BuildViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BuildViewHolder,
        position: Int
    ) {

        val componente =
            componentes[position]

        val formatoMoeda =
            NumberFormat.getCurrencyInstance(
                Locale("pt", "BR")
            )

        holder.binding.tvNome.text =
            componente.nome

        holder.binding.tvCategoria.text =
            componente.categoria

        holder.binding.tvPreco.text =
            formatoMoeda.format(componente.preco)

        holder.binding.btnRemover.setOnClickListener {

            onRemover(componente)
        }
    }

    override fun getItemCount(): Int {
        return componentes.size
    }

    fun atualizarLista(
        novaLista: List<Componente>
    ) {

        componentes =
            novaLista.toList()

        notifyDataSetChanged()
    }
}