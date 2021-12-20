package com.hungngo.groceryappkotlin.adapter.testNestedRecyclerView

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.OrderDetailSellerActivity
import com.hungngo.groceryappkotlin.activity.OrderDetailUserActivity
import com.hungngo.groceryappkotlin.adapter.OrderItemAdapter
import com.hungngo.groceryappkotlin.model.CartItem
import com.hungngo.groceryappkotlin.model.Order
import com.hungngo.groceryappkotlin.model.User

class ListOrderAdapter(
    val context: Context,
    private var listOrder: MutableList<Order>,
    private var iGoToFragment: IOnClickListener
    ): RecyclerView.Adapter<ListOrderAdapter.ViewHolder>() {

    fun setListOrder(listOrder: MutableList<Order>){
        this.listOrder = listOrder
        notifyDataSetChanged()
    }

    interface IOnClickListener{
        fun onClickButtonReBuy()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop_in_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = listOrder[position]
        setShopInfo(holder, order)

        setOrderItem(holder, order.items!!)

        holder.itemView.setOnClickListener{
            Intent(context, OrderDetailUserActivity::class.java).also {
                it.putExtra("order_object", order)
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        }

        holder.btnReBuy.setOnClickListener{
            iGoToFragment.onClickButtonReBuy()
        }

    }

    private fun setOrderItem(holder: ViewHolder, items: MutableList<CartItem>) {
        val orderItemAdapter = OrderItemAdapter(context, items)
        holder.rcvNested.adapter = orderItemAdapter
        holder.rcvNested.layoutManager = LinearLayoutManager(context)
    }

    private fun setShopInfo(holder: ViewHolder, order: Order) {
        val userRef = FirebaseDatabase.getInstance().getReference("Users")
        userRef.child(order.orderTo!!).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val shop = snapshot.getValue(User::class.java)
                holder.tvTitle.text = shop!!.shopName
                try {
                    Glide.with(context).load(shop.profileImage).into(holder.imgShop)
                }catch (e: Exception){
                    holder.imgShop.setImageResource(R.drawable.shop)
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun getItemCount(): Int {
        return listOrder.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgShop: ImageView = itemView.findViewById(R.id.img_shop_cart)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title_cart)
        val btnReBuy: Button = itemView.findViewById(R.id.btn_buy_again)
        val rcvNested: RecyclerView = itemView.findViewById(R.id.rcv_cart_item)
    }
}