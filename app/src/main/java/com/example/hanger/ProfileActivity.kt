package com.example.hanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

     var user: User? = null
    lateinit var auth: FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var nameView: TextView
    lateinit var emailView: TextView
    lateinit var phoneView: TextView
    //lateinit var userProfile: User

    lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nameView = findViewById(R.id.nameTextView)
        emailView = findViewById(R.id.emailTextView)
        phoneView = findViewById(R.id.phoneTextView)

        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.uid.toString()

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        if(userID.isNotEmpty()){
           //Toast.makeText(this,"i" + userID,Toast.LENGTH_SHORT).show()
            getUserData()
        }



    }

    private fun getUserData() {
        databaseReference.child(userID).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val user = snapshot.getValue(User::class.java)

                nameView.setText(user?.name ?: "username")
                emailView.setText(user?.email ?: "email")
                phoneView.setText(user?.phone ?: "phone")
            }

            override fun onCancelled(error: DatabaseError) {
                finish()
                Toast.makeText(this@ProfileActivity,"Error",Toast.LENGTH_SHORT).show()
            }

        })
    }

    //private fun showProgressBar()
}