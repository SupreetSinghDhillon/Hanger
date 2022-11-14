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
import com.example.hanger.model.User
import de.hdodenhof.circleimageview.CircleImageView

class UserListAdapter(private val users: ArrayList<User>, private val context: Context) : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

    class UserListViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val usernameView: TextView
        val avatarImage: CircleImageView

        init {
            usernameView = view.findViewById(R.id.username)
            avatarImage = view.findViewById((R.id.avatar_image))
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
        viewHolder.avatarImage.setImageResource(R.mipmap.ic_launcher)

        viewHolder.itemView.setOnClickListener(object: View.OnClickListener {

            override fun onClick(p0: View?) {
                val intent: Intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("userid", user.id)
                context.startActivity(intent)
            }

        })
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = users.size
}