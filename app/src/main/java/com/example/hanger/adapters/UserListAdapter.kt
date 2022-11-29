package com.example.hanger.adapters

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.MessageActivity
import com.example.hanger.R
import com.example.hanger.model.Message
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File

class UserListAdapter(private val users: ArrayList<User>, private val context: Context) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    private var theLastMessage: String? = null

    class UserListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val usernameView: TextView
        val avatarImage: CircleImageView
        val lastMessage: TextView

        init {
            usernameView = view.findViewById(R.id.username)
            avatarImage = view.findViewById(R.id.avatar_image)
            lastMessage = view.findViewById(R.id.lastMessage)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): UserListViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.user_item_fragment, viewGroup, false)

        return UserListViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: UserListViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        val user = users[position]
        viewHolder.usernameView.text = user.name

        var storageReference = FirebaseStorage.getInstance().reference.child("User Images")
            .child(user.id!!)

        val localFile = File.createTempFile("tempImage","jpg")

        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            viewHolder.avatarImage.setImageBitmap(bitmap)
        }.addOnFailureListener{
            viewHolder.avatarImage.setImageResource(R.mipmap.ic_launcher)
        }


        viewHolder.itemView.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                val intent: Intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("userid", user.id)
                context.startActivity(intent)
            }
        })

        getlastMessage(user.id!!, viewHolder.lastMessage)
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = users.size

    // check the last message
    private fun getlastMessage(userId: String, lastMessage: TextView) {
        val currentUser = FirebaseAuth.getInstance().currentUser!!
        val ref = FirebaseDatabase.getInstance().getReference("Chats")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(messageData: DataSnapshot in snapshot.children) {
                    val message: Message = messageData.getValue<Message>() as Message
                     if (message.receiver == currentUser.uid && message.sender == userId ||
                         message.sender == currentUser.uid && message.receiver == userId) {
                        theLastMessage = message.message
                    }
                }

                if (theLastMessage == null) {
                    theLastMessage = "No Message"
                }
                lastMessage.text = theLastMessage
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}