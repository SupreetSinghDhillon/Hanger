package com.example.hanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.adapters.UserListAdapter
import com.example.hanger.model.Message
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class MessagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter
    private val users: ArrayList<User> = arrayListOf()
    private val userIdList = arrayListOf<String>()

    private lateinit var ref: DatabaseReference
    private lateinit var userDb: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        getUsersWithChats()

    }

    private fun getUsersWithChats() {
        val currentUser = FirebaseAuth.getInstance().currentUser!!

        ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for(messageData: DataSnapshot in snapshot.children) {
                    val message: Message = messageData.getValue<Message>() as Message
                    if (message.sender == currentUser.uid && !userIdList.contains(message.receiver)) {
                        userIdList.add(message.receiver!!)
                    } else if (message.receiver == currentUser.uid && !userIdList.contains(message.sender)) {
                        userIdList.add(message.sender!!)
                    }
                }
                getUsers()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun getUsers() {
        userDb = FirebaseDatabase.getInstance().getReference("Users")
        val context  = this

        userDb.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for(userData: DataSnapshot in snapshot.children) {
                    val user: User = userData.getValue<User>() as User

                    if(userIdList.contains(user.id) && !users.contains(user)) {
                        users.add(user)
                    }
                }
                adapter = UserListAdapter(users, context)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}