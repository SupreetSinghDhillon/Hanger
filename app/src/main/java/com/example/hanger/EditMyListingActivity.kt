package com.example.hanger

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File


class EditMyListingActivity : AppCompatActivity() {
    private lateinit var itemName: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemLocation: EditText
    lateinit var storageReference: StorageReference
    private lateinit var itemDesc: EditText
    private lateinit var itemCategorySpinner: Spinner
    private lateinit var itemInactive: RadioButton
    private lateinit var itemIsActive: RadioButton
    private lateinit var itemPicture: ImageView
    private lateinit var itemId: String
    val arrayOfCategory = arrayOf("Casual", "Prom", "Suits", "Wedding", "Ethnic Wear", "Other" )
    // private lateinit var updateListing: Button
    // private lateinit var database: DatabaseReference


    // TODO: update the database on update button
    // TODO: link the image
    // TODO: delete listing on click of delete button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_listing)

        itemName = findViewById(R.id.editListingName)
        itemPrice = findViewById(R.id.editListingPrice)
        itemLocation = findViewById(R.id.editListingLocation)
        itemDesc = findViewById(R.id.editListingDescription)
        itemInactive = findViewById(R.id.radioInactive)
        itemIsActive = findViewById(R.id.radioActive)
        itemCategorySpinner = findViewById(R.id.editListingCategories)
        itemPicture = findViewById(R.id.editItemPicture)

        setOriginalValuesToFields()

    }

    private fun setOriginalValuesToFields () {
        println("debug"+intent.getStringExtra("itemName"))
        itemId = intent.getStringExtra("itemId")!!
        itemName.setText(intent.getStringExtra("itemName"))
        itemPrice.setText(intent.getStringExtra("itemPrice"))
        itemLocation.setText(intent.getStringExtra("itemLocation"))
        itemDesc.setText(intent.getStringExtra("itemDesc"))
        println("debug received: "+intent.getIntExtra("itemCategory", 0))
        val spinnerPos = intent.getIntExtra("itemCategory", 0)
        itemCategorySpinner.setSelection(spinnerPos)
        if (intent.getBooleanExtra("itemActive", true)){
            itemIsActive.isChecked = true
        } else {
            itemInactive.isChecked = true
        }
        getItemPicture()
    }

    private fun getItemPicture() {
        storageReference = FirebaseStorage.getInstance().reference.child("Item Images").child(itemId)
        val localFile = File.createTempFile("temporaryImage","jpg")
        storageReference.getFile(localFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
            itemPicture.setImageBitmap(bitmap)
        }
            .addOnFailureListener{
                Toast.makeText(this,"Image upload failed",Toast.LENGTH_SHORT).show()
            }
    }

    private fun setNewValuesToUpdate () {

    }

    fun updateListingOnClick (view: View) {
        // unfinished
        finish()
    }

    fun cancelEditListingOnClick (view: View) {
        // unfinished
        finish()
    }
}