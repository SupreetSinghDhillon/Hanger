package com.example.hanger

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.jar.Attributes.Name

class User {

    var name: String = ""
    var email: String = ""
    var phoneNumber: Int = 0

    fun createUser(nameOfUser: String,emailOfUser: String, phone: Int)
    {
        name = nameOfUser
        email = emailOfUser
        phoneNumber = phone
    }
}