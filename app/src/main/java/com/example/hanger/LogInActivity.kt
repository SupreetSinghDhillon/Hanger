package com.example.hanger

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hanger.adapters.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/*
Learnt about Firebase and Login Activity from ->
https://www.youtube.com/watch?v=Z-RE1QuUWPg
 */

class LogInActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var logIn: Button
    private lateinit var progressDialog: ProgressBar
    private lateinit var forgotPassword: TextView
    private lateinit var  auth: FirebaseAuth
    private lateinit var imageView: ImageView
    private var currentUser: FirebaseUser? = null

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



       // setUpWallpaper()
        FirebaseUtil.initializeFirebase()
        initialize()  //set up variables

        Util.checkPermissions(this)
        supportActionBar?.hide()


        register.setOnClickListener {
            registerOnCLick()
        }


        if (currentUser != null) {
            val intent: Intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        forgotPassword.setOnClickListener {
            forgotPasswordOnClick()
        }

        logInOnClick()
    }

    private fun forgotPasswordOnClick() {
        var intent: Intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun logInOnClick() {
        logIn.setOnClickListener {

            logInOnClick()
            var emailInput: String = email.text.toString()
            var passwordInput: String = password.text.toString()

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
            if (TextUtils.isEmpty(password.text)) {
                password.setError("Password is required")
                return@setOnClickListener
            }
            if(!(TextUtils.isEmpty(password.text)))
            {
                if(password.text.toString().length<6)
                {
                    password.setError("Password should be more than 6 characters long")
                    return@setOnClickListener
                }
            }
            //progressDialog.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(emailInput, passwordInput).addOnCompleteListener {

                if (it.isSuccessful) {

                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()

                    var intent: Intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }

                else {
                    Toast.makeText(this, "Please enter correct Email and Password" , Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerOnCLick() {
        var intent: Intent =Intent(this, RegisterUserActivity::class.java)
        startActivity(intent)
    }

    private fun setUpWallpaper() {
        var relativeLayout: RelativeLayout = findViewById(R.id.loginActivity)
        val animationDrawable: AnimationDrawable = relativeLayout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(0)
        animationDrawable.setExitFadeDuration(0)
        animationDrawable.start()
    }

    private fun initialize(){
        imageView = findViewById(R.id.imageView2)
        imageView.setImageResource(R.drawable.logo)
        forgotPassword = findViewById(R.id.forgotPasswordTextView)
        email = findViewById(R.id.editTextTextEmailAddress)
        password = findViewById(R.id.editTextTextPassword)
        logIn = findViewById(R.id.button)
        progressDialog = findViewById(R.id.progressBar)
//        auth = FirebaseAuth.getInstance()
        register = findViewById(R.id.registerTextView)
//        currentUser = FirebaseAuth.getInstance().currentUser
    }
}