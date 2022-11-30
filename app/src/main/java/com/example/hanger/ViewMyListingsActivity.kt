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
    private lateinit var listOfActiveItemsRecyclerView: RecyclerView
    private lateinit var listOfInactiveItemsRecyclerView: RecyclerView
    private lateinit var  activeItemList: ArrayList<ListingItemsModel>
    private lateinit var  inactiveItemList: ArrayList<ListingItemsModel>
    private lateinit var database: DatabaseReference
    lateinit var auth: FirebaseAuth
    private lateinit var loggedInUserId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_my_listings)
        auth = FirebaseAuth.getInstance()
        loggedInUserId = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Listings")

//        mDrawerLayout = findViewById(R.id.drawerLayout);
//        mNavView = findViewById(R.id.navView)

//        toggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close)
//        mDrawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

//        // hamburger menu
//        mNavView.setNavigationItemSelectedListener {
//
//            when (it.itemId) {
//                R.id.profile -> {
//                    var intent: Intent = Intent(this, ProfileActivity::class.java)
//                    startActivity(intent)
//                }
//                R.id.browseListings -> {
//                    var intent: Intent = Intent(this, MainActivity::class.java)
//                    startActivity(intent)
//                }
//                R.id.myListings -> {
//                    var intent: Intent = Intent(this, ViewMyListingsActivity::class.java)
//                    startActivity(intent)
//                }
//                R.id.messages -> {
//                    var intent: Intent = Intent(this, MessagesActivity::class.java)
//                    startActivity(intent)
//                }
//                R.id.logout -> {
//                    FirebaseAuth.getInstance().signOut();
//                    var intent: Intent = Intent(this, LogInActivity::class.java)
//                    startActivity(intent)
//                }
//            }
//            true
//        }

        // fetching data
        listOfActiveItemsRecyclerView = findViewById(R.id.myListingsItems)
        listOfActiveItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        listOfActiveItemsRecyclerView.setHasFixedSize(true)

        // fetching data for inactive
        listOfInactiveItemsRecyclerView = findViewById(R.id.myInactiveListingsItems)
        listOfInactiveItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        listOfInactiveItemsRecyclerView.setHasFixedSize(true)

        activeItemList = arrayListOf<ListingItemsModel>()
        inactiveItemList = arrayListOf<ListingItemsModel>()
        fetchListings()

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


    private fun fetchListings () {
        val context = this
        var queryGetInactiveMyListings = database.orderByChild("userId").equalTo(loggedInUserId)
        queryGetInactiveMyListings.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                activeItemList.clear()
                inactiveItemList.clear()
                // calling this again as the corner case of last item being deleted
                val itemAdapter = ListingAdapter(activeItemList, context) // active listings adapter
                val itemAdapter2 = ListingAdapter(inactiveItemList, context) // inactive listings adapter
                listOfActiveItemsRecyclerView.adapter = itemAdapter
                listOfInactiveItemsRecyclerView.adapter = itemAdapter2
                if (snapshot.exists()){ // if data exists
                    for (itemSnap in snapshot.children){
                        val itemData = itemSnap.getValue(ListingItemsModel:: class.java)
                        if (itemData?.isActive == true) {
                            activeItemList.add(itemData!!)
                        } else {
                            inactiveItemList.add(itemData!!)
                        }

                    }
                    // call again for adding items
                    val itemAdapter = ListingAdapter(activeItemList, context) // active listings adapter
                    val itemAdapter2 = ListingAdapter(inactiveItemList, context) // inactive listings adapter
                    listOfActiveItemsRecyclerView.adapter = itemAdapter
                    listOfInactiveItemsRecyclerView.adapter = itemAdapter2

                    itemAdapter.setOnItemClickListener(object : ListingAdapter.onItemClickListener{
                        override fun onCardClicked(position: Int) {
                            val myIntent = Intent(this@ViewMyListingsActivity, EditMyListingActivity::class.java)
                            // not passing in the ID and grabbing it from database in EditMyListingsActivity as this is easier
                            // but we can always change it later for a more efficient code
                            myIntent.putExtra("editing", true)

                            // TODO: missing put image
                            println("debug: first"+activeItemList[position].itemName)
                            myIntent.putExtra("userId", activeItemList[position].userId)
                            myIntent.putExtra("itemId",activeItemList[position].itemId)
                            myIntent.putExtra("itemName", activeItemList[position].itemName)
                            myIntent.putExtra("itemPrice", activeItemList[position].itemPrice)
                            myIntent.putExtra("itemLocation", activeItemList[position].itemLocation)
                            myIntent.putExtra("itemCategory", activeItemList[position].itemCategory)
                            myIntent.putExtra("itemDesc", activeItemList[position].itemDesc)
                            myIntent.putExtra("itemActive", activeItemList[position].isActive)
                            startActivity(myIntent)
                        }
                    })

                    itemAdapter2.setOnItemClickListener(object : ListingAdapter.onItemClickListener{
                        override fun onCardClicked(position: Int) {
                            val myIntent = Intent(this@ViewMyListingsActivity, EditMyListingActivity::class.java)
                            // not passing in the ID and grabbing it from database in EditMyListingsActivity as this is easier
                            // but we can always change it later for a more efficient code
                            myIntent.putExtra("editing", true)
                            println("debug: first"+inactiveItemList[position].itemName)
                            myIntent.putExtra("userId", inactiveItemList[position].userId)
                            myIntent.putExtra("itemId",inactiveItemList[position].itemId)
                            myIntent.putExtra("itemName", inactiveItemList[position].itemName)
                            myIntent.putExtra("itemPrice", inactiveItemList[position].itemPrice)
                            myIntent.putExtra("itemLocation", inactiveItemList[position].itemLocation)
                            myIntent.putExtra("itemCategory", inactiveItemList[position].itemCategory)
                            myIntent.putExtra("itemDesc", inactiveItemList[position].itemDesc)
                            myIntent.putExtra("itemActive", inactiveItemList[position].isActive)
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