package com.hungngo.groceryappkotlin.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R

class SplashActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //make full screen
        window.requestFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_splash)

        mAuth = FirebaseAuth.getInstance()

        // start login activity after 1sec
        Handler().postDelayed(Runnable {
            val user: FirebaseUser? = mAuth.currentUser
            if (user == null) {
                // user not logged in, start Login activity
                Intent(this, LoginActivity::class.java).also {
                    startActivity(it)
                }
            } else {
                // user logged in
                checkUserType()
            }
        }, 1000)
    }

    private fun checkUserType() {
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Users").child(mAuth.uid.toString())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userType = snapshot.child("userType").value
                    // check type of user: seller or buyer
                    if (userType == MyCommon.SELLER) {
                        Intent(this@SplashActivity, MainSellerActivity::class.java).also {
                            startActivity(it)
                        }
                        finish()
                    } else {
                        Intent(this@SplashActivity, MainBuyerActivity::class.java).also {
                            startActivity(it)
                        }
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}
