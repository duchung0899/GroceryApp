package com.hungngo.groceryappkotlin.activity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.hungngo.groceryappkotlin.R
import kotlinx.android.synthetic.main.activity_forget_password.*

class ForgetPasswordActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        mAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        img_forget_back.setOnClickListener(this)
        btn_recover.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_forget_back -> {
                onBackPressed()
            }
            R.id.btn_recover -> {
                recoverPassword()
            }
        }
    }

    private fun recoverPassword() {
        val email = edt_recover_email.text.toString().trim()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Invalid email pattenr", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.setMessage("Sending instructions to reset password...")
        progressDialog.show()
        
        mAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { 
                progressDialog.dismiss()
                Toast.makeText(this, "Password reset instructions sent to your email...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { 
                progressDialog.dismiss()
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}