package com.hungngo.groceryappkotlin.activity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import kotlin.collections.HashMap

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        tv_forget_pw.setOnClickListener(this)
        tv_register.setOnClickListener(this)
        btn_login.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.tv_forget_pw -> {
                val intent = Intent(this, ForgetPasswordActivity::class.java)
                startActivity(intent)
            }
            R.id.tv_register -> {
                val intent = Intent(this, RegisterUserActivity::class.java)
                startActivity(intent)
            }
            R.id.btn_login -> {
                loginUser()
            }

        }
    }

    private fun loginUser() {
        progressDialog.setMessage("Logging in...")
        progressDialog.show()

        val email = edt_login_email.text.toString().trim()
        val password = edt_login_password.text.toString().trim()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email pattern", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT)
                .show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                makeUserOnline()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun makeUserOnline() {
        // after logged, make user online
        val map = HashMap<String, Any>()
        map["online"] = true

        // update value to db
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Users").child(mAuth.uid!!).updateChildren(map)
            .addOnSuccessListener {
                // check user type to go to the main activity
                checkUserType()
            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserType() {
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Users").orderByChild("uid").equalTo(mAuth.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val userType = ds.child("userType").value
                        // check type of user: seller or buyer
                        if (userType == MyCommon.SELLER) {
                            progressDialog.dismiss()
                            Intent(this@LoginActivity, MainSellerActivity::class.java).also {
                                startActivity(it)
                            }
                            finish()
                        } else {
                            progressDialog.dismiss()
                            Intent(this@LoginActivity, MainBuyerActivity::class.java).also {
                                startActivity(it)
                            }
                            finish()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }
}