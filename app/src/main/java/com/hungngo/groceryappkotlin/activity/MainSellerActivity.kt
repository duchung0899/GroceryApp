package com.hungngo.groceryappkotlin.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.fragment.seller.SellerHomeFragment
import com.hungngo.groceryappkotlin.fragment.seller.SellerMessageFragment
import com.hungngo.groceryappkotlin.fragment.seller.SellerOrderFragment
import com.hungngo.groceryappkotlin.fragment.seller.SellerProfileFragment
import kotlinx.android.synthetic.main.activity_main_seller.*

class MainSellerActivity : AppCompatActivity(), AHBottomNavigation.OnTabSelectedListener {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_seller)

        mAuth = FirebaseAuth.getInstance()

        setBottomNavigation()

        checkUser()
    }

    private fun setBottomNavigation() {
        val item1 =
            AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_home, R.color.colorPrimary)
        val item2 = AHBottomNavigationItem(
            R.string.tab_2,
            R.drawable.ic_order_gray,
            R.color.colorPrimary
        )
        val item3 = AHBottomNavigationItem(
            R.string.tab_3,
            R.drawable.ic_message,
            R.color.colorPrimary
        )

        val item4 = AHBottomNavigationItem(
            R.string.tab_4,
            R.drawable.ic_person_gray,
            R.color.colorPrimary
        )

        ah_bottom_navigation.addItem(item1)
        ah_bottom_navigation.addItem(item2)
        ah_bottom_navigation.addItem(item3)
        ah_bottom_navigation.addItem(item4)

        ah_bottom_navigation.isColored = false
        ah_bottom_navigation.accentColor = resources.getColor(R.color.colorPrimary);
        ah_bottom_navigation.inactiveColor = resources.getColor(R.color.colorGray02);

        ah_bottom_navigation.titleState = AHBottomNavigation.TitleState.ALWAYS_SHOW
        ah_bottom_navigation.setOnTabSelectedListener(this)
    }


    private fun checkUser() {
        val user: FirebaseUser? = mAuth.currentUser
        if (user == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }else{
            loadFragment(SellerHomeFragment.newInstance())
        }
    }

//    private fun showProductsUI() {
//        loadMyProduct()
//        // show products ui and hide orders ui
//        rl_product.visibility = View.VISIBLE
//        rl_order.visibility = View.GONE
//
//        tv_tab_product.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
//        tv_tab_product.setBackgroundResource(R.drawable.bg_reg_3)
//
//        tv_tab_order.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
//        tv_tab_order.setBackgroundColor(
//            ContextCompat.getColor(
//                applicationContext,
//                android.R.color.transparent
//            )
//        )
//    }
//
//    private fun showOrdersUI() {
//        loadMyOrder()
//        // show orders ui and hide products ui
//        rl_order.visibility = View.VISIBLE
//        rl_product.visibility = View.GONE
//
//        tv_tab_order.setTextColor(ContextCompat.getColor(applicationContext, R.color.black))
//        tv_tab_order.setBackgroundResource(R.drawable.bg_reg_3)
//
//        tv_tab_product.setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
//        tv_tab_product.setBackgroundColor(
//            ContextCompat.getColor(
//                applicationContext,
//                android.R.color.transparent
//            )
//        )
//    }

    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_fragment_shop, fragment).commit()
    }

    override fun onTabSelected(position: Int, wasSelected: Boolean): Boolean {
        when(position){
            0 -> {
                loadFragment(SellerHomeFragment.newInstance())
            }
            1 -> {
                loadFragment(SellerOrderFragment.newInstance())

            }
            2 -> {
                val transaction = supportFragmentManager.beginTransaction()
                transaction.replace(R.id.main_fragment_shop, SellerMessageFragment.newInstance())
                    .commit()
            }
            3 -> {
                loadFragment(SellerProfileFragment.newInstance())
            }
        }
        return true
    }
}