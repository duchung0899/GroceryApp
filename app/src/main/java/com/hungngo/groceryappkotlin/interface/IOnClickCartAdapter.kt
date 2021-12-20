package com.hungngo.groceryappkotlin.`interface`

import com.hungngo.groceryappkotlin.model.CartItem

interface IOnClickCartAdapter{
    fun increaseItem(cartItem: CartItem, total: Double)
    fun decreaseItem(cartItem: CartItem, total: Double)
    fun deleteItem(cartItem: CartItem, position: Int)
}