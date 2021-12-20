package com.hungngo.groceryappkotlin.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hungngo.groceryappkotlin.model.CartItem

object CartDAO {
    private var mAuth = FirebaseAuth.getInstance()
    private val cartRef = FirebaseDatabase.getInstance().getReference("Cart")

    fun addItemToCart(cartItem: CartItem, uid: String){
        cartRef.child(uid).child(cartItem.itemSID!!).child(cartItem.itemPid!!.toString()).setValue(cartItem)
    }

    fun deleteItemFromCart(cartItem: CartItem, uid: String){
        cartRef.child(uid).child(cartItem.itemSID!!).child(cartItem.itemPid.toString()).removeValue()
    }

    fun deleteItemOfShop(uid: String, shopId: String){
        cartRef.child(uid).child(shopId).removeValue()
    }
}