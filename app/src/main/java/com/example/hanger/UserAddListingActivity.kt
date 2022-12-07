package com.example.hanger

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.location.Address
import android.location.Geocoder
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
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.HashMap
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
    private lateinit var newListingLatlng: String
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
    var statesHM: HashMap<String, String> = HashMap<String, String>()


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
        database = FirebaseDatabase.getInstance().getReference("Listings")
        auth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()

        createStatesAbb(statesHM) // for retreiving the short forms of province

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
                val imageBitmap = com.example.hanger.Util.getBitmap(this, tempUri)
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

    fun selectLocationOnClick (view: View) {
        val intent: Intent = Intent(this, MapsActivity::class.java)
        this.startActivityForResult(intent, 0)
    }

    // get result from map selection
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 0) { // only map calls should give results
            val retrievedBundle: Bundle? = data?.getParcelableExtra("bundle")
            val retrievedLatLng: LatLng? = retrievedBundle?.getParcelable("selectedLocation")
            if (retrievedLatLng != null) {
                convertToAddress(retrievedLatLng)
            }
            val lat = retrievedLatLng?.latitude
            val lng = retrievedLatLng?.longitude
            newListingLatlng = "$lat,$lng"
        }
    }

    fun convertToAddress(retrievedLatLng: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(retrievedLatLng!!.latitude, retrievedLatLng!!.longitude, 1)
        println("debug: $addresses")
        var cityName = ""
        if (addresses[0].locality != null) cityName = addresses[0].locality
        var stateName = ""
        if (addresses[0].adminArea != null) stateName = addresses[0].adminArea
        val countryName: String? = addresses[0].countryCode
        // only assign abbrev if not null in dictionary
        if (statesHM.get(stateName) != null)  stateName = statesHM.get(stateName).toString()
        println("debug: returned value is "+retrievedLatLng)
        println("debug: display is $cityName, $stateName, $countryName")
        itemLocation.setText("$cityName, $stateName, $countryName")
        newListingLocation = "$cityName, $stateName, $countryName"
        return newListingLocation
    }

    fun publishNewListingOnClick (view: View) {
        // getting values
        newListingName = itemName.text.toString()
        newListingPrice = itemPrice.text.toString()
        newListingDesc = itemDesc.text.toString()
        newListingCategory = inputCategorySpinner.selectedItemPosition

        // pushing to listings table
        //val itemId = database.push().key!!
        itemId = database.push().key!!
        val item = ListingItemsModel(auth.currentUser?.uid, itemId, newListingName, newListingPrice, newListingLocation, newListingLatlng, newListingDesc, newListingCategory, true)
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

    // credit https://stackoverflow.com/questions/26879882/obtaining-state-abbreviation-from-getadminarea
    fun createStatesAbb (states: HashMap<String, String>) {
        states.put("Alabama","AL");
        states.put("Alaska","AK");
        states.put("Alberta","AB");
        states.put("American Samoa","AS");
        states.put("Arizona","AZ");
        states.put("Arkansas","AR");
        states.put("Armed Forces (AE)","AE");
        states.put("Armed Forces Americas","AA");
        states.put("Armed Forces Pacific","AP");
        states.put("British Columbia","BC");
        states.put("California","CA");
        states.put("Colorado","CO");
        states.put("Connecticut","CT");
        states.put("Delaware","DE");
        states.put("District Of Columbia","DC");
        states.put("Florida","FL");
        states.put("Georgia","GA");
        states.put("Guam","GU");
        states.put("Hawaii","HI");
        states.put("Idaho","ID");
        states.put("Illinois","IL");
        states.put("Indiana","IN");
        states.put("Iowa","IA");
        states.put("Kansas","KS");
        states.put("Kentucky","KY");
        states.put("Louisiana","LA");
        states.put("Maine","ME");
        states.put("Manitoba","MB");
        states.put("Maryland","MD");
        states.put("Massachusetts","MA");
        states.put("Michigan","MI");
        states.put("Minnesota","MN");
        states.put("Mississippi","MS");
        states.put("Missouri","MO");
        states.put("Montana","MT");
        states.put("Nebraska","NE");
        states.put("Nevada","NV");
        states.put("New Brunswick","NB");
        states.put("New Hampshire","NH");
        states.put("New Jersey","NJ");
        states.put("New Mexico","NM");
        states.put("New York","NY");
        states.put("Newfoundland","NF");
        states.put("North Carolina","NC");
        states.put("North Dakota","ND");
        states.put("Northwest Territories","NT");
        states.put("Nova Scotia","NS");
        states.put("Nunavut","NU");
        states.put("Ohio","OH");
        states.put("Oklahoma","OK");
        states.put("Ontario","ON");
        states.put("Oregon","OR");
        states.put("Pennsylvania","PA");
        states.put("Prince Edward Island","PE");
        states.put("Puerto Rico","PR");
        states.put("Quebec","PQ");
        states.put("Rhode Island","RI");
        states.put("Saskatchewan","SK");
        states.put("South Carolina","SC");
        states.put("South Dakota","SD");
        states.put("Tennessee","TN");
        states.put("Texas","TX");
        states.put("Utah","UT");
        states.put("Vermont","VT");
        states.put("Virgin Islands","VI");
        states.put("Virginia","VA");
        states.put("Washington","WA");
        states.put("West Virginia","WV");
        states.put("Wisconsin","WI");
        states.put("Wyoming","WY");
        states.put("Yukon Territory","YT");
    }
}