package com.example.hanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.core.view.View

class RegisterUserActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var name: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var password: EditText
    private lateinit var register: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var database: DatabaseReference



    private lateinit var  auth: FirebaseAuth

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

        supportActionBar?.hide()

        email = findViewById(R.id.editTextTextEmailAddress2)
        name = findViewById(R.id.editTextTextPersonName)
        phoneNumber = findViewById(R.id.editTextNumber)
        password = findViewById(R.id.editTextTextPassword2)
        register = findViewById(R.id.button2)
        progressBar = findViewById<ProgressBar>(R.id.progressBar2)
        auth = FirebaseAuth.getInstance()
       // user = auth.currentUser!!

       database = FirebaseDatabase.getInstance().reference.child("Users")


        register.setOnClickListener {


            var emailInput: String = email.text.toString()
            var passwordInput: String = password.text.toString()
            var nameInput = name.text.toString()
            var phoneInput = phoneNumber.text.toString()

            if (TextUtils.isEmpty(email.text)) {
                email.setError("Email Address is required")
                return@setOnClickListener
            }
            if(!(TextUtils.isEmpty(email.text)))
            {
               if(!(emailInput.matches(regex)))
               {
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

            if(!(TextUtils.isEmpty(password.text)))
            {
                if(passwordInput.length<6)
                {
                    password.setError("Password should be more than 6 characters long")
                    return@setOnClickListener
                }
            }

                auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                    .addOnCompleteListener {

                        if (it.isSuccessful) {
                            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                            var user: User = User()
                            user.createUser(nameInput, emailInput, phoneInput)

                            database.push().setValue(user)

                            var intent: Intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)

                        } else {
                            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

}