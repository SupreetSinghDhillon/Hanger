package com.example.hanger

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.hanger.model.ListingItemsModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private lateinit var textViewStatus: TextView

    private lateinit var btnContact: Button
    private lateinit var btnUpdateListing: Button
    private lateinit var btnCancelEditListing: Button
    private lateinit var btnDeleteListing: Button
    private lateinit var btnUpdateListingPicture: Button

    private var isEditing: Boolean = false

    private var listingIsActive: Boolean = true
    private lateinit var database: DatabaseReference
    val arrayOfCategory = arrayOf("Casual", "Prom", "Suits", "Wedding", "Ethnic Wear", "Other" )
    // private lateinit var updateListing: Button
    // private lateinit var database: DatabaseReference
    private lateinit var userId: String


    // TODO: update the database on update button
    // TODO: link the image
    // TODO: delete listing on click of delete button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_my_listing)

        //get db
        database = FirebaseDatabase.getInstance().getReference("Listings")
        itemId = intent.getStringExtra("itemId")!!
        isEditing = intent.getBooleanExtra("editing", false)

        textViewStatus = findViewById(R.id.textViewStatus)

        itemName = findViewById(R.id.editListingName)
        itemPrice = findViewById(R.id.editListingPrice)
        itemLocation = findViewById(R.id.editListingLocation)
        itemDesc = findViewById(R.id.editListingDescription)
        itemInactive = findViewById(R.id.radioInactive)
        itemIsActive = findViewById(R.id.radioActive)
        itemCategorySpinner = findViewById(R.id.editListingCategories)
        itemPicture = findViewById(R.id.editItemPicture)

        btnContact = findViewById(R.id.buttonContact)
        btnUpdateListing = findViewById(R.id.buttonUpdateListing)
        btnCancelEditListing = findViewById(R.id.buttonCancelEditListing)
        btnDeleteListing = findViewById(R.id.buttonDeleteListing)
        btnUpdateListingPicture = findViewById(R.id.buttonUpdateListingPicture)

        if (!isEditing) {
            itemName.isEnabled = false
            itemPrice.isEnabled = false
            itemLocation.isEnabled = false
            itemDesc.isEnabled = false
            itemInactive.visibility = View.INVISIBLE
            itemIsActive.visibility = View.INVISIBLE
            itemCategorySpinner.isEnabled = false
//            itemPicture.isEnabled = false
            textViewStatus.visibility = View.INVISIBLE

            btnUpdateListingPicture.visibility = View.INVISIBLE
            btnContact.visibility = View.VISIBLE
            btnUpdateListing.visibility = View.INVISIBLE
            btnCancelEditListing.visibility = View.INVISIBLE
            btnDeleteListing.visibility = View.INVISIBLE
        } else {
            itemName.isEnabled = true
            itemPrice.isEnabled = true
            itemLocation.isEnabled = true
            itemDesc.isEnabled = true
            itemInactive.visibility = View.VISIBLE
            itemIsActive.visibility = View.VISIBLE
            itemCategorySpinner.isEnabled = true
            textViewStatus.visibility = View.VISIBLE

            btnUpdateListingPicture.visibility = View.VISIBLE
            btnContact.visibility = View.INVISIBLE
            btnUpdateListing.visibility = View.VISIBLE
            btnCancelEditListing.visibility = View.VISIBLE
            btnDeleteListing.visibility = View.VISIBLE
        }

        setOriginalValuesToFields()
        btnContact.setOnClickListener {
            val intent: Intent = Intent(this, MessageActivity::class.java)
            intent.putExtra("userid", userId)
            this.startActivity(intent)
        }
    }

    private fun setOriginalValuesToFields () {
        println("debug"+intent.getStringExtra("itemName"))

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
        userId = intent.getStringExtra("userId")!!
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
        // getting values
        var newListingName = itemName.text.toString()
        var newListingPrice = itemPrice.text.toString()
        var newListingLocation = itemLocation.text.toString()
        var newListingDesc = itemDesc.text.toString()
        var newListingCategory = itemCategorySpinner.selectedItemPosition
        if (itemIsActive.isChecked){
            listingIsActive = true
        } else if (itemInactive.isChecked){
            listingIsActive = false
        }
        // update every field (assume everything is changed)
        database.child(itemId).child("itemName").setValue(newListingName);
        database.child(itemId).child("itemPrice").setValue(newListingPrice);
        database.child(itemId).child("itemLocation").setValue(newListingLocation);
        database.child(itemId).child("itemCategory").setValue(newListingCategory);
        database.child(itemId).child("itemDesc").setValue(newListingDesc);
        database.child(itemId).child("active").setValue(listingIsActive);

        finish()
    }

    fun deleteListingOnClick (view: View) {
        database.child(itemId).removeValue()
        finish()
    }

    fun cancelEditListingOnClick (view: View) {
        finish()
    }
}