package com.example.hanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.adapters.UserListAdapter
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class MessagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter
    private val users: ArrayList<User> = arrayListOf()

    private val userDB: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        getUsers()

    }

    private fun getUsers() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val context  = this
        userDB.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for(userData: DataSnapshot in snapshot.children) {
                    val newUser: User = userData.getValue<User>() as User
                    newUser.id = userData.key

                    if (currentUser != null && newUser.id != currentUser.uid) {
                        users.add(newUser)
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