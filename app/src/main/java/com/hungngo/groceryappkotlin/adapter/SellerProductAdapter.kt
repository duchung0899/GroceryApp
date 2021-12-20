package com.hungngo.groceryappkotlin.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.AddProductActivity
import com.hungngo.groceryappkotlin.filter.FilterProduct
import com.hungngo.groceryappkotlin.firebase.FirebaseDAO
import com.hungngo.groceryappkotlin.model.Product
import kotlinx.android.synthetic.main.item_product_seller.view.*

class SellerProductAdapter(var context: Context, var listProduct: MutableList<Product>) :
    RecyclerView.Adapter<SellerProductAdapter.ViewHolder>(), Filterable {

    private var filterList = listProduct
    private var filter: FilterProduct? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product_seller, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = listProduct[position]
        Glide.with(context).load(product.productIcon).into(holder.imgProduct)
        holder.tvTitle.text = product.productTitle
        holder.tvQuantity.text = product.productQuantity.toString()
        holder.tvOriginPrice.text = "$${product.productPrice.toString()}"
        holder.tvDiscountPrice.text = "$${product.discountPrice.toString()}"
        holder.tvDiscountNote.text = product.discountNote
        holder.tvProductDes.text = product.productDescription

        if (product.discountAvailable!!){
            // discount available
            holder.tvDiscountNote.visibility = View.VISIBLE
            holder.tvDiscountPrice.visibility = View.VISIBLE
        }else{
            // discount not available, hide it
            holder.tvDiscountNote.visibility = View.GONE
            holder.tvDiscountPrice.visibility = View.GONE
        }

        holder.itemView.setOnClickListener{
            // send product to AddProductActivity to show detail or u can update it
            val intent = Intent(context, AddProductActivity::class.java)
            val bundle = Bundle()
            bundle.putSerializable("PRODUCT", product)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }

        holder.imgDelete.setOnClickListener{
            AlertDialog.Builder(context).also {
                it.setTitle("Confirm Delete!!!")
                it.setMessage("Are you sure to delete ${product.productTitle}")
                it.setPositiveButton("Yes") { _, _ ->
                    deleteProduct(product.productId, position)
//                    context.startActivity(Intent(context, MainSellerActivity::class.java))
                }
                it.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                it.create().show()
            }

        }
    }

    private fun deleteProduct(productId: Long?, position: Int) {
//        val mAuth = FirebaseAuth.getInstance()
//        val ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.uid!!)
//        ref.child("Products").child(productId.toString()).removeValue()
//            .addOnCompleteListener {
//                if (it.isSuccessful){
//                    Toast.makeText(context, "Product deleted!!!", Toast.LENGTH_SHORT).show()
//                }
//            }

        listProduct.removeAt(position)
        notifyItemRemoved(position)
        FirebaseDAO(context).deleteProduct(productId!!)
    }

    override fun getItemCount(): Int = listProduct.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduct: ImageView = itemView.findViewById(R.id.img_item_order)
        val imgDelete: ImageView = itemView.findViewById(R.id.img_delete_product)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_item_name)
        val tvQuantity: TextView = itemView.findViewById(R.id.tv_item_quantity_order)
        val tvOriginPrice: TextView = itemView.findViewById(R.id.tv_pprice_item)
        val tvDiscountPrice: TextView = itemView.findViewById(R.id.tv_pdiscountprice_item)
        val tvDiscountNote: TextView = itemView.findViewById(R.id.tv_discount_note_item)
        val tvProductDes: TextView = itemView.findViewById(R.id.tv_item_total_price)
    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterProduct(this, filterList)
        }
        return filter as FilterProduct
    }

}