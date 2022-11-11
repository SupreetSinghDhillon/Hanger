package com.example.hanger

import android.R.attr.button
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class UserAddListingActivity : AppCompatActivity() {
    private lateinit var itemName: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemLocation: EditText
    private lateinit var itemDesc: EditText
    private lateinit var publishListing: Button
    private lateinit var database: DatabaseReference
    private lateinit var newListingName: String
    private lateinit var newListingPrice: String
    private lateinit var newListingLocation: String
    var newListingDesc: String? = "This user has not added any description."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_add_listing)
        supportActionBar?.hide()
        itemName = findViewById(R.id.editListingName)
        itemPrice = findViewById(R.id.editListingPrice)
        itemLocation = findViewById(R.id.editListingLocation)
        itemDesc = findViewById(R.id.editListingDescription)
        publishListing = findViewById(R.id.buttonPublishListing)
        // to do: push logged in user's account onto the table as well so the buyer can contact them

        database = FirebaseDatabase.getInstance().getReference("Listings")

        // only enable button if all the fields are entered
        val editTexts = listOf(itemName, itemPrice, itemLocation)
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var et1 = itemName.text.toString().trim()
                    var et2 = itemPrice.text.toString().trim()
                    var et3 = itemLocation.text.toString().trim()

                    publishListing.isEnabled = et1.isNotEmpty() && et2.isNotEmpty() && et3.isNotEmpty()
                    publishListing.isClickable = et1.isNotEmpty() && et2.isNotEmpty() && et3.isNotEmpty()
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int) {
                }

                override fun afterTextChanged(
                    s: Editable) {
                }
            })
        }
    }

    fun publishNewListingOnClick (view: View) {
        // getting values
        newListingName = itemName.text.toString()
        newListingPrice = itemPrice.text.toString()
        newListingLocation = itemLocation.text.toString()
        newListingDesc = itemDesc.text.toString()

        // pushing to listings table
        val itemId = database.push().key!!
        val item = ListingItemsModel(itemId, newListingName, newListingPrice, newListingLocation, newListingDesc)
        database.child(itemId).setValue(item).addOnCompleteListener{
            Toast.makeText(this, "Listing created successfully!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener{ err->
            Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    fun cancelListingOnClick(view: View){
        finish()
    }
}