package com.example.hanger

import android.content.Intent
import android.os.Bundle
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

class ListingsActivity: AppCompatActivity() {
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var mDrawerLayout: DrawerLayout
    lateinit var mNavView: NavigationView

    private lateinit var textViewCategory: TextView
    private lateinit var recyclerViewListings: RecyclerView
    private lateinit var listings: ArrayList<ListingItemsModel>
    private lateinit var db: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listings)

        mDrawerLayout = findViewById(R.id.drawerLayout);
        mNavView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close)
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // hamburger menu
        mNavView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.profile -> {
                    var intent: Intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                }
                R.id.browseListings -> {
                    var intent: Intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.myListings -> {
                    var intent: Intent = Intent(this, ViewMyListingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.logout -> {
                    FirebaseAuth.getInstance().signOut();
                    var intent: Intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

        textViewCategory = findViewById(R.id.textViewListings)
        textViewCategory.text = intent.getStringExtra("CategoryName")

        recyclerViewListings = findViewById(R.id.recyclerViewListings)
        recyclerViewListings.layoutManager = LinearLayoutManager(this)
        recyclerViewListings.setHasFixedSize(true)

        listings = ArrayList()
        fetchListings()
    }

    private fun fetchListings() {
        val category = intent.getIntExtra("CategoryId", 0)
        println("debug: category is $category")
        db = Firebase.database.reference.child("Listings")
        val categoryQuery = db.orderByChild("itemCategory").equalTo(category.toDouble())
        categoryQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("debug: snapshot is $snapshot")
                listings.clear()
                for (data in snapshot.children) {
                    val listing = data.getValue<ListingItemsModel>()
                    listings.add(listing!!)
                }
                val listingAdapter = ListingAdapter(listings)
                recyclerViewListings.adapter = listingAdapter
                listingAdapter.setOnItemClickListener(object : ListingAdapter.onItemClickListener{
                    override fun onCardClicked(position: Int) {

                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }
}