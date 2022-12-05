package com.example.hanger

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.adapters.ListingAdapter
import com.example.hanger.model.ListingItemsModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import com.google.maps.android.SphericalUtil
import java.util.regex.Pattern

/*
    NOTES: Currently, fetchListings() is called every time a filter is applied.
    should make it so that only fetch data at the start or when something changes...
 */
class ListingsActivity: AppCompatActivity(), AdapterView.OnItemSelectedListener, LocationListener {

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager
    private lateinit var currentLatLng: LatLng

    private lateinit var seekBarLocation: SeekBar
    private lateinit var textViewDistance: TextView
    private var distanceFilter: Int = 10 // km

    private lateinit var spinnerFilters: Spinner
    private lateinit var selectedFilter: String

    private lateinit var listingsContext: Context
    private lateinit var textViewCategory: TextView
    private lateinit var recyclerViewListings: RecyclerView
    private lateinit var listings: ArrayList<ListingItemsModel>
//    private lateinit var dateOrderedListings: ArrayList<ListingItemsModel>
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listings)

        listingsContext = this
        selectedFilter = "Date"

        textViewCategory = findViewById(R.id.textViewListings)
        textViewCategory.text = intent.getStringExtra("CategoryName")

        spinnerFilters = findViewById(R.id.spinnerFilters)
        ArrayAdapter.createFromResource(
            this,
            R.array.array_filters,
            android.R.layout.simple_spinner_item
        ).also { arrayAdapter ->
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerFilters.adapter = arrayAdapter
            spinnerFilters.onItemSelectedListener = this
        }

        textViewDistance = findViewById(R.id.textViewDistance)
        seekBarLocation = findViewById(R.id.seekBarLocation)
        seekBarLocation.incrementProgressBy(5)
        seekBarLocation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > 50) {
                    textViewDistance.text = "50+ km"
                } else {
                    textViewDistance.text = "$progress+ km"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                distanceFilter = seekBar!!.progress
                fetchListings()
            }

        })

        recyclerViewListings = findViewById(R.id.recyclerViewListings)
        recyclerViewListings.layoutManager = LinearLayoutManager(this)
        recyclerViewListings.setHasFixedSize(true)

        listings = ArrayList()
        checkPermission()
    }

    // for filters
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
//        println("debug: selected spinner pos $pos")
        selectedFilter = parent?.getItemAtPosition(pos).toString()
//        println("debug: selected spinner text $selectedFilter")
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }


    private fun fetchListings() {
        val category = intent.getIntExtra("CategoryId", 0)
        println("debug: category is $category")
        db = Firebase.database.reference.child("Listings")
        val categoryQuery = db.orderByChild("itemCategory").equalTo(category.toDouble())
        val context = this
        categoryQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                println("debug: snapshot is $snapshot")

                listings.clear()
                for (data in snapshot.children) {
                    val listing = data.getValue<ListingItemsModel>()
                    // https://stackoverflow.com/questions/43927273/android-how-to-convert-string-value-of-latlng-from-firebase-back-to-latlng
                    var raw = listing!!.itemLatlng as String
                    raw = raw.replace("[", "").replace("]", "")
                    val latLngString = raw.split(",")
                    println("debug: latLngString is $latLngString")
                    val listingLatLng = LatLng(latLngString[0].toDouble(), latLngString[1].toDouble())
                    val distance = SphericalUtil.computeDistanceBetween(currentLatLng, listingLatLng) / 1000 // km
                    println("debug: distance is $distance")
                    if (listing.isActive) {
                        if (distanceFilter > 50 || distance < distanceFilter) {
                            listings.add(listing)
                        }
                    }
                }

                // other filter sorting
                if (selectedFilter == "Price: Low to High") {
                    listings.sortWith(compareBy<ListingItemsModel> {
                        it.itemPrice?.toDouble()
                    })
                } else if (selectedFilter == "Price: High to Low") {
                    listings.sortWith(compareBy<ListingItemsModel> {
                        it.itemPrice?.toDouble()
                    })
                    listings.reverse()
                } else {
                    listings.reverse()
                }

                val listingAdapter = ListingAdapter(listings, context)
                recyclerViewListings.adapter = listingAdapter
                listingAdapter.setOnItemClickListener(object : ListingAdapter.onItemClickListener {
                    override fun onCardClicked(position: Int) {
                        val intent = Intent(listingsContext, EditMyListingActivity::class.java)
                        intent.putExtra("editing", false)
                        intent.putExtra("userId", listings[position].userId)
                        intent.putExtra("itemId", listings[position].itemId)
                        intent.putExtra("itemName", listings[position].itemName)
                        intent.putExtra("itemPrice", listings[position].itemPrice)
                        intent.putExtra("itemLocation", listings[position].itemLocation)
                        intent.putExtra("itemLatlng", listings[position].itemLatlng)
                        intent.putExtra("itemCategory", listings[position].itemCategory)
                        intent.putExtra("itemDesc", listings[position].itemDesc)
                        intent.putExtra("itemActive", listings[position].isActive)
                        startActivity(intent)
                    }
                })


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else
            initLocationManager()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initLocationManager()
        }
    }

    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)

                if (location != null) {
                    onLocationChanged(location)
                }
            }
            fetchListings()
        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLatLng = LatLng(location.latitude, location.longitude)
    }
}