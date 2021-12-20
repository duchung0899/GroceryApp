package com.hungngo.groceryappkotlin.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.R
import com.hungngo.groceryappkotlin.adapter.fragment_adt.TabOrderAdapter
import kotlinx.android.synthetic.main.activity_user_order.*

class UserOrderActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_order)

        img_back.setOnClickListener(this)

        val tabOrderAdapter = TabOrderAdapter(supportFragmentManager, lifecycle)
        vp2_order.adapter = tabOrderAdapter

        TabLayoutMediator(tl_order, vp2_order) { tab, position ->
            tab.text = MyCommon.orderStatesList[position]
        }.attach()

        // if user click to confirm/pending/review image, set selected tab
        val position = intent.getIntExtra("Position", -1)
        if (position != -1){
            tl_order.getTabAt(position)!!.select()
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.img_back -> onBackPressed()
        }
    }
}