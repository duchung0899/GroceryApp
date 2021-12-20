package com.hungngo.groceryappkotlin.adapter.fragment_adt

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.hungngo.groceryappkotlin.MyCommon
import com.hungngo.groceryappkotlin.fragment.tab_in_order.CompletedFragment
import com.hungngo.groceryappkotlin.fragment.tab_in_order.ConfirmFragment
import com.hungngo.groceryappkotlin.fragment.tab_in_order.PendingFragment
import com.hungngo.groceryappkotlin.fragment.tab_in_order.ReviewFragment

class TabOrderAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle)
    : FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        val fragment: Fragment
        when(position){
            0 -> {
                fragment = ConfirmFragment()
                bundle.putString("Order_State", MyCommon.CONFIRM)
                fragment.arguments = bundle
                return fragment
            }
            1 -> {
                fragment = ConfirmFragment()
                bundle.putString("Order_State", MyCommon.PROGRESS)
                fragment.arguments = bundle
                return fragment
            }
            2 -> {
                fragment = ConfirmFragment()
                bundle.putString("Order_State", MyCommon.COMPLETED)
                fragment.arguments = bundle
                return fragment
            }
            3 -> {
                fragment = ConfirmFragment()
                bundle.putString("Order_State", MyCommon.CANCEL)
                fragment.arguments = bundle
                return fragment
            }
            4 -> {
//                fragment = ConfirmFragment()
//                bundle.putString("Order_State", MyCommon.CANCEL)
//                fragment.arguments = bundle
                return ReviewFragment.newInstance("", "")
            }
        }
        return CompletedFragment()
    }

}