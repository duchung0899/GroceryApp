package com.hungngo.groceryappkotlin.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.activity.ChatLogActivity
import com.hungngo.groceryappkotlin.activity.MainBuyerActivity
import com.hungngo.groceryappkotlin.adapter.CategoryAdapter
import com.hungngo.groceryappkotlin.adapter.ShopAdapter
import com.hungngo.groceryappkotlin.model.Category
import com.hungngo.groceryappkotlin.model.User
import com.hungngo.groceryappkotlin.service.MyFirebaseMessagingService
import kotlinx.android.synthetic.main.activity_main_buyer.*
import kotlinx.android.synthetic.main.activity_main_buyer.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*

@SuppressLint("SetTextI18n")
class HomeFragment : Fragment(), View.OnClickListener {

    private lateinit var shopAdapter: ShopAdapter
    private var listShop = mutableListOf<User>()

    private lateinit var categoryAdapter: CategoryAdapter
    private var listCategory = mutableListOf<Category>()

    private lateinit var mAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)

        view.img_chat_home.setOnClickListener(this)

        val user = arguments?.getSerializable("USER") as User?
        val mainActivity = activity as MainBuyerActivity
        if (user != null){
            mainActivity.rl_buyer.visibility = View.VISIBLE
            mainActivity.rl_buyer.tv_buyer_title.text = "${user.name} (${user.userType})"
        }

        mAuth = FirebaseAuth.getInstance()

        shopAdapter = ShopAdapter(view.context)
        view.rcv_home_shop.adapter = shopAdapter
        view.rcv_home_shop.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        categoryAdapter = CategoryAdapter(view.context)
        view.rcv_home_category.adapter = categoryAdapter
        view.rcv_home_category.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        getShopInfo()

        getCategoryInfo()

        return view
    }

    private fun getCategoryInfo() {
        listCategory.add(Category(1, "Beverages", R.drawable.drink2))
        listCategory.add(Category(2, "Beauty & Personal Care", R.drawable.skin_care))
        listCategory.add(Category(3, "Snacks", R.drawable.snack))
        listCategory.add(Category(4, "Chocolate", R.drawable.chocolate))
        listCategory.add(Category(5, "Dairy", R.drawable.dairy))
        listCategory.add(Category(6, "Frozen Food", R.drawable.frozenfrozen_food))
        listCategory.add(Category(7, "Fruits", R.drawable.fruit))
        listCategory.add(Category(8, "Pet Care", R.drawable.petcare))
        listCategory.add(Category(9, "Pharmacy", R.drawable.pharmecy))
        listCategory.add(Category(10, "Vegetables", R.drawable.vegetables2))
        listCategory.add(Category(11, "Others", R.drawable.othets))

        categoryAdapter.setCategory(listCategory)

    }

    private fun getShopInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("userType").equalTo(MyCommon.SELLER)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    listShop.clear()
                    for (ds in snapshot.children){
                        val shop = ds.getValue(User::class.java)
                        listShop.add(shop!!)
                    }
                    listShop.sortByDescending { it.online }
                    shopAdapter.setShop(listShop)
                }
                override fun onCancelled(error: DatabaseError) {
                }

            })

    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.img_chat_home -> {
//                val intent = Intent(context, ChatLogActivity::class.java)
//                requireContext().startActivity(intent)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            HomeFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}