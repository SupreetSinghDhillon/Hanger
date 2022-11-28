package com.example.hanger
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.io.File

class ProfileActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var profileImageView: ImageView
    lateinit var storageReference: StorageReference
    lateinit var nameView: TextView
    lateinit var emailView: TextView
    lateinit var phoneView: TextView
    lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nameView = findViewById(R.id.nameTextView)
        emailView = findViewById(R.id.emailTextView)
        phoneView = findViewById(R.id.phoneTextView)
        profileImageView = findViewById(R.id.profilePhoto)


        //uploadPicButton = findViewById(R.id.changePictureButton)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.uid.toString()

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        if(userID.isNotEmpty()){
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
                getUserProfile()
            }

            override fun onCancelled(error: DatabaseError) {
                finish()
                Toast.makeText(this@ProfileActivity,"Error",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getUserProfile() {
        storageReference = FirebaseStorage.getInstance().reference.child("User Images").child(auth.currentUser!!.uid)
        val localFile = File.createTempFile("tempImage","jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            profileImageView.setImageBitmap(bitmap)
        }
            .addOnFailureListener{
                    Toast.makeText(this,"Image upload failed",Toast.LENGTH_SHORT).show()
            }
    }
}