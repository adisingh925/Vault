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
    private val onItemClickListener: OnItemClickListener,
    private val onItemLongClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<DataAdapter.MyViewHolder>() {

    private lateinit var binding: DataItemBinding
    private var itemList = ArrayList<Item>()

    interface OnItemClickListener {
        fun onItemClick(index: Int)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(primaryKey: Int, status: Boolean)
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
            onItemLongClickListener.onItemLongClick(itemList[position].id, holder.card.isChecked)
            true
        }
    }

    fun setData(data: List<Item>) {
        if (data.size > this.itemList.size) {
            for (itemData in data.indices) {
                if (!this.itemList.contains(data[itemData])) {
                    this.itemList.add(data[itemData])
                    notifyItemInserted(this.itemList.size - 1)
                }
            }
        } else {
            for (itemData in data.indices) {
                if (data[itemData] != this.itemList[itemData]) {
                    this.itemList[itemData] = data[itemData]
                    notifyItemChanged(itemData)
                }
            }
        }
    }

    fun deleteItem(primaryKey: Int) {
        val position = itemList.indexOfFirst { it.id == primaryKey }
        if (position != -1) {
            val newItems = ArrayList(itemList)
            newItems.removeAt(position)
            itemList.clear()
            itemList.addAll(newItems)
            notifyItemRemoved(position)
            val itemChangedCount = itemList.size - position
            notifyItemRangeChanged(position, itemChangedCount)
        }
    }
}