package com.hungngo.groceryappkotlin.filter

import android.widget.Filter
import com.hungngo.groceryappkotlin.adapter.ProductAdapter
import com.hungngo.groceryappkotlin.adapter.SellerProductAdapter
import com.hungngo.groceryappkotlin.model.Product
import java.util.*

class FilterProductInShop(private var adapter: ProductAdapter, private var filterList: MutableList<Product>): Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val results = FilterResults()
        val strSearch = constraint.toString().toLowerCase(Locale.ROOT)
        //validate data for searching query
        if (constraint != null && constraint.isNotEmpty()){
            //store our filter list
            val tmpList = mutableListOf<Product>()
            for (item  in filterList){
                if (item.productTitle!!.toLowerCase(Locale.ROOT).contains(strSearch)||
                        item.productCategory!!.toLowerCase(Locale.ROOT).contains(strSearch)){
                    tmpList.add(item)
                }
            }
            results.count = tmpList.size
            results.values = tmpList
        }else{
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.productList = results!!.values as MutableList<Product>
        adapter.notifyDataSetChanged()
    }
}