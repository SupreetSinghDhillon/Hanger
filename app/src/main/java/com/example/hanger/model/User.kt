package com.example.hanger.model

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
 class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var phone: String? = null,
    var imgUrl: String? = null
)