package com.hungngo.groceryappkotlin.model

class Cart (
    val shopName: String? = null,
    val deliveryFee: Double? = null,
    val profileImage: String? =null,
    val listCartItem: MutableList<CartItem>? = null
)