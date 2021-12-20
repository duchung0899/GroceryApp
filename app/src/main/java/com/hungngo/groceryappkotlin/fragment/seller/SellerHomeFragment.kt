package com.hungngo.groceryappkotlin.fragment.seller

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.AddProductActivity
import com.hungngo.groceryappkotlin.adapter.SellerProductAdapter
import com.hungngo.groceryappkotlin.model.Product
import kotlinx.android.synthetic.main.fragment_seller_home.*
import kotlinx.android.synthetic.main.fragment_seller_home.view.*


class SellerHomeFragment : Fragment(), View.OnClickListener {
    // adapter
    private lateinit var productAdapter: SellerProductAdapter
    private var listProduct = mutableListOf<Product>()

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seller_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToTopic(MyCommon.FCM_TOPIC_MESSAGE)
//        subscribeToTopic(MyCommon.FCM_TOPIC_ORDER)
        mAuth = FirebaseAuth.getInstance()

        view.img_seller_filter.setOnClickListener(this)
        view.fab_add_product.setOnClickListener(this)

        productAdapter = SellerProductAdapter(requireContext(), listProduct)
        view.rcv_seller_product.adapter = productAdapter
        view.rcv_seller_product.layoutManager = LinearLayoutManager(requireContext())

        loadMyProduct()

        // search
        view.edt_search_product_seller.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    productAdapter.filter.filter(s)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun loadMyProduct() {
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("Users").child(mAuth.uid!!).child("Products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listProduct.clear()
                    for (ds in snapshot.children) {
                        val product = ds.getValue(Product::class.java)
                        listProduct.add(product!!)
                    }
                    productAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun showCategory() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Category list")
            setItems(MyCommon.productCategory1) { _, which ->
                val cateSelected = MyCommon.productCategory1[which]
                tv_show_category.text = cateSelected
                getProductWithCategory(cateSelected)
            }
            show()
        }
    }

    private fun getProductWithCategory(cateSelected: String) {
        listProduct.clear()
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("Users").child(mAuth.uid!!).child("Products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val product = ds.getValue(Product::class.java)
                        if (cateSelected == "All") {
                            listProduct.add(product!!)
                        } else {
                            if (product!!.productCategory == cateSelected) {
                                listProduct.add(product)
                            }
                        }
                    }
                    productAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SellerHomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.img_seller_filter -> {
                showCategory()
            }
            R.id.fab_add_product -> {
                startActivity(Intent(requireContext(), AddProductActivity::class.java))
            }
        }
    }
}