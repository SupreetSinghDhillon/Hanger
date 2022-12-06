package com.example.hanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.Notifications.*
import com.example.hanger.adapters.MessageAdapter
import com.example.hanger.model.Message
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import de.hdodenhof.circleimageview.CircleImageView
import retrofit2.Call
import retrofit2.Callback

class MessageActivity : AppCompatActivity() {

    private lateinit var avatarImage: CircleImageView
    private lateinit var username: TextView
    private lateinit var currUser: FirebaseUser
    private lateinit var reference: DatabaseReference
    private lateinit var toolbar: Toolbar
    private lateinit var userId: String

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var recyclerView: RecyclerView
    private var messages: ArrayList<Message> = arrayListOf()

    private lateinit var btn_send: ImageButton
    private lateinit var text_send: EditText

    private lateinit var apiService: APIService
    private var notify = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        avatarImage = findViewById(R.id.avatar_image)
        username = findViewById(R.id.username)
        btn_send = findViewById(R.id.btn_send)
        text_send = findViewById(R.id.text_send)
        userId = intent.getStringExtra("userid")!!

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
                notify = true
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

        apiService = Client.getClient("https://fcm.googleapis.com/")
            .create(APIService::class.java)
    }

    private fun sendMessage(sender: String, receiver: String, message: String) {
        val db: DatabaseReference = FirebaseDatabase.getInstance().getReference()

        val map: HashMap<String, Any> = HashMap()

        map.put("sender", sender)
        map.put("receiver", receiver)
        map.put("message", message)

        db.child("Chats").push().setValue(map)

        var chatRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(currUser.uid)
            .child(userId)

        chatRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    chatRef.child("id").setValue(userId)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
            .child(userId)
            .child(currUser.uid)

        chatRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()) {
                    chatRef.child("id").setValue(currUser.uid)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        val msg: String = message
        reference = FirebaseDatabase.getInstance().getReference("Users").child(currUser.uid)
        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val user: User = snapshot.getValue<User>() as User
                if(notify) {
                    sendNotification(receiver, user.name, msg)
                }
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun sendNotification(receiver: String, name: String?, msg: String) {
        val tokens: DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val query: Query = tokens.orderByKey().equalTo(receiver)
        val context = this
        query.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshots: DataSnapshot) {
                for (snapshot: DataSnapshot in snapshots.children) {
                    val token: Token = snapshot.getValue<Token>() as Token
                    val data = Data(
                        currUser.uid,
                        R.mipmap.ic_launcher,
                        "$name: $msg",
                        "New Message",
                        userId
                    )
                    val sender = Sender(data, token.token)
                    apiService.sendNotification(sender).enqueue(object: Callback<Response>{
                        override fun onResponse(
                            call: Call<Response>,
                            response: retrofit2.Response<Response>
                        ) {
                            if(response.code() == 200) {
                                if (response.body()?.success != 1) {
                                    Toast.makeText(context, "Failed to send notification!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<Response>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
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