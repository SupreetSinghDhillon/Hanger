package com.example.hanger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var toggle:ActionBarDrawerToggle

    lateinit var mDrawerLayout: DrawerLayout

    lateinit var mNavView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDrawerLayout = findViewById(R.id.drawerLayout);
        mNavView = findViewById(R.id.navView)

        toggle = ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close)
        mDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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

}