package com.example.hanger
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import org.w3c.dom.Text
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

class ProfileActivity : AppCompatActivity() {

    lateinit var auth: FirebaseAuth
    lateinit var databaseReference: DatabaseReference
    lateinit var profileImageView: ImageView
    lateinit var storageReference: StorageReference
    private lateinit var firebaseStorage: FirebaseStorage
    lateinit var nameView: TextView
    lateinit var emailView: TextView
    lateinit var phoneView: TextView
    lateinit var tempUri: Uri
    lateinit var saveButton: Button
    lateinit var changePicture: Button
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    lateinit var galleryLauncher: ActivityResultLauncher<String>
    var imageChanged: Int = 0
    var profileImageSelected = false
    lateinit var userID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        nameView = findViewById(R.id.nameTextView)
        emailView = findViewById(R.id.emailTextView)
        phoneView = findViewById(R.id.phoneTextView)
        profileImageView = findViewById(R.id.profilePhoto)
        saveButton = findViewById(R.id.saveButton)
        changePicture = findViewById(R.id.changePicture)

        val tempImgFile = File(getExternalFilesDir(null), "tmp_image.jpg")
        tempUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, tempImgFile)

        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference


        galleryCameraLauncher()



        changePicture.setOnClickListener {
            showDialog()
        }

        saveButton.setOnClickListener {
            saveUserInformation()
        }


        //uploadPicButton = findViewById(R.id.changePictureButton)
        auth = FirebaseAuth.getInstance()
        userID = auth.currentUser?.uid.toString()

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        if(userID.isNotEmpty()){
            getUserData()
        }
    }

    private fun saveUserInformation() {
        if(profileImageSelected) {
            val reference =
                firebaseStorage.reference.child("User Images").child(auth.currentUser!!.uid)
            reference.putFile(tempUri).addOnCompleteListener {
                finish()
            }
        }
    }

    private fun galleryCameraLauncher(){

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { it: ActivityResult ->
            if (it.resultCode == Activity.RESULT_OK) {
                imageChanged = 1
                val imageBitmap = Util.getBitmap(this, tempUri)
                // finalImageUri = tempUri.toString()
                profileImageView.setImageBitmap(imageBitmap)
                profileImageSelected = true
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                imageChanged = 1
                profileImageView.setImageURI(it)
                profileImageSelected = true

                thread {
                    val inputStream = contentResolver.openInputStream(it)
                    val outputStream = FileOutputStream(tempUri.path)
                    //finalImageUri = tempUri.toString()
                    if (inputStream != null) {
                        FileUtils.copy(inputStream, outputStream)
                        inputStream.close()
                        outputStream.close()
                    }
                }
            }
        }
    }
    private fun showDialog(){
        AlertDialog
            .Builder(this)
            .setTitle("Pick Profile Picture")
            .setPositiveButton(
                "Open Camera",
                DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    val cameraStartIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraStartIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri)
                    cameraLauncher.launch(cameraStartIntent)
                })
            .setNegativeButton(
                "Select From Gallery",
                DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    galleryLauncher.launch("image/*")
                })
            .create()
            .show()
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