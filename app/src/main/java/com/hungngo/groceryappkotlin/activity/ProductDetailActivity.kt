package com.hungngo.groceryappkotlin.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.firebase.CartDAO
import com.hungngo.groceryappkotlin.model.CartItem
import com.hungngo.groceryappkotlin.model.Product
import com.hungngo.groceryappkotlin.model.User
import kotlinx.android.synthetic.main.activity_product_detail.*

class ProductDetailActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var ref: DatabaseReference
    private lateinit var product: Product
    private lateinit var shop: User

    private var totalPrice = 0.0
    private var unitPrice = 0.0
    private var quantity = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        ref = FirebaseDatabase.getInstance().getReference("Cart")
        mAuth = FirebaseAuth.getInstance()

        loadProductInfo()

        btn_add_to_cart.setOnClickListener(this)
        btn_chat.setOnClickListener(this)
        btn_buy_now.setOnClickListener(this)
        img_decrease.setOnClickListener(this)
        img_increase.setOnClickListener(this)

        img_decrease.isEnabled = true
    }

    @SuppressLint("SetTextI18n")
    private fun loadProductInfo() {
        shop = intent.extras!!.getSerializable("shop_object") as User
        product = intent.extras!!.getSerializable("product_object") as Product

        tv_product_detail_title.text = "${product.productTitle} - ${product.productDescription}"
        tv_product_detail_discount_note.text = product.discountNote
        tv_product_detail_quantity.text = product.productQuantity.toString()
        tv_product_detail_price.text = "$${product.productPrice}"
        tv_product_detail_discount.text = "$${product.discountPrice}"

        if (product.discountAvailable!!){
            // get unit price of product
            unitPrice = product.discountPrice!!.toDouble()
            // visible discount
            tv_product_detail_discount.visibility = View.VISIBLE
            tv_product_detail_discount.visibility = View.VISIBLE

            // set color and strike through original price
            tv_product_detail_price.paintFlags = tv_product_detail_price.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tv_product_detail_price.setTextColor(ContextCompat.getColor(applicationContext, R.color.colorGray02))
        }else{
            // get unit price of product
            unitPrice = product.productPrice!!.toDouble()
            tv_product_detail_discount.visibility = View.GONE
            tv_product_detail_discount.visibility = View.GONE

            // set color for original price
            tv_product_detail_price.setTextColor(ContextCompat.getColor(this, R.color.red))
            tv_product_detail_price.paintFlags = tv_product_detail_price.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        try{
            Glide.with(this).load(product.productIcon).into(img_product_detail)
        }catch(e: Exception){
            img_product_detail.setImageResource(R.drawable.ic_cart_gray)
        }


        // get first quantity and total price of product
        quantity = product.productQuantity!!
        totalPrice = quantity*unitPrice
        tv_product_detail_total.text = "Total: $$totalPrice"
        // set gray color for minus button if quantity = 1
        checkQuantityEqualOne()
    }

    private fun addProductToCart() {
        val cartItem = CartItem(shop.uid ,product.productId!!,
            product.productTitle!!, product.productIcon!!, unitPrice, quantity, totalPrice)
//        val ref = FirebaseDatabase.getInstance().getReference("Cart")
//        ref.child(mAuth.uid!!).child(shop.uid!!).child(product.productId.toString())
//            .setValue(cartItem).addOnCompleteListener {
//                if (it.isSuccessful){
//                    Toast.makeText(this, "Add product to cart successfully", Toast.LENGTH_SHORT).show()
//                }else{
//                    Toast.makeText(this, "Add product to cart failed", Toast.LENGTH_SHORT).show()
//                }
//            }
        CartDAO.addItemToCart(cartItem, mAuth.uid!!)
        Toast.makeText(this, "Add product to cart successfully", Toast.LENGTH_SHORT).show()
    }

    private fun checkQuantityEqualOne(){
        if (quantity == 1){
            img_decrease.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorGray02))
        }else{
            img_decrease.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.colorPrimary))
        }
    }

    private fun openChatLogActivity() {
        Intent(this, ChatLogActivity::class.java).apply {
            this.putExtra("shop_object", shop)
            val roomId = MyCommon.generateChatRoomId(mAuth.uid!!, shop.uid!!)
            MyCommon.roomSelected = roomId
            startActivity(this)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btn_add_to_cart -> {
                addProductToCart()
            }

            R.id.img_decrease -> {
                if (quantity > 1) {
                    quantity -= 1
                    totalPrice -= unitPrice

                    tv_product_detail_quantity.text = quantity.toString()
                    tv_product_detail_total.text = "Total: $$totalPrice"
                }
                checkQuantityEqualOne()
            }

            R.id.img_increase -> {
                quantity += 1
                totalPrice += unitPrice
                tv_product_detail_quantity.text = quantity.toString()
                tv_product_detail_total.text = "Total: $$totalPrice"
                checkQuantityEqualOne()
            }

            R.id.btn_chat -> {
                openChatLogActivity()
            }
        }
    }
}