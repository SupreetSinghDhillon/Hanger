package com.example.hanger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.adapters.ListingAdapter
import com.example.hanger.model.ListingItemsModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class ListingsActivity: AppCompatActivity(), AdapterView.OnItemSelectedListener {
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var mDrawerLayout: DrawerLayout
    lateinit var mNavView: NavigationView

    private lateinit var spinnerFilters: Spinner
    private lateinit var selectedFilter: String

    private lateinit var listingsContext: Context
    private lateinit var textViewCategory: TextView
    private lateinit var recyclerViewListings: RecyclerView
    private lateinit var listings: ArrayList<ListingItemsModel>
    private lateinit var dateOrderedListings: ArrayList<ListingItemsModel>
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

        recyclerViewListings = findViewById(R.id.recyclerViewListings)
        recyclerViewListings.layoutManager = LinearLayoutManager(this)
        recyclerViewListings.setHasFixedSize(true)

        listings = ArrayList()
        fetchListings()
    }

    // for filters
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
//        println("debug: selected spinner pos $pos")
        selectedFilter = parent?.getItemAtPosition(pos).toString()
//        println("debug: selected spinner text $selectedFilter")
        fetchListings()
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }

    private fun fetchListings() {
        val category = intent.getIntExtra("CategoryId", 0)
        println("debug: category is $category")
        db = Firebase.database.reference.child("Listings")
        val categoryQuery = db.orderByChild("itemCategory").equalTo(category.toDouble())
        categoryQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
//                println("debug: snapshot is $snapshot")
                listings.clear()
                for (data in snapshot.children) {
                    val listing = data.getValue<ListingItemsModel>()
                    listings.add(listing!!)
                }
                dateOrderedListings = ArrayList(listings.map{it.copy()})
                if (selectedFilter == "Price: Low to High") {
                    listings.sortWith(compareBy<ListingItemsModel> {
                        it.itemPrice?.toDouble()
                    })
                } else if (selectedFilter == "Price: High to Low") {
                    listings.sortWith(compareBy<ListingItemsModel> {
                        it.itemPrice?.toDouble()   
                    })
                    listings.reverse()
                }

                val listingAdapter = ListingAdapter(listings)
                recyclerViewListings.adapter = listingAdapter
                listingAdapter.setOnItemClickListener(object : ListingAdapter.onItemClickListener {
                    override fun onCardClicked(position: Int) {
                        val intent = Intent(listingsContext, EditMyListingActivity::class.java)
                        intent.putExtra("editing", false)

                        intent.putExtra("itemId", listings[position].itemId)
                        intent.putExtra("itemName", listings[position].itemName)
                        intent.putExtra("itemPrice", listings[position].itemPrice)
                        intent.putExtra("itemLocation", listings[position].itemLocation)
                        intent.putExtra("itemCategory", listings[position].itemCategory)
                        intent.putExtra("itemDesc", listings[position].itemDesc)
                        intent.putExtra("itemActive", listings[position].isActive)
                        // TODO: missing put image
                        startActivity(intent)
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
}