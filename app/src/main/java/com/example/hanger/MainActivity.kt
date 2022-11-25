package com.example.hanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.GridView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.example.hanger.adapters.CategoryAdapter
import com.example.hanger.model.CategoryModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mNavView: NavigationView

    private lateinit var gridViewCategories: GridView
    private var categoryList: ArrayList<CategoryModel> = ArrayList()
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDrawerLayout = findViewById(R.id.drawerLayout);
        mNavView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close)
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        populateCategories(categoryList)
        gridViewCategories = findViewById(R.id.gridViewCategories)
        categoryAdapter = CategoryAdapter(categoryList = categoryList, context = this)
        gridViewCategories.adapter = categoryAdapter

        mNavView.setNavigationItemSelectedListener {

            when(it.itemId){
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
                R.id.messages -> {
                    var intent: Intent = Intent(this, MessagesActivity::class.java)
                    startActivity(intent)
                }
                R.id.logout ->{
                    FirebaseAuth.getInstance().signOut();
                    var intent: Intent = Intent(this, LogInActivity::class.java)
                    startActivity(intent)
                }
            }
            true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun populateCategories(categoryList: ArrayList<CategoryModel>) {
        categoryList.add(CategoryModel(0, "Casual", R.drawable.ic_casual))
        categoryList.add(CategoryModel(1, "Dresses", R.drawable.ic_dresses))
        categoryList.add(CategoryModel(2, "Suits", R.drawable.ic_suits))
        categoryList.add(CategoryModel(3, "Prom/Wedding", R.drawable.ic_promwedding))
        categoryList.add(CategoryModel(4, "Hype", R.drawable.ic_hype))
        categoryList.add(CategoryModel(5, "Ethnic", R.drawable.ic_ethnic))
    }
}