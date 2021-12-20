package com.hungngo.groceryappkotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.OrderDetailSellerActivity
import com.hungngo.groceryappkotlin.model.Order
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SetTextI18n")
class SellerOrderAdapter(
    val context: Context, private var orderList: MutableList<Order>
    ): RecyclerView.Adapter<SellerOrderAdapter.ViewHolder>() {

    fun setOrders(orderList: MutableList<Order>){
        this.orderList = orderList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_seller, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orderList[position]
        // load client name
        loadClientName(holder, order.orderBy)

        holder.quantity.text = "Quantity: ${order.items!!.size}"
        holder.price.text = "Price ${order.orderCost}"

        // change color for each order's status
        holder.status.text = order.orderStatus
        if (order.orderStatus == MyCommon.PROGRESS){
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
        }else{
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }

        // convert orderStartDate (Long) to date
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = order.orderStartDate!!
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.startDate.text = simpleDateFormat.format(calendar.time)

        holder.itemView.setOnClickListener {
            Intent(context, OrderDetailSellerActivity::class.java).also {
                it.putExtra("order_object", order)
                context.startActivity(it)
            }
        }
    }

    private fun loadClientName(holder: ViewHolder, orderBy: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(orderBy!!)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").value
                holder.clientName.text = "Client: $name"
            }
            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val clientName: TextView = itemView.findViewById(R.id.tv_order_client)
        val quantity: TextView = itemView.findViewById(R.id.tv_order_quantity)
        val price: TextView = itemView.findViewById(R.id.tv_order_price)
        val startDate: TextView = itemView.findViewById(R.id.tv_order_time)
        val status: TextView = itemView.findViewById(R.id.tv_order_status)
    }
}