package com.example.hanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.adapters.MessageAdapter
import com.example.hanger.model.Message
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import de.hdodenhof.circleimageview.CircleImageView

class MessageActivity : AppCompatActivity() {

    private lateinit var avatarImage: CircleImageView
    private lateinit var username: TextView
    private lateinit var currUser: FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var toolbar: Toolbar

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private var messages: ArrayList<Message> = arrayListOf()

    private lateinit var btn_send: ImageButton
    private lateinit var text_send: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        avatarImage = findViewById(R.id.avatar_image)
        username = findViewById(R.id.username)
        btn_send = findViewById(R.id.btn_send)
        text_send = findViewById(R.id.text_send)
        val userId = intent.getStringExtra("userid")!!

        currUser = FirebaseAuth.getInstance().currentUser!!
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userToMessage: User = snapshot.getValue<User>() as User
                username.text = userToMessage.name
                avatarImage.setImageResource(R.mipmap.ic_launcher)

                readMessages(currUser.uid, userId)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        btn_send.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                val message = text_send.text.toString()

                if (message == "") return

                sendMessage(currUser.uid, userId, message)
                text_send.setText("")
            }
        })

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
    }

    private fun sendMessage(sender: String, receiver: String, message: String) {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        val map: HashMap<String, Any> = HashMap()

        map.put("sender", sender)
        map.put("receiver", receiver)
        map.put("message", message)

        db.child("Chats").push().setValue(map)
    }

    private fun readMessages(id: String, userId: String) {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chats")

        db.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messages.clear()

                for (messageData: DataSnapshot in snapshot.children) {
                    val message: Message = messageData.getValue<Message>() as Message

                    if ((message.receiver == id && message.sender == userId) ||
                            message.receiver == userId && message.sender == id) {
                        messages.add(message)
                    }
                }
                messageAdapter = MessageAdapter(messages)
                recyclerView.adapter = messageAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}