package com.example.hanger.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.R
import com.example.hanger.model.ListingItemsModel

class ListingAdapter (private val itemList: ArrayList<ListingItemsModel>) : RecyclerView.Adapter <ListingAdapter.ViewHolder>(){
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onCardClicked (position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener) {
        mListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val itemView = LayoutInflater.from(parent.context).inflate(R.layout.listing_item_layout, parent, false)
        return ViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currItem = itemList[position]
        holder.tvItemName.text = currItem.itemName
        holder.tvItemPrice.text = "$"+currItem.itemPrice+" per day"
        holder.tvItemLocation.text = currItem.itemLocation
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    // populate each card here
    class ViewHolder (itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val tvItemName: TextView = itemView.findViewById(R.id.textViewName)
        val tvItemPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val tvItemLocation: TextView = itemView.findViewById(R.id.textViewLocation)

        init {
            itemView.setOnClickListener{
                clickListener.onCardClicked(adapterPosition)
            }
        }
    }


}