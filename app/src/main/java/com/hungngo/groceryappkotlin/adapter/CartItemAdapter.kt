package com.hungngo.groceryappkotlin.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.chauthai.swipereveallayout.ViewBinderHelper
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.`interface`.IOnClickCartAdapter
import com.hungngo.groceryappkotlin.model.CartItem

@SuppressLint("SetTextI18n")
class CartItemAdapter(
    var context: Context,
    private var listItem: MutableList<CartItem>,
    private var mIOnClickCartAdapter: IOnClickCartAdapter?): RecyclerView.Adapter<CartItemAdapter.ViewHolder>() {

    private val viewBinderHelper = ViewBinderHelper()

    fun setCart(listItem: MutableList<CartItem>){
        this.listItem = listItem
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // get your data object first.
        val cartItem = listItem[position]

        // check if quantity equal one, set gray color for minus button
        checkQuantityEqualOne(holder, cartItem.itemQuantity!!)

        // Save/restore the open/close state.
        // You need to provide a String id which uniquely defines the data object.
        viewBinderHelper.bind(holder.swipeLayout, cartItem.itemPid.toString())
        // set only one item can swipe at the same time
        viewBinderHelper.setOpenOnlyOne(true)

        // get total of all items to pass to CartFragment
        var total = 0.0
        listItem.forEach {
            total += it.itemTotalPrice!!
        }

        try {
            Glide.with(context).load(cartItem.itemIcon).into(holder.imgItem)
        }catch (e: Exception){
            holder.imgItem.setImageResource(R.drawable.ic_cart_gray)
        }

        holder.tvTitle.text = cartItem.itemName
        holder.tvUnitPrice.text = "$${cartItem.itemUnitPrice.toString()}"
        holder.tvTotal.text = "$${cartItem.itemTotalPrice.toString()}"
        holder.tvQuantity.text = cartItem.itemQuantity.toString()

        holder.imgIncrease.setOnClickListener{
            // increase item and quantity
            increaseItem(cartItem, holder)

            // send total price after increase item to card
            total+= cartItem.itemUnitPrice!!
            mIOnClickCartAdapter?.increaseItem(cartItem, total)
        }

        holder.imgDecrease.setOnClickListener{
            if (cartItem.itemQuantity!! > 1){
                // decrease item and quantity
                decreaseItem(cartItem, holder)

                // send total price after decrease item to card
                total-= cartItem.itemUnitPrice!!
                mIOnClickCartAdapter?.decreaseItem(cartItem, total)
            }
        }

        holder.btnDelete.setOnClickListener{
            mIOnClickCartAdapter?.deleteItem(cartItem, position)
        }
    }

    private fun decreaseItem(cartItem: CartItem, holder: CartItemAdapter.ViewHolder) {
        cartItem.itemQuantity = cartItem.itemQuantity?.minus(1)
        cartItem.itemTotalPrice = cartItem.itemTotalPrice?.minus(cartItem.itemUnitPrice!!)

        // update view
        holder.tvQuantity.text = cartItem.itemQuantity.toString()
        holder.tvTotal.text = "$${cartItem.itemTotalPrice.toString()}"
        checkQuantityEqualOne(holder, cartItem.itemQuantity!!)
    }

    private fun increaseItem(cartItem: CartItem, holder: ViewHolder) {
        cartItem.itemQuantity = cartItem.itemQuantity?.plus(1)
        cartItem.itemTotalPrice = cartItem.itemTotalPrice?.plus(cartItem.itemUnitPrice!!)

        // update view
        holder.tvQuantity.text = cartItem.itemQuantity.toString()
        holder.tvTotal.text = "$${cartItem.itemTotalPrice.toString()}"
//        holder.tvTotal.text = String.format("$%.2f",cartItem.itemTotalPrice)
        checkQuantityEqualOne(holder, cartItem.itemQuantity!!)
    }

    private fun checkQuantityEqualOne(holder: ViewHolder, quantity: Int){
        if (quantity == 1){
            holder.imgDecrease.setBackgroundColor(ContextCompat.getColor(context, R.color.colorGray02))
        }else{
            holder.imgDecrease.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
        }
    }

    override fun getItemCount(): Int = listItem.size

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgItem: ImageView = itemView.findViewById(R.id.img_cart_item)
        val imgIncrease: ImageView = itemView.findViewById(R.id.img_item_increase)
        val imgDecrease: ImageView = itemView.findViewById(R.id.img_item_decrease)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_item_title)
        val tvUnitPrice: TextView = itemView.findViewById(R.id.tv_item_unit_price)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_item_total)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity)

        val btnDelete: TextView = itemView.findViewById(R.id.btn_cart_delete)
        val swipeLayout: SwipeRevealLayout = itemView.findViewById(R.id.swl_order_item)
    }

}