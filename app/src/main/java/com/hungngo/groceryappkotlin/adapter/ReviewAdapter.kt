package com.hungngo.groceryappkotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.model.CartItem

class ReviewAdapter(val context: Context,private val listItem: MutableList<CartItem>): RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    val listRating = mutableListOf<Int>()
    val listReview = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = listItem[position]
        if (item.itemIcon != null){
            Glide.with(context).load(item.itemIcon).into(holder.imgItem)
        }
        holder.tvTitle.text = item.itemName
        holder.tvQuantity.text = "x${item.itemQuantity}"
        holder.tvPrice.text = "$${item.itemTotalPrice}"


        var numStar = 0
        var review = ""
        holder.rbRating.onRatingBarChangeListener =
            RatingBar.OnRatingBarChangeListener { ratingBar, rating, fromUser ->
                numStar = rating.toInt()
            }

        holder.edtReview.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
            override fun afterTextChanged(s: Editable?) {
                review = s.toString()
            }
        })

        if (numStar != 0 && review != ""){
            listRating.add(numStar)
            listReview.add(review)
        }
    }

    override fun getItemCount(): Int {
        return listItem.size
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgItem: ImageView  = itemView.findViewById(R.id.img_item_review)
        val tvTitle: TextView  = itemView.findViewById(R.id.tv_item_review_title)
        val tvQuantity: TextView  = itemView.findViewById(R.id.tv_item_review_quantity)
        val tvPrice: TextView  = itemView.findViewById(R.id.tv_item_review_price)
        val rbRating: RatingBar  = itemView.findViewById(R.id.rb_rating)
        val edtReview: EditText  = itemView.findViewById(R.id.edt_review)
    }
}