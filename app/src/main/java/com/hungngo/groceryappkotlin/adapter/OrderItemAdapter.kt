package com.hungngo.groceryappkotlin.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.model.CartItem

@SuppressLint("SetTextI18n")
class OrderItemAdapter(val context: Context, private val listOrderItems: MutableList<CartItem>):
    RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = listOrderItems[position]
        try {
            Glide.with(context).load(cartItem.itemIcon).into(holder.imgOrderItem)
        }catch (e: Exception){
            holder.imgOrderItem.setImageResource(R.drawable.ic_cart_gray)
        }

        holder.tvName.text = cartItem.itemName
        holder.tvQuantity.text = "Quantity: ${cartItem.itemQuantity}"
        holder.tvTotal.text = "Total: $${cartItem.itemTotalPrice}"
    }

    override fun getItemCount(): Int {
        return listOrderItems.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgOrderItem: ImageView = itemView.findViewById(R.id.img_item_order)
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity_order)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_item_total_price)
    }
}