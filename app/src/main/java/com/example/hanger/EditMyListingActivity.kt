package com.example.hanger

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.hanger.model.ListingItemsModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread


class EditMyListingActivity : AppCompatActivity() {
    private lateinit var itemName: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemLocation: EditText
    lateinit var storageReference: StorageReference
    private lateinit var itemDesc: EditText
    lateinit var tempUri: Uri
    var imageChanged: Int = 0
    var profileImageSelected = false
    private lateinit var itemCategorySpinner: Spinner
    private lateinit var itemInactive: RadioButton
    private lateinit var itemIsActive: RadioButton
    private lateinit var firebaseStorage: FirebaseStorage
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    lateinit var galleryLauncher: ActivityResultLauncher<String>
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
        firebaseStorage = FirebaseStorage.getInstance()
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

        val tempImgFile = File(getExternalFilesDir(null), "temp_image.jpg")
        tempUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, tempImgFile)

        galleryCameraLauncher()

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
        btnUpdateListingPicture.setOnClickListener {
            showDialog()
        }
    }
    private fun showDialog(){
        AlertDialog
            .Builder(this)
            .setTitle("Pick Profile Picture")
            .setPositiveButton(
                "Open Camera",
                DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    val cameraStartIntent: Intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraStartIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri)
                    cameraLauncher.launch(cameraStartIntent)
                })
            .setNegativeButton(
                "Select From Gallery",
                DialogInterface.OnClickListener { _: DialogInterface, _: Int ->
                    galleryLauncher.launch("image/*")
                })
            .create()
            .show()
    }
    private fun uploadItemPic() {
        val reference = firebaseStorage.reference.child("Item Images").child(itemId)
        reference.putFile(tempUri).addOnCompleteListener {
            finish()
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
    private fun galleryCameraLauncher(){

        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { it: ActivityResult ->
            if (it.resultCode == Activity.RESULT_OK) {
                imageChanged = 1
                val imageBitmap = Util.getBitmap(this, tempUri)
                // finalImageUri = tempUri.toString()
                itemPicture.setImageBitmap(imageBitmap)
                profileImageSelected = true
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                imageChanged = 1
                itemPicture.setImageURI(it)
                profileImageSelected = true

                thread {
                    val inputStream = contentResolver.openInputStream(it)
                    val outputStream = FileOutputStream(tempUri.path)
                    //finalImageUri = tempUri.toString()
                    if (inputStream != null) {
                        FileUtils.copy(inputStream, outputStream)
                        inputStream.close()
                        outputStream.close()
                    }
                }
            }
        }
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
        uploadItemPic()

        //finish()
    }

    fun deleteListingOnClick (view: View) {
        database.child(itemId).removeValue()
        finish()
    }

    fun cancelEditListingOnClick (view: View) {
        finish()
    }
}