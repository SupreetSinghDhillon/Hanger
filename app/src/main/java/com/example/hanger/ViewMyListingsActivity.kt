package com.example.hanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hanger.adapters.ListingAdapter
import com.example.hanger.model.ListingItemsModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

// credit: https://www.youtube.com/watch?v=DW-d0kalMvU&list=PLHQRWugvckFry9Q1OT6hLNfyUizT73PwX&index=4

class ViewMyListingsActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle
    lateinit var mDrawerLayout: DrawerLayout
    lateinit var mNavView: NavigationView
    private lateinit var listOfItemsRecyclerView: RecyclerView
    private lateinit var  itemList: ArrayList<ListingItemsModel>
    private lateinit var database: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_my_listings)

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

        // fetching data
        listOfItemsRecyclerView = findViewById(R.id.myListingsItems)
        listOfItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        listOfItemsRecyclerView.setHasFixedSize(true)

        itemList = arrayListOf<ListingItemsModel>()
        getItemListData()

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun addListingOnClick (view: View){
        val myIntent = Intent(this, UserAddListingActivity::class.java)
        this.startActivity(myIntent)
    }

    private fun getItemListData () {
        database = FirebaseDatabase.getInstance().getReference("Listings")
        database.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemList.clear()
                if (snapshot.exists()){ // if data exists
                    for (itemSnap in snapshot.children){
                        val itemData = itemSnap.getValue(ListingItemsModel:: class.java)
                        itemList.add(itemData!!)
                    }
                    val itemAdapter = ListingAdapter(itemList)
                    listOfItemsRecyclerView.adapter = itemAdapter

                    itemAdapter.setOnItemClickListener(object : ListingAdapter.onItemClickListener{
                        override fun onCardClicked(position: Int) {
                            val myIntent = Intent(this@ViewMyListingsActivity, EditMyListingActivity::class.java)
                            intent.putExtra("itemId",itemList[position].itemId)
                            //put the rest of the info here
                            // put name
                            // put image
                            // put address
                            // put price
                            startActivity(myIntent)
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        }
        )
    }

}