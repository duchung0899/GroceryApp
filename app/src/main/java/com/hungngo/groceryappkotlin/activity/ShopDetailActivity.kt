package com.hungngo.groceryappkotlin.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.`interface`.IOnClickProductAdapter
import com.hungngo.groceryappkotlin.adapter.ProductAdapter
import com.hungngo.groceryappkotlin.model.Product
import com.hungngo.groceryappkotlin.model.User
import kotlinx.android.synthetic.main.activity_shop_detail.*

@SuppressLint("SetTextI18n")
class ShopDetailActivity : AppCompatActivity(), View.OnClickListener, IOnClickProductAdapter {
    private var shopLatitude: Double? = 0.0
    private var shopLongitude: Double? = 0.0
    private var userLatitude: Double? = 0.0
    private var userLongitude: Double? = 0.0

    private lateinit var mAuth: FirebaseAuth

    private lateinit var productAdapter: ProductAdapter
    private val listProduct = mutableListOf<Product>()

    private var shop: User? = null
    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shop_detail)

        img_filter_shop.setOnClickListener(this)
        img_call.setOnClickListener(this)
        img_map.setOnClickListener(this)
        tv_chat_shop.setOnClickListener(this)
        tv_follow_shop.setOnClickListener(this)

        mAuth = FirebaseAuth.getInstance()

        // get current User to get userLatitude and userLongitude
        getCurrentUser()

        // load shop's information
        loadShopInfo()

        // load follow
        checkFollow()

        // load all products of shop
        productAdapter = ProductAdapter(this, listProduct, this)
        rcv_product_inshop.layoutManager = GridLayoutManager(this, 2)
        rcv_product_inshop.adapter = productAdapter
        getProductOfShop()

        // search in shop
        edt_search_in_shop.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    productAdapter.filter.filter(s)
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
        Log.d("TAG", "onCreate: $userLatitude, $userLongitude")
    }

    private fun checkFollow() {
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("Follow").child(shop?.uid!!).addValueEventListener(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(user?.uid!!).exists() && user != null && user?.uid != null) {
                    if (tv_follow_shop != null){
                        tv_follow_shop.tag = "followed"
                        tv_follow_shop.text = "Followed"
                        tv_follow_shop.background =
                            ContextCompat.getDrawable(applicationContext, R.drawable.bg_reg_7)
                        tv_follow_shop.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_check_green, 0, 0, 0)
                    }
                } else {
                    if (tv_follow_shop != null){
                        tv_follow_shop.tag = "not follow"
                        tv_follow_shop.text = "Follow"
                        tv_follow_shop.background =
                            ContextCompat.getDrawable(applicationContext, R.drawable.bg_reg_6)
                        tv_follow_shop.setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_follow_white, 0, 0, 0)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun getCurrentUser() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("uid").equalTo(mAuth.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    user = ds.getValue(User::class.java)
                    userLatitude = user!!.latitude
                    userLongitude = user!!.longitude
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    private fun loadShopInfo() {
        val intent = intent
        shop = intent.getSerializableExtra("SHOP_INFO") as User?
        if (shop != null) {
           setShopInfo()
        }
//        else{
//            val shopId = intent.getStringExtra("shop_id")
//            if (shopId != null){
//                FirebaseDatabase.getInstance().getReference("Users")
//                    .child(shopId).addListenerForSingleValueEvent(object : ValueEventListener{
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            shop = snapshot.getValue(User::class.java)
//                            setShopInfo()
//                        }
//                        override fun onCancelled(error: DatabaseError) {
//                        }
//                    })
//            }
//        }
    }

    private fun setShopInfo(){
        shopLatitude = shop!!.latitude!!
        shopLongitude = shop!!.longitude!!

        tv_detail_shop_name.text = shop!!.shopName
        tv_detail_shop_phone.text = shop!!.phone.toString()
        tv_detail_shop_address.text = shop!!.address
        tv_detail_shop_delivery.text = "Delivery Fee: $${shop!!.deliveryFee}"

        if (shop!!.profileImage != null)
            Glide.with(this).load(shop!!.profileImage).into(img_shop_detail)
        else
            img_shop_detail.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.gray04))

        if (shop!!.shopOpen!!){
            tv_detail_shop_open.text = "OPEN"
        }else{
            tv_detail_shop_open.text = "CLOSE"
        }
    }

    private fun getProductOfShop() {
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(shop!!.uid!!)
        ref.child("Products").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listProduct.clear()
                for (ds in snapshot.children) {
                    val product = ds.getValue(Product::class.java)
                    listProduct.add(product!!)
                }
                productAdapter.setProduct(listProduct)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun showCategory() {
        AlertDialog.Builder(this).apply {
            setTitle("Category list")
            setItems(MyCommon.productCategory1) { _, which ->
                val cateSelected = MyCommon.productCategory1[which]
                tv_user_category.text = cateSelected
                getProductWithCategory(cateSelected)
            }
            show()
        }
    }

    private fun getProductWithCategory(cateSelected: String) {
        val ref = FirebaseDatabase.getInstance().reference
        ref.child("Users").child(shop!!.uid!!).child("Products")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listProduct.clear()
                    for (ds in snapshot.children){
                        val product = ds.getValue(Product::class.java)
                        if (cateSelected == "All"){
                            listProduct.add(product!!)
                        }else{
                            if (product!!.productCategory == cateSelected){
                                listProduct.add(product)
                            }
                        }
                    }
                    productAdapter.setProduct(listProduct)
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun followShop() {
        val ref = FirebaseDatabase.getInstance().reference.child("Follow")
        if (tv_follow_shop.tag == "followed"){
            ref.child(shop!!.uid!!).child(user!!.uid!!).removeValue()
        }else{
            ref.child(shop!!.uid!!).child(user!!.uid!!).setValue(true)
        }
    }

    private fun checkMap() {
        val address = "https://maps.google.com/maps?saddr=$userLatitude,$userLongitude" +
                "&daddr=$shopLatitude,$shopLongitude"
        Intent(Intent.ACTION_VIEW, Uri.parse(address)).apply {
            startActivity(this)
        }
    }

    private fun makeACall() {
        Intent(Intent.ACTION_DIAL).also {
            it.data = Uri.parse("tel:${Uri.encode(shop!!.phone)}")
            startActivity(it)
        }
    }

    override fun clickToProduct(product: Product) {
        Intent(this, ProductDetailActivity::class.java).also {
            it.putExtra("product_object", product)
            it.putExtra("shop_object", shop)
            startActivity(it)
        }
    }

    private fun openChatLog() {
        Intent(this, ChatLogActivity::class.java).apply {
            this.putExtra("shop_object", shop)
            val roomId = MyCommon.generateChatRoomId(user!!.uid!!, shop!!.uid!!)
            MyCommon.roomSelected = roomId
            startActivity(this)
            // register topic
//            FirebaseMessaging.getInstance().subscribeToTopic(MyCommon.FCM_TOPIC_MESSAGE)
//                .addOnSuccessListener {
//                    startActivity(this)
//                }
        }
    }



    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.img_filter_shop -> {
                showCategory()
            }
            R.id.img_call -> {
                makeACall()
            }
            R.id.img_map -> {
                checkMap()
            }
            R.id.tv_follow_shop -> {
                followShop()
            }
            R.id.tv_chat_shop ->{
                openChatLog()
            }
        }
    }
}