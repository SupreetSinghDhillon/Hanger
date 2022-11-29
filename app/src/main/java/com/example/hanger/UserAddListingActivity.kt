package com.example.hanger

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.FileUtils
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.hanger.adapters.Util
import com.example.hanger.model.ListingItemsModel
import com.example.hanger.model.MyViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import kotlin.concurrent.thread


class UserAddListingActivity : AppCompatActivity() {
    private lateinit var itemName: EditText
    private lateinit var itemPrice: EditText
    private lateinit var itemLocation: EditText
    private lateinit var itemDesc: EditText
    private lateinit var itemId: String
    private lateinit var publishListing: Button
    private lateinit var database: DatabaseReference
    private lateinit var newListingName: String
    private lateinit var newListingPrice: String
    private lateinit var newListingOwnerId: String
    private lateinit var auth: FirebaseAuth
    lateinit var tempUri: Uri
    private lateinit var newListingLocation: String
    private var newListingCategory: Int = 0
    var newListingDesc: String? = "This user has not added any description."
    private var recentImageUri: Uri? = null
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var myViewModel: MyViewModel
    private lateinit var itemPicture: ImageView
    lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var inputCategorySpinner: Spinner
    private lateinit var firebaseStorage: FirebaseStorage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_add_listing)
        supportActionBar?.hide()
        itemName = findViewById(R.id.editListingName)
        itemPrice = findViewById(R.id.editListingPrice)
        itemLocation = findViewById(R.id.editListingLocation)
        itemDesc = findViewById(R.id.editListingDescription)
        publishListing = findViewById(R.id.buttonPublishListing)
        itemPicture = findViewById(R.id.newItemPicture)
        inputCategorySpinner = findViewById(R.id.editListingCategories)
        // to do: push logged in user's account onto the table as well so the buyer can contact them
        database = FirebaseDatabase.getInstance().getReference("Listings")
        auth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

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
        val tempImgFile = File(getExternalFilesDir(null), "temp_image.jpg")
        tempUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID, tempImgFile)

        //renders image
        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.listingImage.observe(this) { it ->
            itemPicture.setImageBitmap(it)
        }

        //image
        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode === RESULT_OK) {
                val img = result.data
                this.contentResolver.takePersistableUriPermission(
                    img?.data!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                if (img != null && img.data != null) {
                    recentImageUri = img.data
                    val bitmap = Util.getBitmap(this, recentImageUri!!)
                    myViewModel.listingImage.value = bitmap
                }
            }
        }
        cameraLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { it: ActivityResult ->
            if (it.resultCode == Activity.RESULT_OK) {
                val imageBitmap = com.xd.camerademokotlin.Util.getBitmap(this, tempUri)
                // finalImageUri = tempUri.toString()
                itemPicture.setImageBitmap(imageBitmap)
            }
        }

        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) {
            if (it != null) {
                itemPicture.setImageURI(it)

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

    fun publishNewListingOnClick (view: View) {
        // getting values
        newListingName = itemName.text.toString()
        newListingPrice = itemPrice.text.toString()
        newListingLocation = itemLocation.text.toString()
        newListingDesc = itemDesc.text.toString()
        newListingCategory = inputCategorySpinner.selectedItemPosition
        newListingOwnerId = auth.currentUser?.uid.toString()

        // pushing to listings table
        //val itemId = database.push().key!!
        itemId = database.push().key!!
        val item = ListingItemsModel(newListingOwnerId, itemId, newListingName, newListingPrice, newListingLocation, newListingDesc, newListingCategory, true)
        database.child(itemId).setValue(item).addOnCompleteListener{
            uploadItemPic()
            Toast.makeText(this, "Listing created successfully!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener{ err->
            Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
        }
        finish()
    }

    private fun uploadItemPic() {
        val reference = firebaseStorage.reference.child("Item Images").child(itemId)
        reference.putFile(tempUri)
    }


    fun cancelListingOnClick(view: View){
        finish()
    }


    fun openPictureSelectionOnClick (view: View) {

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
}