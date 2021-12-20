package com.hungngo.groceryappkotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.`interface`.IOnClickProductAdapter
import com.hungngo.groceryappkotlin.filter.FilterProductInShop
import com.hungngo.groceryappkotlin.model.Product

class ProductAdapter(
    val context: Context,
    var productList: MutableList<Product>,
    private val iOnClickProductAdapter: IOnClickProductAdapter
    ): RecyclerView.Adapter<ProductAdapter.ViewHolder>(), Filterable {

//    private var productList = mutableListOf<Product>()
    private var filterProductInShop: FilterProductInShop? = null
    private var filterList = productList

    fun setProduct(productList: MutableList<Product>){
        this.productList = productList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_product_buyer,
            parent,
            false
        )
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        Glide.with(context).load(product.productIcon).into(holder.imgProduct)
        holder.tvProductTitle.text = product.productTitle
        holder.tvOriginPrice.text = "$${product.productPrice.toString()}"
        holder.tvDiscountPrice.text = "$${product.discountPrice.toString()}"
        holder.tvDiscountNote.text = product.discountNote

        if (product.discountAvailable!!){
            // visible discount
            holder.tvDiscountPrice.visibility = View.VISIBLE
            holder.tvDiscountNote.visibility = View.VISIBLE

            holder.tvOriginPrice.paintFlags = holder.tvOriginPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvOriginPrice.setTextColor(ContextCompat.getColor(context, R.color.colorGray02))
//            holder.tvDiscountPrice.setTextColor(ContextCompat.getColor(context, R.color.red))
        }else{
            holder.tvDiscountPrice.visibility = View.GONE
            holder.tvDiscountNote.visibility = View.GONE

            holder.tvOriginPrice.setTextColor(ContextCompat.getColor(context, R.color.red))
            holder.tvOriginPrice.paintFlags = holder.tvOriginPrice.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        holder.itemView.setOnClickListener {
            iOnClickProductAdapter.clickToProduct(product)
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgProduct: ImageView = itemView.findViewById(R.id.img_user_product)
        val tvDiscountNote: TextView =itemView.findViewById(R.id.tv_user_discount_note)
        val tvProductTitle: TextView =itemView.findViewById(R.id.tv_user_product_title)
        val tvDiscountPrice: TextView =itemView.findViewById(R.id.tv_user_product_discount)
        val tvOriginPrice: TextView =itemView.findViewById(R.id.tv_user_product_price)
        val tvCity: TextView =itemView.findViewById(R.id.tv_user_product_area)
        val tvSold: TextView =itemView.findViewById(R.id.tv_user_product_amount)
    }

    override fun getFilter(): Filter {
        if (filterProductInShop == null){
            filterProductInShop = FilterProductInShop(this, filterList)
        }
        return filterProductInShop as FilterProductInShop
    }
}