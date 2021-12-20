package com.hungngo.groceryappkotlin.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.ShopDetailActivity
import com.hungngo.groceryappkotlin.model.User

class ShopAdapter(var context: Context): RecyclerView.Adapter<ShopAdapter.ViewHolder>() {
    private var listShop: MutableList<User> = mutableListOf()

    fun setShop(listShop: MutableList<User>){
        this.listShop = listShop
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shop, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shop = listShop[position]
        holder.tvShopName.text = shop.shopName
        Glide.with(context).load(shop.profileImage).into(holder.imgShop)

        if (shop.online!!){
            holder.imgStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_circle02)
        }else{
            holder.imgStatus.background = ContextCompat.getDrawable(context, R.drawable.bg_circle03)
        }

        holder.itemView.setOnClickListener {
            Intent(context, ShopDetailActivity::class.java).apply {
                putExtra("SHOP_INFO", shop)
                context.startActivity(this)
            }
        }
    }

    override fun getItemCount(): Int {
        return listShop.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgShop: ImageView = itemView.findViewById(R.id.img_shop)
        val imgStatus: ImageView = itemView.findViewById(R.id.img_status)
        val tvShopName: TextView = itemView.findViewById(R.id.tv_shop_name)
    }
}