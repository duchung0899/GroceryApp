package com.hungngo.groceryappkotlin.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.`interface`.IOnClickGoToCartFragment
import com.hungngo.groceryappkotlin.fragment.CartFragment
import com.hungngo.groceryappkotlin.fragment.HomeFragment
import com.hungngo.groceryappkotlin.fragment.MessageFragment
import com.hungngo.groceryappkotlin.fragment.ProfileFragment
import com.hungngo.groceryappkotlin.model.User
import kotlinx.android.synthetic.main.activity_main_buyer.*
import kotlinx.android.synthetic.main.activity_main_seller.*

class MainBuyerActivity : AppCompatActivity(), View.OnClickListener, IOnClickGoToCartFragment {

    private var currentUser: User? = null

    private lateinit var mAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_buyer)

        img_buyer_logout.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()

        checkUser()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNavigationView.setOnItemSelectedListener(onNav)

        loadFragment(HomeFragment.newInstance())
    }

    private val onNav = BottomNavigationView.OnNavigationItemSelectedListener {
        val bundle = Bundle()
        bundle.putSerializable("USER", currentUser)

        when (it.itemId) {
            R.id.nav_home -> {
                val homeFragment = HomeFragment()
                homeFragment.arguments = bundle
                loadFragment(homeFragment)
            }
            R.id.nav_cart -> {
                val cartFragment = CartFragment()
                cartFragment.arguments = bundle
                loadFragment(cartFragment)
            }
            R.id.nav_profile -> {
                val profileFragment = ProfileFragment()
                profileFragment.arguments = bundle
                loadFragment(profileFragment)
            }
            R.id.nav_message -> {
                val messageFragment = MessageFragment()
                messageFragment.arguments = bundle
                loadFragment(messageFragment)
            }
        }

        return@OnNavigationItemSelectedListener true
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment, fragment).commit()
    }

    private fun checkUser() {
        val user: FirebaseUser? = mAuth.currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            loadMyInfo()
        }
    }

    private fun loadMyInfo() {
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("Users").orderByChild("uid").equalTo(mAuth.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val user = ds.getValue(User::class.java)
                        currentUser = user
                        tv_buyer_title.text = "${user?.name} (${user?.userType})"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun makeUserOffline() {
        // after logged out, make user online
        val map = HashMap<String, Any>()
        map["online"] = false

        // update value to db
        val userRef = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.uid!!)
        userRef.updateChildren(map)
            .addOnSuccessListener {
                // check user type to go to the main activity
                mAuth.signOut()
//                startActivity(Intent(this, LoginActivity::class.java))
                checkUser()
            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_buyer_logout -> {
                makeUserOffline()
            }
        }
    }

    override fun goToCartFragment() {
        val bundle = Bundle()
        bundle.putSerializable("USER", currentUser)
        val cartFragment = CartFragment()
        cartFragment.arguments = bundle
        loadFragment(cartFragment)
    }
}