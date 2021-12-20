package com.hungngo.groceryappkotlin.adapter

import android.content.Context
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.model.Category

class CategoryAdapter(val context: Context): RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var listCategory = mutableListOf<Category>()

    fun setCategory(listCategory : MutableList<Category>){
        this.listCategory = listCategory
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = listCategory[position]
        holder.tvNameCategory.text = category.name
        Glide.with(context).load(category.image).into(holder.imageCate)
    }

    override fun getItemCount(): Int {
        return listCategory.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageCate: ImageView = itemView.findViewById(R.id.img_category)
        val tvNameCategory: TextView = itemView.findViewById(R.id.tv_name_category)
    }
}