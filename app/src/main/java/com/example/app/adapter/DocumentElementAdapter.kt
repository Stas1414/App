package com.example.app.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app.R
import com.example.app.model.DocumentElement

class DocumentElementAdapter(
    private var items: List<DocumentElement> = emptyList()
) : RecyclerView.Adapter<DocumentElementAdapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvElementAmount)
        val tvStoreFrom: TextView = itemView.findViewById(R.id.tvElementStoreFrom)
        val tvDebugInfo: TextView = itemView.findViewById(R.id.tvElementDebugInfo)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_item, parent, false)
        return ViewHolder(view)
    }


    override fun getItemCount(): Int = items.size


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val element = items[position]


        holder.tvName.text = element.name ?: "Название продукта не найдено"


        val amountText = element.amount?.toString() ?: "Не указано"
        holder.tvAmount.text = "Количество: $amountText"


        val storeFromNameText = element.storeFromName ?: "Склад не указан"
        holder.tvStoreFrom.text = "Склад-отправитель: $storeFromNameText"

        holder.tvDebugInfo.text = "ID поз.: ${element.id ?: "N/A"} / ID товара: ${element.elementId ?: "N/A"} / ID скл.: ${element.storeFrom ?: "N/A"}"
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newItems: List<DocumentElement>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}