package com.example.hanger

import android.net.Uri
import com.example.hanger.model.ListingItemsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

object FirebaseUtil {
    private lateinit var db: FirebaseDatabase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStorage: FirebaseStorage

    fun initializeFirebase() {
        db = FirebaseDatabase.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStorage = FirebaseStorage.getInstance()
    }

    fun addListing(
        newListingName: String,
        newListingPrice: String,
        newListingLocation: String,
        newListingDesc: String,
        newListingCategory: Int,
        isActive: Boolean,
        imageUri: Uri)
    {
        CoroutineScope(IO).launch {
            val dbListings = db.getReference("Listings")
            val listingId = dbListings.push().key!!
            val listing = ListingItemsModel(listingId, newListingName, newListingPrice, newListingLocation, newListingDesc, newListingCategory, isActive)
            dbListings.child(listingId).setValue(listing).addOnCompleteListener {
                val reference = firebaseStorage.reference.child("Item Images").child(listingId)
                reference.putFile(imageUri)
            }
        }
    }

    fun fetchListings() {

    }

}