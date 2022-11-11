package com.example.hanger

import android.provider.ContactsContract.CommonDataKinds.Email
import java.util.jar.Attributes.Name
/*
class User {

    var name: String = ""
    var email: String = ""
    var phoneNumber: String = ""

    fun User(){

    }

    fun createUser(nameOfUser: String,emailOfUser: String, phone: String)
    {
        name = nameOfUser
        email = emailOfUser
        phoneNumber = phone
    }
}

 */
 class User{
    lateinit var name: String
    lateinit var email: String
    lateinit var phone: String

    constructor(){

    }

    constructor(name: String, email: String, phone: String){
        this.name = name;
        this.email=email;
        this.phone =phone;
    }




}