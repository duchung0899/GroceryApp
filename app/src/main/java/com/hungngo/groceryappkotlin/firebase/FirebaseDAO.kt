package com.hungngo.groceryappkotlin.firebase

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hungngo.groceryappkotlin.model.Product

class FirebaseDAO(val context: Context) {

    companion object {
        private var mAuth = FirebaseAuth.getInstance()

        private var database = FirebaseDatabase.getInstance().getReference("Users")
    }

     fun getAllProduct(): MutableList<Product> {
        val listProduct = mutableListOf<Product>()
        database.child(mAuth.uid.toString()).child("Products")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        val product = ds.getValue(Product::class.java)
                        listProduct.add(product!!)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
                }

            })

        return listProduct
    }

    fun deleteProduct(id: Long) {
        database.child(mAuth.uid.toString()).child("Products").child(id.toString())
            .removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Product deleted", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun addProduct(product: Product) {
        database.child(mAuth.uid.toString()).child("Products").child(product.productId.toString()).setValue(product)
            .addOnSuccessListener {
                Toast.makeText(context, "Product Added", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getProductByCategory(categoryName: String): MutableList<Product>{
        val listProduct = mutableListOf<Product>()
        database.child(mAuth.uid!!).child("Products")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val product = ds.getValue(Product::class.java)
                        if (categoryName == "All"){
                            listProduct.add(product!!)
                        }else{
                            if (product!!.productCategory == categoryName){
                                listProduct.add(product)
                            }
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        return listProduct
    }

}