package com.example.hanger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var resetButton: Button
    private lateinit var emailEditText: EditText
    private lateinit var auth: FirebaseAuth
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
        setContentView(R.layout.activity_forgot_password)

        resetButton = findViewById(R.id.resetPasswordButton)
        emailEditText = findViewById(R.id.editTextTextEmailAddress3)
        auth = FirebaseAuth.getInstance()

        resetButton.setOnClickListener {

            var email: String = emailEditText.text.toString()
            if (TextUtils.isEmpty(emailEditText.text)) {
                emailEditText.setError("Email Address is required")
                return@setOnClickListener
            }
            if(!(TextUtils.isEmpty(emailEditText.text)))
            {
                if(!(email.matches(regex)))
                {
                    emailEditText.setError("Please Enter Correct Email Address")
                    return@setOnClickListener
                }

            }

            auth.sendPasswordResetEmail(email).addOnCompleteListener {

                if(it.isSuccessful)
                {
                    Toast.makeText(this,"Check Email to reset Password",Toast.LENGTH_SHORT).show()
                }
                else{
                    Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}