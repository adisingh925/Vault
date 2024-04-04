package app.android.adreal.vault.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.android.adreal.vault.databinding.DataItemBinding
import app.android.adreal.vault.model.Item

class DataAdapter(
    private val context: Context,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<DataAdapter.MyViewHolder>() {

    private lateinit var binding: DataItemBinding
    private var itemList = emptyList<Item>()

    interface OnItemClickListener {
        fun onItemClick(index: Int)
    }

    class MyViewHolder(binding: DataItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.title
        val card = binding.cardView
        val description = binding.description
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        binding = DataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = itemList[position].title
        holder.description.text = itemList[position].description

        holder.card.setOnClickListener {
            onItemClickListener.onItemClick(position)
        }

        holder.card.setOnLongClickListener {
            holder.card.isChecked = !holder.card.isChecked
            true
        }
    }

    fun setData(data: List<Item>) {
        this.itemList = data
        notifyDataSetChanged()
    }
}