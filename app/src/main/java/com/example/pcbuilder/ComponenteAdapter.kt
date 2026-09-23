package com.example.pcbuilder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pcbuilder.databinding.ItemComponenteBinding
import java.text.NumberFormat
import java.util.Locale

class ComponenteAdapter(
    private val componentes: List<Componente>,
    private val onClick: (Componente) -> Unit
) : RecyclerView.Adapter<ComponenteAdapter.ComponenteViewHolder>() {

    class ComponenteViewHolder(
        val binding: ItemComponenteBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ComponenteViewHolder {

        val binding = ItemComponenteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ComponenteViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ComponenteViewHolder,
        position: Int
    ) {

        val componente = componentes[position]

        val formatoMoeda =
            NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        holder.binding.tvNome.text = componente.nome
        holder.binding.tvCategoria.text = componente.categoria
        holder.binding.tvPreco.text = formatoMoeda.format(componente.preco)

        if (componente.imagemResId != null) {
            holder.binding.imgComponente.setImageResource(
                componente.imagemResId
            )
        } else {
            holder.binding.imgComponente.setImageResource(
                android.R.drawable.ic_menu_manage
            )
        }

        holder.binding.root.setOnClickListener {
            onClick(componente)
        }
    }

    override fun getItemCount(): Int {
        return componentes.size
    }
}