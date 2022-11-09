package com.example.hanger

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/*
Learnt about Firebase and Login Activity from ->
https://www.youtube.com/watch?v=gaykE36N7PY
 */

class LogInActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var logIn: Button
    private lateinit var progressDialog: ProgressBar

    private lateinit var  auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private lateinit var imageView: ImageView

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

    private lateinit var register: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        supportActionBar?.hide()

        imageView = findViewById(R.id.imageView2)
        imageView.setImageResource(R.drawable.hanger)

        register = findViewById(R.id.registerTextView)
        register.setOnClickListener {
            var intent: Intent =Intent(this, RegisterUserActivity::class.java)
            startActivity(intent)
        }

        email = findViewById(R.id.editTextTextEmailAddress)
        password = findViewById(R.id.editTextTextPassword)
        logIn = findViewById(R.id.button)
        progressDialog = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()

        logIn.setOnClickListener {
            var emailInput: String = email.text.toString()
            var passwordInput: String = password.text.toString()



            //progressDialog.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(emailInput,passwordInput).addOnCompleteListener {

                if(it.isSuccessful)
                {

                    Toast.makeText(this,"Success", Toast.LENGTH_SHORT).show()
                   var intent: Intent = Intent(this, HangerActivity::class.java)
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this,"Error" + it.exception, Toast.LENGTH_SHORT).show()
                }

            }
        }
    }
}