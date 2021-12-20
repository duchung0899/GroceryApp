package com.hungngo.groceryappkotlin.adapter.testNestedRecyclerView

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.CartItemAdapter
import com.hungngo.groceryappkotlin.model.Cart
import com.hungngo.groceryappkotlin.model.CartItem
import com.hungngo.groceryappkotlin.model.User

class ListCartAdapter(
    val context: Context,
    private var listCart: MutableList<Cart>,
    private var listCartItem: MutableList<CartItem>
    ): RecyclerView.Adapter<ListCartAdapter.ViewHolder>() {

    fun setListCart(listCart: MutableList<Cart>){
        this.listCart = listCart
        notifyDataSetChanged()
    }

//    fun setListCartItem(listCartItem: MutableList<CartItem>){
//        this.listShop = listShop
//        notifyDataSetChanged()
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop_in_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cart = listCart[position]
        holder.tvTitle.text = cart.shopName
        try {
            Glide.with(context).load(cart.profileImage).into(holder.imgShop)
        }catch (e: Exception){
            holder.imgShop.setImageResource(R.drawable.shop)
        }

        val cartItemAdapter = CartItemAdapter(context, listCartItem, null)
        holder.rcvNested.adapter = cartItemAdapter
        holder.rcvNested.layoutManager = LinearLayoutManager(context)
    }

    override fun getItemCount(): Int {
        return listCart.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgShop: ImageView = itemView.findViewById(R.id.img_shop_cart)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_cart)
        val rcvNested: RecyclerView = itemView.findViewById(R.id.rcv_cart_item)
    }
}