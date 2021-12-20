package com.hungngo.groceryappkotlin.fragment.seller

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.LoginActivity
import com.hungngo.groceryappkotlin.model.User
import kotlinx.android.synthetic.main.fragment_seller_profile.*
import kotlinx.android.synthetic.main.fragment_seller_profile.view.*


class SellerProfileFragment : Fragment(), View.OnClickListener {

    private var mContext: Context? = null

    private lateinit var tvSellerTitle: TextView
    private lateinit var tvSellerPhone: TextView
    private lateinit var imgSellerProfile: ImageView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var progressdialog: ProgressDialog

    private var shop: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_seller_profile, container, false)

        tvSellerTitle = view.findViewById(R.id.tv_seller_title)
        tvSellerPhone = view.findViewById(R.id.tv_seller_phone)
        imgSellerProfile = view.findViewById(R.id.img_profile_seller)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.img_seller_logout.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()
        progressdialog = ProgressDialog(requireContext())
        progressdialog.setTitle("Please wait...")
        progressdialog.setCanceledOnTouchOutside(false)

        loadMyInfo()
    }

    private fun loadMyInfo() {
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("Users").orderByChild("uid").equalTo(mAuth.uid)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val user = ds.getValue(User::class.java)
                        shop = user
                        tvSellerTitle.text = user?.name
                        tvSellerPhone.text = user?.phone

                        if (user?.profileImage != null) {
                            Glide.with(mContext!!)
                                .load(user.profileImage).into(imgSellerProfile)
                        }
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
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("Users").child(mAuth.uid!!).updateChildren(map)
            .addOnSuccessListener {
                // check user type to go to the main activity
                mAuth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
            }
            .addOnFailureListener {
                progressdialog.dismiss()
                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.img_seller_logout -> {
                makeUserOffline()
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (mContext == null)
            mContext = context.applicationContext
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SellerProfileFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}