package com.example.hanger

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.hanger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var name: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var password: EditText
    private lateinit var register: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var changePictureButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var tempUri: Uri
    var profileImageSelected = false
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    lateinit var galleryLauncher: ActivityResultLauncher<String>
    var imageChanged: Int = 0

    /*
    Took the below pattern from ->
    https://stackoverflow.com/questions/1819142/how-should-i-validate-an-e-mail-address
     */
    val regex: Regex = Regex(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)

        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid
        database = FirebaseDatabase.getInstance().reference.child("Users")

        supportActionBar?.hide()

        email = findViewById(R.id.editTextTextEmailAddress2)
        name = findViewById(R.id.editTextTextPersonName)
        phoneNumber = findViewById(R.id.editTextNumber)
        password = findViewById(R.id.editTextTextPassword2)
        register = findViewById(R.id.button2)
        progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        profileImageView = findViewById(R.id.personImage)

        firebaseStorage = FirebaseStorage.getInstance()
        storageReference = firebaseStorage.reference



        val tempImgFile = File(getExternalFilesDir(null), "tmp_image.jpg")
        tempUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, tempImgFile)


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



        changePictureButton  = findViewById(R.id.changePictureButton)
        changePictureButton.setOnClickListener {

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

        register.setOnClickListener {
            var emailInput: String = email.text.toString()
            var passwordInput: String = password.text.toString()
            var nameInput = name.text.toString()
            var phoneInput = phoneNumber.text.toString()

            if (TextUtils.isEmpty(email.text)) {
                email.setError("Email Address is required")
                return@setOnClickListener
            }
            if (!(TextUtils.isEmpty(email.text))) {
                if (!(emailInput.matches(regex))) {
                    email.setError("Please Enter Correct Email Address")
                    return@setOnClickListener
                }
            }
            if (TextUtils.isEmpty(name.text)) {
                name.setError("Full Name is required")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(phoneNumber.text)) {
                phoneNumber.setError("Phone Number is required")
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(password.text)) {
                password.setError("Password is required")
                return@setOnClickListener
            }

            if (!(TextUtils.isEmpty(password.text))) {
                if (passwordInput.length < 6) {
                    password.setError("Password should be more than 6 characters long")
                    return@setOnClickListener
                }
            }

            auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                       Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                        val userId = it.result.user!!.uid.toString()
                        var user: User = User(
                            userId,
                            nameInput,
                            emailInput,
                            phoneInput
                        )

                        CoroutineScope(IO).launch {
                            database.child(userId).setValue(user).addOnCompleteListener {
                                if(it.isSuccessful){
                                    uploadProfilePic()
                                    println("debug: registration success")
//                                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        var intent: Intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                        Log.d("ERROR", it.exception.toString())
                    }
                }
        }
    }

    private fun uploadProfilePic() {
        if(profileImageSelected) {
            val reference =
                firebaseStorage.reference.child("User Images").child(auth.currentUser!!.uid)
            reference.putFile(tempUri)
        }
    }
}