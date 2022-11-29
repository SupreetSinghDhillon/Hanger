package com.example.hanger.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.MessageActivity
import com.example.hanger.R
import com.example.hanger.model.ListingItemsModel
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class ListingAdapter (private val itemList: ArrayList<ListingItemsModel>, private val context: Context) : RecyclerView.Adapter <ListingAdapter.ViewHolder>(){
    private lateinit var mListener: onItemClickListener
    lateinit var storageReference: StorageReference

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
        //holder.imageView
        storageReference = FirebaseStorage.getInstance().reference.child("Item Images").child(currItem.itemId!!)
        val localFile = File.createTempFile("temporaryImage2","jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            holder.imageView.setImageBitmap(bitmap)
        }.addOnFailureListener{
           // Toast.makeText(this,"Image upload failed", Toast.LENGTH_SHORT).show()
        }

        holder.chatButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                val intent: Intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("userid", currItem.userId)
                context.startActivity(intent)
            }
        })
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    // populate each card here
    class ViewHolder (itemView: View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val tvItemName: TextView = itemView.findViewById(R.id.textViewName)
        val tvItemPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val tvItemLocation: TextView = itemView.findViewById(R.id.textViewLocation)
        val imageView: ImageView = itemView.findViewById(R.id.imageViewPicture)
        val chatButton: Button = itemView.findViewById((R.id.chat))

        init {
            itemView.setOnClickListener{
                clickListener.onCardClicked(adapterPosition)
            }
        }
    }


}