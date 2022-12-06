package com.example.hanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler.Value
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.Notifications.Token
import com.example.hanger.adapters.UserListAdapter
import com.example.hanger.model.Chatlist
import com.example.hanger.model.Message
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.messaging.FirebaseMessaging

class MessagesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserListAdapter
    private val users: ArrayList<User> = arrayListOf()
    private val usersList = arrayListOf<Chatlist>()
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        getUsersWithChats()
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if(it.isComplete){
                updateToken(it.result.toString())
            }
        }

    }

    private fun getUsersWithChats() {
        currentUser = FirebaseAuth.getInstance().currentUser!!

        val ref: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("Chatlist").child(currentUser.uid)
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for(data: DataSnapshot in snapshot.children) {
                    val chatlist = data.getValue<Chatlist>() as Chatlist
                    usersList.add(chatlist)
                }
                chatList()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun updateToken(tokenStr: String) {
        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token = Token(tokenStr)
        ref.child(currentUser.uid).setValue(token)
    }

    private fun chatList() {
        val userRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val context  = this
        userRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                users.clear()
                for(data: DataSnapshot in snapshot.children) {
                    val user: User = data.getValue<User>() as User
                    for(chatList: Chatlist in usersList) {
                        if (user.id == chatList.id) {
                            users.add(user)
                        }
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