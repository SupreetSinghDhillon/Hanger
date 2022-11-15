package com.example.hanger.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.MessageActivity
import com.example.hanger.R
import com.example.hanger.model.Message
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView

class MessageAdapter (private val messages: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    val MSG_LEFT = 0
    val MSG_RIGHT = 1

    class MessageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val textView: TextView
        val avatarImage: CircleImageView

        init {
            textView = view.findViewById(R.id.show_message)
            avatarImage = view.findViewById((R.id.avatar_image))
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MessageViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = if (viewType == MSG_LEFT) {
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.chat_bubble_left, viewGroup, false)
        } else {
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.chat_bubble_right, viewGroup, false)
        }
        return MessageViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val message = messages[position]
        viewHolder.textView.text = message.message
        viewHolder.avatarImage.setImageResource(R.mipmap.ic_launcher)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = messages.size

    override fun getItemViewType(position: Int): Int {
        val currUser =  FirebaseAuth.getInstance().currentUser!!
        return if (currUser.uid == messages[position].sender) {
            MSG_RIGHT
        } else {
            MSG_LEFT
        }
    }
}