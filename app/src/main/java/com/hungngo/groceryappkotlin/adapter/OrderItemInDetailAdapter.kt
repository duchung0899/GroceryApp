package com.hungngo.groceryappkotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.model.CartItem

@SuppressLint("SetTextI18n")
class OrderItemInDetailAdapter(
    val context: Context,
    private val listOrderItems: MutableList<CartItem>,
    private val iOnClick: IOnClickToItem): RecyclerView.Adapter<OrderItemInDetailAdapter.ViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_order_item_indetail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cartItem = listOrderItems[position]

        // Save/restore the open/close state.
        // You need to provide a String id which uniquely defines the data object.
        viewBinderHelper.bind(holder.swipeLayout, cartItem.itemPid.toString())
        // set only one item can swipe at the same time
        viewBinderHelper.setOpenOnlyOne(true)

        try {
            Glide.with(context).load(cartItem.itemIcon).into(holder.imgOrderItem)
        } catch (e: Exception) {
            holder.imgOrderItem.setImageResource(R.drawable.ic_cart_gray)
        }

        holder.tvName.text = cartItem.itemName
        holder.tvQuantity.text = "Quantity: ${cartItem.itemQuantity}"
        holder.tvTotal.text = "Total: $${cartItem.itemTotalPrice}"

        holder.btnRating.setOnClickListener {
            iOnClick.clickToReview(cartItem.itemPid)
        }
    }

    override fun getItemCount(): Int {
        return listOrderItems.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgOrderItem: ImageView = itemView.findViewById(R.id.img_item_order)
        val tvName: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity_order)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_item_total_price)
        val btnRating: TextView = itemView.findViewById(R.id.btn_rating)
        val swipeLayout: SwipeRevealLayout = itemView.findViewById(R.id.swl_order_item)
    }

    interface IOnClickToItem {
        fun clickToReview(itemPid: Long?)
    }
}